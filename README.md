<div align="center">

<img src="./.github/assets/komik_logo.png" alt="Logo Mihon Sync" title="Logo Mihon Sync" width="80"/>

# Mihon Sync

### Pembaca komik dengan sinkronisasi Google Drive
Fork dari [Mihon](https://github.com/mihonapp/mihon) yang menambahkan sinkronisasi pustaka dan progres baca antar perangkat lewat akun Google.

[![Rilis terbaru](https://img.shields.io/github/v/release/samsulrifai/mihon_sync?label=Rilis&labelColor=27303D&color=FF0510)](https://github.com/samsulrifai/mihon_sync/releases/latest)
[![CI](https://img.shields.io/github/actions/workflow/status/samsulrifai/mihon_sync/build.yml?labelColor=27303D)](https://github.com/samsulrifai/mihon_sync/actions/workflows/build.yml)
[![Lisensi: Apache-2.0](https://img.shields.io/github/license/samsulrifai/mihon_sync?labelColor=27303D&color=0877d2)](/LICENSE)

## Unduh

Unduh APK dari [halaman rilis](https://github.com/samsulrifai/mihon_sync/releases/latest). Kalau ragu, pilih `mihon-sync-arm64-v8a-….apk`.

*Butuh Android 8.0 atau lebih baru, dan Google Play Services untuk sinkronisasi.*

Mihon Sync terpasang sebagai aplikasi terpisah (`app.mihon.sync`), jadi bisa dipakai berdampingan dengan Mihon asli. Setelah terpasang, aplikasi memberi tahu sendiri kalau ada rilis baru.

## Fitur tambahan

<div align="left">

Semua fitur Mihon tetap ada, ditambah:

* **Sinkronisasi Google Drive.** Masuk dengan akun Google di *Pengaturan → Data dan penyimpanan* untuk menyinkronkan pustaka, kategori, progres baca, riwayat, tracker, dan repo ekstensi antar perangkat.
  * Bab yang sudah dibaca di perangkat mana pun dianggap sudah dibaca, dan halaman terakhir yang paling jauh yang dipakai.
  * Komik yang dihapus dari pustaka di satu perangkat ikut terhapus di perangkat lain.
  * Sinkron otomatis saat aplikasi dibuka dan ditutup, dan bisa juga terjadwal (tiap 6 jam, 12 jam, atau harian).
  * Data disimpan di folder aplikasi tersembunyi di Google Drive milikmu sendiri, tanpa server pihak ketiga. Pengaturan aplikasi tidak ikut disinkronkan, jadi tiap perangkat tetap punya pengaturannya sendiri.
* **Pasang ekstensi yang belum ada.** Setelah sinkron ke perangkat baru, satu ketukan memasang semua ekstensi yang dibutuhkan komik di pustaka.
* **Ikon sumber di pustaka.** Ikon website komik tampil di sebelah lencana bahasa pada sampul. Bisa dimatikan di menu tampilan pustaka.
* **Alasan komik dilewati saat refresh.** Setelah refresh manual, notifikasi menunjukkan komik mana yang dilewati dan kenapa, misalnya karena masih ada bab belum dibaca atau komik sudah tamat.
* **Pembaruan dari GitHub.** Aplikasi memeriksa rilis baru di repo ini.
* Nama dan logo baru: **Mihon Sync** dengan logo KOMIK.

</div>

## Build sendiri

<div align="left">

Butuh JDK 21 dan Android SDK.

```bash
./gradlew assembleDebug
```

Login Google tidak memakai *client secret*. Google mengenali aplikasi dari nama paket dan SHA-1 sertifikat penandatangannya, jadi kalau kamu build dengan kunci sendiri, daftarkan dulu di [Google Cloud Console](https://console.cloud.google.com):

1. Aktifkan **Google Drive API**.
2. Atur *OAuth consent screen* dengan tipe *External*. Selama statusnya masih *Testing*, tambahkan akun yang dipakai ke *Test users*.
3. Buat OAuth client bertipe **Android** untuk setiap kombinasi paket dan kunci yang dipakai:
   * Debug: `app.mihon.dev` dengan SHA-1 dari `~/.android/debug.keystore`
   * Rilis: `app.mihon.sync` dengan SHA-1 dari kunci rilis

</div>

## Merilis versi baru

<div align="left">

1. Sekali saja: jalankan `scripts/setup-release-signing.sh`. Skrip ini membuat kunci rilis, menyimpannya sebagai secret GitHub Actions, dan menampilkan SHA-1 untuk Google Cloud. Simpan cadangan keystore dan `keystore.properties` di tempat aman. Tanpa keduanya, pembaruan tidak bisa dipasang di atas versi lama.
2. Naikkan `versionName` (misalnya `0.21.2-sync.2`) dan `versionCode` di `app/build.gradle.kts`.
3. Buat tag yang sama dengan versinya, lalu push:
   ```bash
   git tag v0.21.2-sync.2 && git push origin v0.21.2-sync.2
   ```
   GitHub Actions akan membangun, menandatangani, dan menerbitkan APK ke halaman rilis.

Versi mengikuti versi Mihon yang menjadi dasarnya, ditambah akhiran `-sync.N` untuk rilis fork ini.

</div>

### Kredit

Mihon Sync dibangun di atas [Mihon](https://github.com/mihonapp/mihon). Terima kasih untuk semua kontributor Mihon dan Tachiyomi.

<a href="https://github.com/mihonapp/mihon/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=mihonapp/mihon" alt="Kontributor Mihon" title="Kontributor Mihon" width="800"/>
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
