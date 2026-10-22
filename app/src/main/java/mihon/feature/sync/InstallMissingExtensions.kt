package mihon.feature.sync

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.extension.ExtensionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import logcat.LogPriority
import mihon.core.metro.AppCoroutineScope
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.manga.interactor.GetFavorites
import tachiyomi.domain.source.service.SourceManager

/**
 * Installs the extensions that provide the sources of library entries, such as after syncing the
 * library to a new device.
 */
@Inject
class InstallMissingExtensions(
    @AppCoroutineScope private val scope: CoroutineScope,
    private val getFavorites: GetFavorites,
    private val sourceManager: SourceManager,
    private val extensionManager: ExtensionManager,
    private val extensionStoreRepository: ExtensionStoreRepository,
) {

    /**
     * Starts installing the missing extensions and returns how many there are.
     */
    suspend fun await(): Int {
        val missingSourceIds = getFavorites.await()
            .map { it.source }
            .distinct()
            .filter { sourceManager.get(it) == null }
            .toSet()
        if (missingSourceIds.isEmpty()) return 0

        // Untrusted extensions are installed but provide no sources, they only need to be trusted
        val installedPackages = (extensionManager.getLoadedExtensions() + extensionManager.getNotLoadedExtensions())
            .mapTo(mutableSetOf()) { it.pkgName }

        val extensions = extensionStoreRepository.fetchExtensions()
            .filter { extension ->
                extension.pkgName !in installedPackages && extension.sources.any { it.id in missingSourceIds }
            }
            .groupBy { it.pkgName }
            .map { (_, listings) -> listings.maxBy { it.versionCode } }

        extensions.forEach { extension ->
            scope.launch(Dispatchers.IO) {
                extensionManager.installExtension(extension)
                    .catch { logcat(LogPriority.ERROR, it) { "Failed to install ${extension.pkgName}" } }
                    .collect()
            }
        }
        return extensions.size
    }
}
