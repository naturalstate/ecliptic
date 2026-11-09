# Ecliptic — Play Store Launch Reference

Internal notes for the Play Console submission. Not shipped in the app.

## Privacy policy
- Hosted file: `privacy.html` (upload to the site, e.g. `https://ryand202.sg-host.com/ecliptic/privacy.html`).
- The in-app **About → Privacy Policy** link points to that URL — keep them in sync.
- Enter the same URL in Play Console → App content → Privacy policy.

## Data Safety form answers
Based on actual app behavior (no backend, no SDKs, no accounts).

- **Does your app collect or share any of the required user data types?** → **No.**
  - The app has no analytics/ads/crash SDKs and operates no server.
  - Scan history is stored only on-device and is not transmitted anywhere automatically.
  - The URL/domain the user types is sent to third-party lookup services (DNS, RDAP,
    ipinfo) to perform the scan. This is user-initiated functionality on data the user
    enters, not collection of the *user's* personal data. If the form pushes back,
    classify any such item as **not collected / processed ephemerally, not linked to a
    user, not shared for ads**.
- **Is all user data encrypted in transit?** → Yes (HTTPS; `usesCleartextTraffic=false`).
- **Do you provide a way to request data deletion?** → Uninstalling removes all local data; no server data exists.

## Permissions rationale (if asked)
- `INTERNET` — the only permission. Required to load and analyze the website the user scans.
- No location, contacts, camera, storage, or background permissions.

## Third-party endpoints contacted during a scan
Disclose in the privacy policy (already listed there):
`dns.google`, `rdap.net`, `rdap.iana.org`, `ipinfo.io`, `crt.sh` (subdomains),
`api.hackertarget.com` (reverse-IP), `urlhaus-api.abuse.ch` (reputation),
`cdn.jsdelivr.net` + `cdn.simpleicons.org` (icons), Google Fonts, Google Play
(in-app review), plus the scanned site itself.

## App content / review notes
- Core function loads arbitrary user-supplied sites in a WebView with JavaScript enabled
  and injects a read-only fingerprinting script. This is the intended behavior of a
  website technology scanner (comparable to Wappalyzer). No data exfiltration.
- Fingerprint data: `enthec/webappanalyzer` (open-source Wappalyzer DB) bundled as assets.
- Content rating questionnaire: utility/tool, no user-generated content shared between
  users, no ads → expect "Everyone".

## Release build checklist
1. `versionCode` = 22, `versionName` = "1.0.0" (bump both every upload; code must exceed
   any previously uploaded value).
2. Build a **signed release**: `.\gradlew assembleRelease` (or `bundleRelease` for an
   `.aab`, which Play prefers). Signing reads from `local.properties` + keystore.
3. **Test the release build on a device** — R8/minify is on. Exercise every tab (Tech,
   Intel sub-tabs incl. Spider, Code, Repeat), run a scan, export a report, open the
   Privacy link. Watch for release-only crashes.
4. Prefer uploading an **App Bundle (.aab)**: `.\gradlew bundleRelease`
   → `app/build/outputs/bundle/release/app-release.aab`.
5. Confirm the Privacy Policy URL resolves before submitting.

## Store listing todo (outside code)
- Screenshots (phone), feature graphic, short + full description.
- Keep "no telemetry / on-device" claim consistent with this policy.
