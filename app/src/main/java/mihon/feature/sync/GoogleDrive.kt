package mihon.feature.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import java.io.File

/**
 * Stores a single backup file in the hidden app data folder of the user's Google Drive.
 *
 * Signing in goes through Google Play services, which recognizes the app by its package name and
 * signing certificate, so no client secret has to be shipped with the app.
 */
@Inject
@SingleIn(AppScope::class)
class GoogleDrive(
    private val context: Context,
    private val networkHelper: NetworkHelper,
    private val json: Json,
    private val preferences: GoogleDriveSyncPreferences,
) {

    private val client get() = networkHelper.client

    private val authorizationRequest = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(SCOPE_APP_DATA), Scope(SCOPE_EMAIL)))
        .build()

    val isSignedIn: Boolean
        get() = preferences.accountEmail.get().isNotEmpty()

    /**
     * Has a [AuthorizationResult.getPendingIntent] to show the consent screen with when the user
     * hasn't granted access yet.
     */
    suspend fun authorize(): AuthorizationResult {
        return Identity.getAuthorizationClient(context).authorize(authorizationRequest).await()
    }

    fun getAuthorizationResult(data: Intent?): AuthorizationResult {
        return Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
    }

    suspend fun completeSignIn(result: AuthorizationResult) {
        val token = requireNotNull(result.accessToken) { "No access token" }
        preferences.accountEmail.set(fetchEmail(token))
    }

    suspend fun signOut() {
        try {
            getAccessToken()?.let { revoke(it) }
        } finally {
            preferences.accountEmail.delete()
            preferences.lastSyncTimestamp.delete()
            preferences.syncedLibrary.delete()
        }
    }

    /**
     * Returns null when the user has to sign in again, which can't be done in the background.
     */
    suspend fun getAccessToken(): String? {
        val result = authorize()
        return result.accessToken.takeUnless { result.hasResolution() }
    }

    /**
     * Returns the id of the sync file, or null when nothing was synced to this account yet.
     */
    suspend fun findSyncFile(token: String): String? = withContext(Dispatchers.IO) {
        val url = "$API_URL/drive/v3/files".toHttpUrl().newBuilder()
            .addQueryParameter("spaces", APP_DATA_FOLDER)
            .addQueryParameter("q", "name = '$SYNC_FILE_NAME' and trashed = false")
            .addQueryParameter("fields", "files(id)")
            .build()
        val response = client.newCall(authorizedRequest(token).url(url).build()).awaitSuccess()
        with(json) { response.parseAs<FileList>() }.files.firstOrNull()?.id
    }

    suspend fun download(token: String, fileId: String, destination: File) = withContext(Dispatchers.IO) {
        val request = authorizedRequest(token).url("$API_URL/drive/v3/files/$fileId?alt=media").build()
        client.newCall(request).awaitSuccess().body.source().use { source ->
            destination.sink().buffer().use { it.writeAll(source) }
        }
    }

    /**
     * Replaces the content of [fileId], or creates the sync file when it's null.
     */
    suspend fun upload(token: String, fileId: String?, source: File) = withContext(Dispatchers.IO) {
        val content = source.asRequestBody(OCTET_STREAM)
        val request = if (fileId != null) {
            authorizedRequest(token)
                .url("$API_URL/upload/drive/v3/files/$fileId?uploadType=media")
                .patch(content)
                .build()
        } else {
            val metadata = buildJsonObject {
                put("name", SYNC_FILE_NAME)
                putJsonArray("parents") { add(APP_DATA_FOLDER) }
            }
            val body = MultipartBody.Builder()
                .setType("multipart/related".toMediaType())
                .addPart(metadata.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .addPart(content)
                .build()
            authorizedRequest(token)
                .url("$API_URL/upload/drive/v3/files?uploadType=multipart")
                .post(body)
                .build()
        }
        client.newCall(request).awaitSuccess().close()
    }

    private suspend fun fetchEmail(token: String): String = withContext(Dispatchers.IO) {
        val request = authorizedRequest(token).url(USER_INFO_URL).build()
        val response = client.newCall(request).awaitSuccess()
        with(json) { response.parseAs<UserInfo>() }.email
    }

    private suspend fun revoke(token: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(REVOKE_URL)
            .post(FormBody.Builder().add("token", token).build())
            .build()
        client.newCall(request).awaitSuccess().close()
    }

    private fun authorizedRequest(token: String): Request.Builder {
        return Request.Builder()
            .header("Authorization", "Bearer $token")
            .cacheControl(CacheControl.FORCE_NETWORK)
    }

    @Serializable
    private data class FileList(val files: List<DriveFile> = emptyList())

    @Serializable
    private data class DriveFile(val id: String)

    @Serializable
    private data class UserInfo(val email: String)

    companion object {
        private const val API_URL = "https://www.googleapis.com"
        private const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
        private const val REVOKE_URL = "https://oauth2.googleapis.com/revoke"

        private const val SCOPE_APP_DATA = "https://www.googleapis.com/auth/drive.appdata"
        private const val SCOPE_EMAIL = "https://www.googleapis.com/auth/userinfo.email"

        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val SYNC_FILE_NAME = "mihon_sync.tachibk"

        private val OCTET_STREAM = "application/octet-stream".toMediaType()
    }
}
