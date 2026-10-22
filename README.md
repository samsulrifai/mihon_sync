<div align="center">

<img src="./.github/assets/komik_logo.png" alt="Mihon Sync logo" title="Mihon Sync logo" width="80"/>

# Mihon Sync

### Comic reader with Google Drive sync
A fork of [Mihon](https://github.com/mihonapp/mihon) that keeps your library and reading progress in sync across devices through your Google account.

[![Latest release](https://img.shields.io/github/v/release/samsulrifai/mihon_sync?label=Release&labelColor=27303D&color=FF0510)](https://github.com/samsulrifai/mihon_sync/releases/latest)
[![CI](https://img.shields.io/github/actions/workflow/status/samsulrifai/mihon_sync/build.yml?labelColor=27303D)](https://github.com/samsulrifai/mihon_sync/actions/workflows/build.yml)
[![License: Apache-2.0](https://img.shields.io/github/license/samsulrifai/mihon_sync?labelColor=27303D&color=0877d2)](/LICENSE)

## Download

Get the APK from the [releases page](https://github.com/samsulrifai/mihon_sync/releases/latest). If you're unsure which one to pick, use `mihon-sync-arm64-v8a-….apk`.

*Requires Android 8.0 or higher, and Google Play services for syncing.*

Mihon Sync installs as a separate app (`app.mihon.sync`), so it can be used next to the original Mihon. Once installed, the app lets you know when a new release is out.

## What this fork adds

<div align="left">

Everything Mihon has, plus:

* **Google Drive sync.** Sign in with your Google account in *Settings → Data and storage* to sync your library, categories, reading progress, history, trackers and extension repos between devices.
  * A chapter read on any device counts as read, and the furthest page read wins.
  * Entries removed from the library on one device are removed on the others too.
  * Syncs automatically when the app is opened and left, and optionally on a schedule (every 6 hours, 12 hours or daily).
  * Data is kept in a hidden app folder of your own Google Drive, with no third-party server. App settings aren't synced, so each device keeps its own.
* **Install missing extensions.** After syncing to a new device, one tap installs every extension your library needs.
* **Source icon in the library.** The comic website's icon is shown next to the language badge on covers. It can be turned off in the library display menu.
* **Why entries were skipped.** After a manual library update, a notification lists the entries that were skipped and why, for example because they have unread chapters or are completed.
* **Updates from GitHub.** The app checks this repository for new releases.
* A new name and logo: **Mihon Sync** with the KOMIK logo.

</div>

## Building

<div align="left">

Requires JDK 21 and the Android SDK.

```bash
./gradlew assembleDebug
```

Google sign-in uses no client secret. Google recognizes the app by its package name and the SHA-1 of its signing certificate, so when building with your own key, register it in the [Google Cloud Console](https://console.cloud.google.com) first:

1. Enable the **Google Drive API**.
2. Set up the *OAuth consent screen* as *External*. While it's in *Testing*, add the accounts you sign in with as *Test users*.
3. Create an OAuth client of type **Android** for every package and key combination you use:
   * Debug: `app.mihon.dev` with the SHA-1 of `~/.android/debug.keystore`
   * Release: `app.mihon.sync` with the SHA-1 of the release key

</div>

## Releasing a new version

<div align="left">

1. Once: run `scripts/setup-release-signing.sh`. It creates the release key, stores it as GitHub Actions secrets and prints the SHA-1 for Google Cloud. Keep a backup of the keystore and `keystore.properties` somewhere safe. Without them, updates can't be installed over existing installs.
2. Bump `versionName` (for example `0.21.2-sync.2`) and `versionCode` in `app/build.gradle.kts`.
3. Tag the same version and push the tag:
   ```bash
   git tag v0.21.2-sync.2 && git push origin v0.21.2-sync.2
   ```
   GitHub Actions builds and signs the APKs and creates a draft release with them.
4. Review the notes on the [releases page](https://github.com/samsulrifai/mihon_sync/releases) and publish the draft. The app only offers the update once it's published.

Versions follow the Mihon version they're based on, with a `-sync.N` suffix for this fork's releases.

</div>

### Credits

Mihon Sync is built on [Mihon](https://github.com/mihonapp/mihon). Thank you to everyone who has contributed to Mihon and Tachiyomi!

<a href="https://github.com/mihonapp/mihon/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=mihonapp/mihon" alt="Mihon app contributors" title="Mihon app contributors" width="800"/>
</a>

### Disclaimer

The developer(s) of this application does not have any affiliation with the content providers available, and this application hosts zero content.

Mihon Sync is an unofficial fork and is not affiliated with or endorsed by the Mihon Open Source Project. Please report issues with this fork [here](https://github.com/samsulrifai/mihon_sync/issues), not to Mihon.

### License

<pre>
Copyright © 2015 Javier Tomás
Copyright © 2024 Mihon Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>

This fork modifies the original Mihon source code; see the commit history for the changes.

</div>
