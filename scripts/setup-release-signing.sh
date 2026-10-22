#!/usr/bin/env bash
# Creates the release signing key for Mihon Sync and stores it as GitHub Actions secrets, so
# releases built by CI and locally are signed with the same key.
#
# Keep a backup of the keystore and keystore.properties somewhere safe: without them, updates
# can't be installed over existing installs and Google Drive sign-in stops working.
set -euo pipefail

repo="samsulrifai/mihon_sync"
keystore="$HOME/.android/mihon-sync-release.jks"
alias="mihonsync"
root="$(cd "$(dirname "$0")/.." && pwd)"
properties="$root/keystore.properties"

if [[ -e "$keystore" ]]; then
    echo "$keystore already exists, not overwriting it" >&2
    exit 1
fi

password="$(openssl rand -base64 48 | tr -d '/+=\n' | head -c 32)"

keytool -genkeypair -keystore "$keystore" -storetype PKCS12 -alias "$alias" \
    -keyalg RSA -keysize 4096 -validity 36500 \
    -storepass "$password" -keypass "$password" \
    -dname "CN=Mihon Sync, O=samsulrifai" >/dev/null 2>&1
chmod 600 "$keystore"

# Picked up by app/build.gradle.kts for local release builds
umask 077
cat > "$properties" <<EOF
storeFile=$keystore
storePassword=$password
keyAlias=$alias
keyPassword=$password
EOF

base64 -w0 "$keystore" | gh secret set SIGNING_KEY -R "$repo"
printf %s "$password" | gh secret set KEY_STORE_PASSWORD -R "$repo"
printf %s "$password" | gh secret set KEY_PASSWORD -R "$repo"
printf %s "$alias" | gh secret set ALIAS -R "$repo"
openssl rand -base64 16 | tr -d '\n' | gh secret set GRADLE_ENCRYPTION_KEY -R "$repo"

echo
echo "Secrets set on $repo:"
gh secret list -R "$repo"
echo
echo "Release key SHA-1 (add it to the Google Cloud Android OAuth client for app.mihon.sync):"
keytool -list -v -keystore "$keystore" -storepass "$password" -alias "$alias" | grep -E "SHA1:"
