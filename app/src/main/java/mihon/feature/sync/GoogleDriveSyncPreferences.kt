package mihon.feature.sync

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

@Inject
@SingleIn(AppScope::class)
class GoogleDriveSyncPreferences(
    preferenceStore: PreferenceStore,
) {

    /** Email of the signed in Google account, empty when signed out. */
    val accountEmail: Preference<String> = preferenceStore.getString(
        Preference.appStateKey("google_drive_account_email"),
        "",
    )

    val syncInterval: Preference<Int> = preferenceStore.getInt("google_drive_sync_interval", 0)

    val syncOnAppStartAndExit: Preference<Boolean> = preferenceStore.getBoolean(
        "google_drive_sync_on_app_start_and_exit",
        true,
    )

    val lastSyncTimestamp: Preference<Long> = preferenceStore.getLong(
        Preference.appStateKey("google_drive_last_sync_timestamp"),
        0L,
    )

    /**
     * The library as of the last sync, to tell entries removed since then apart from ones never synced.
     */
    val syncedLibrary: Preference<Set<String>> = preferenceStore.getStringSet(
        Preference.appStateKey("google_drive_synced_library"),
        emptySet(),
    )
}
