package mihon.feature.sync

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.net.toUri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Inject
import eu.kanade.domain.manga.interactor.UpdateManga
import eu.kanade.tachiyomi.data.backup.BackupDecoder
import eu.kanade.tachiyomi.data.backup.BackupNotifier
import eu.kanade.tachiyomi.data.backup.create.BackupCreateWorker
import eu.kanade.tachiyomi.data.backup.create.BackupCreator
import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreWorker
import eu.kanade.tachiyomi.data.backup.restore.BackupRestorer
import eu.kanade.tachiyomi.data.backup.restore.RestoreOptions
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.util.system.cancelNotification
import eu.kanade.tachiyomi.util.system.isRunning
import eu.kanade.tachiyomi.util.system.setForegroundSafely
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.withContext
import logcat.LogPriority
import mihon.app.di.AppGraph
import mihon.app.di.appGraph
import mihon.core.metro.metroGraph
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.manga.interactor.GetFavorites
import tachiyomi.domain.manga.model.MangaUpdate
import tachiyomi.i18n.MR
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

/**
 * Syncs the library with Google Drive: the backup there is restored on top of the local library,
 * then a backup of the merged library replaces it.
 */
class GoogleDriveSyncWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val graph: AppGraph = context.metroGraph()

    @Inject private lateinit var googleDrive: GoogleDrive

    @Inject private lateinit var preferences: GoogleDriveSyncPreferences

    @Inject private lateinit var backupCreatorFactory: BackupCreator.Factory

    @Inject private lateinit var backupRestorerFactory: BackupRestorer.Factory

    @Inject private lateinit var backupDecoder: BackupDecoder

    @Inject private lateinit var getFavorites: GetFavorites

    @Inject private lateinit var updateManga: UpdateManga

    @Inject private lateinit var notifier: BackupNotifier

    override suspend fun doWork(): Result {
        graph.inject(this)

        if (!googleDrive.isSignedIn) return Result.failure()

        if (BackupRestoreWorker.isRunning(context.workManager) || BackupCreateWorker.isManualJobRunning(context)) {
            return Result.retry()
        }

        setForegroundSafely()

        return try {
            withContext(Dispatchers.IO) { sync() }
            preferences.lastSyncTimestamp.set(Clock.System.now().toEpochMilliseconds())
            Result.success()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logcat(LogPriority.ERROR, e)
            notifier.showSyncError(e.message)
            Result.failure()
        } finally {
            context.cancelNotification(Notifications.ID_RESTORE_PROGRESS)
        }
    }

    private suspend fun sync() {
        val token = googleDrive.getAccessToken()
            ?: throw IllegalStateException(context.stringResource(MR.strings.google_drive_sign_in_again))

        val remoteFile = File(context.cacheDir, "google_drive_sync_remote.tachibk")
        val localFile = File(context.cacheDir, "google_drive_sync_local.tachibk")
        try {
            val fileId = googleDrive.findSyncFile(token)
            if (fileId != null) {
                googleDrive.download(token, fileId, remoteFile)
                mergeRemote(remoteFile)
            } else if (getFavorites.await().isEmpty()) {
                // Nothing on either side yet, and an empty backup can't be created
                return
            }

            localFile.createNewFile()
            backupCreatorFactory.create(isAutoBackup = false).backup(localFile.toUri(), BACKUP_OPTIONS)
            googleDrive.upload(token, fileId, localFile)

            preferences.syncedLibrary.set(getFavorites.await().mapTo(mutableSetOf()) { libraryKey(it.source, it.url) })
        } finally {
            remoteFile.delete()
            localFile.delete()
        }
    }

    private suspend fun mergeRemote(remoteFile: File) {
        val uri = remoteFile.toUri()
        val localLibrary = getFavorites.await().mapTo(mutableSetOf()) { libraryKey(it.source, it.url) }
        val remoteLibrary = backupDecoder.decodeEntries(uri)
            .filter { it.favorite }
            .map { libraryKey(it.source, it.url) }
            .toSet()

        backupRestorerFactory.create(notifier = notifier, isSync = true).restore(uri, RESTORE_OPTIONS)

        // Restoring only ever adds to the library, so what was removed on either side since the last
        // sync has to be removed again
        val removed = preferences.syncedLibrary.get() - (localLibrary intersect remoteLibrary)
        if (removed.isEmpty()) return

        val updates = getFavorites.await()
            .filter { libraryKey(it.source, it.url) in removed }
            .map { MangaUpdate(it.id) { favoriteAt = null } }
        updateManga.awaitAll(updates)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            Notifications.ID_RESTORE_PROGRESS,
            notifier.showRestoreProgress(sync = true).build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    companion object {
        // App settings stay per device
        private val BACKUP_OPTIONS = BackupOptions(appSettings = false, privateSettings = false)
        private val RESTORE_OPTIONS = RestoreOptions(appSettings = false)

        private fun libraryKey(source: Long, url: String) = "$source:$url"

        suspend fun isRunning(context: Context): Boolean {
            return context.workManager.isRunning(TAG_MANUAL) || context.workManager.isRunning(TAG_AUTO)
        }

        fun setupTask(context: Context, prefInterval: Int? = null) {
            val preferences = context.appGraph.googleDriveSyncPreferences
            val interval = prefInterval ?: preferences.syncInterval.get()
            if (interval > 0 && preferences.accountEmail.get().isNotEmpty()) {
                val constraints = Constraints(
                    requiredNetworkType = NetworkType.CONNECTED,
                    requiresBatteryNotLow = true,
                )

                val request = PeriodicWorkRequestBuilder<GoogleDriveSyncWorker>(
                    interval.toLong(),
                    TimeUnit.HOURS,
                    10,
                    TimeUnit.MINUTES,
                )
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                    .addTag(TAG_AUTO)
                    .setConstraints(constraints)
                    .build()

                context.workManager.enqueueUniquePeriodicWork(TAG_AUTO, ExistingPeriodicWorkPolicy.UPDATE, request)
            } else {
                context.workManager.cancelUniqueWork(TAG_AUTO)
            }
        }

        fun startNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<GoogleDriveSyncWorker>()
                .addTag(TAG_MANUAL)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
            context.workManager.enqueueUniqueWork(TAG_MANUAL, ExistingWorkPolicy.KEEP, request)
        }
    }
}

private const val TAG_AUTO = "GoogleDriveSync"
private const val TAG_MANUAL = "$TAG_AUTO:manual"
