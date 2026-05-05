<div align="center">

<img src="assets/ecliptic-logo.png" alt="Ecliptic" width="120" />

# Ecliptic

**Website intelligence, in your pocket.**

The only active Android app for deep website tech-stack fingerprinting and HTTP recon — all on-device, no backend, no tracking.

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/min%20SDK-26%20(Android%208)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=for-the-badge)](CONTRIBUTING.md)

---

<img src="assets/screenshots/tech-results.png" alt="Ecliptic Tech Results Screen" width="280" />

</div>

---

## What is Ecliptic?

Ecliptic is a native Android app that gives you instant, deep intelligence on any website. Point it at a URL — or share directly from Chrome — and it fingerprints the entire tech stack using three independent analysis layers: live HTTP headers, HTML source analysis, and JavaScript runtime probing via a real Chromium engine (Android WebView).

Think of it as a pocket-sized combination of Wappalyzer, Burp Suite's repeater, and a DNS/WHOIS lookup tool — with everything running entirely on your device.

---

## Screenshots

<div align="center">

| Tech Detection | Security Headers | Network & DNS | HTTP Repeater |
|:-:|:-:|:-:|:-:|
| <img src="/screenshots/technologies_main_screen.jpg" width="180" /> | <img src="/screenshots/security_headers.jpg" width="180" /> | <img src="/screenshots/network_intel.jpg" width="180" /> | <img src="/screenshots/repeat_request_raw.jpg" width="180" /> |
| Detect 1,500+ technologies across 30+ categories | Full security header audit with live scoring | TLS certificate, DNS records, ASN lookup | Send custom raw or wizard-built HTTP requests |

</div>

---

## Features at a Glance

```
🔍  Technology fingerprinting    →  1,500+ technologies, 30+ categories
🛡️  Security header audit        →  10 headers scored, full values shown
🍪  Deep cookie inspection       →  HttpOnly, Secure, SameSite, expiry, domain
⚡  API detection                →  REST, GraphQL, SOAP, gRPC, JSON-RPC, OpenAPI
🌐  Network recon                →  TLS cert, DNS (A/CNAME/MX/NS/TXT), ASN
📋  WHOIS / RDAP                 →  Registrar, dates, name servers
🤖  Recon files                  →  robots.txt, security.txt, sitemap.xml
🔁  HTTP Repeater                →  RAW and wizard modes, fullscreen response
👁️  Source viewer                →  Syntax-highlighted HTML, JS globals, DOM tree
📤  Chrome Share integration     →  Share any URL from Chrome → instant scan
🕑  Scan history                 →  Persistent across sessions, ← → navigation
📋  One-tap copy                 →  Copy any tab's data to clipboard instantly
📊  HTML report export           →  Full scan exported as shareable HTML file
```

---

## Detection Capabilities

### 🔬 Technology Fingerprinting

Powered by the [enthec/webappanalyzer](https://github.com/enthec/webappanalyzer) signature database — the same engine behind Wappalyzer — running entirely on-device. Detection uses three simultaneous layers:

| Layer | What it checks |
|---|---|
| **HTTP Headers** | `X-Powered-By`, `Server`, `X-Generator`, `CF-Ray`, cache headers |
| **HTML Analysis** | Meta tags, script/CSS URLs, HTML attributes, body classes, title |
| **JS Runtime** | Live JavaScript global probing via injected scripts in Chromium WebView |

**Detected categories include:** CMS, JavaScript Frameworks, CSS Frameworks, UI Libraries, Analytics, Tag Managers, CDN, Web Servers, Databases, Programming Languages, Caching, Authentication, Ecommerce, Video, Mapping, Advertising, A/B Testing, and more.

**Smart grouping:** Major CMS platforms (WordPress, Shopify, Drupal, Joomla, CraftCMS, Magento, etc.) are sorted to the top of their category. Payment card brands (Visa, Mastercard, Amex, Discover, etc.) are grouped into a single "Major Credit Cards" entry rather than listed individually.

---

### 🛡️ Security Headers

Dedicated audit tab scoring 10 critical security headers:

| Header | What it protects |
|---|---|
| `Content-Security-Policy` | XSS, code injection |
| `Strict-Transport-Security` | Protocol downgrade attacks |
| `X-Frame-Options` | Clickjacking |
| `X-Content-Type-Options` | MIME-type sniffing |
| `Referrer-Policy` | Information leakage |
| `Permissions-Policy` | Browser feature access |
| `X-XSS-Protection` | Reflected XSS (legacy) |
| `Cross-Origin-Opener-Policy` | Cross-origin isolation |
| `Cross-Origin-Embedder-Policy` | Spectre mitigations |
| `Cross-Origin-Resource-Policy` | Cross-origin data leaks |

Present headers shown in full with their values. Missing headers flagged in red. Live score indicator (N/10).

---

### ⚡ API Detection

Automatic detection of API technologies from HTML source, HTTP headers, and script URLs:

- **GraphQL** — endpoint patterns, `__schema` introspection hints, Apollo client globals
- **OpenAPI / Swagger** — `/swagger-ui`, `/api-docs`, `swagger.json` patterns
- **REST** — conventional REST endpoint structures
- **SOAP** — WSDL references, `SOAPAction` headers, XML namespace patterns
- **gRPC** — `grpc-status`, `grpc-message`, `content-type: application/grpc` headers
- **JSON-RPC** — `application/json-rpc` content type, method/params payload patterns

Each detected API shows type, confidence percentage, evidence string, and endpoint if identifiable.

---

### 🌐 Network Tab

<div align="center">
<img src="assets/screenshots/network-dns.png" alt="Network Tab" width="280" />
</div>

- **TLS Certificate** — Protocol version, cipher suite, subject, issuer, validity period, Subject Alternative Names (SANs)
- **DNS Records** — Live lookup via Google DNS-over-HTTPS: A, CNAME, MX, NS, TXT records
- **ASN** — Autonomous System Number and organisation lookup (via ipinfo.io) from first A record
- **Social Links** — Extracted Twitter/X, Facebook, Instagram, LinkedIn, YouTube, GitHub, TikTok, Pinterest links from page source

---

### 🤖 Recon Tab

- **Wayback Machine** — Direct link to archive.org history for the scanned URL
- **WHOIS / RDAP** — Registrar, registrant, creation date, expiry date, last updated, domain status, name servers
- **robots.txt** — Fetched live, collapsible full view
- **security.txt** — Fetched from `/.well-known/security.txt` and `/security.txt`
- **sitemap.xml** — Fetched from `/sitemap.xml` and `/sitemap_index.xml`

---

### 🍪 Cookie Inspector

Full `Set-Cookie` header parsing — not just name/value:

| Attribute | Shown |
|---|---|
| Name & Value | ✅ |
| Domain | ✅ |
| Path | ✅ |
| Expires / Max-Age | ✅ |
| HttpOnly | ✅ flag chip |
| Secure | ✅ flag chip |
| SameSite | ✅ flag chip |

---

### 📋 Headers Tab

- **Request headers** — exactly what Ecliptic sent to the server
- **Response headers** — full server response headers
- **Security Headers** — dedicated sub-tab with score and full values (see above)

---

### 👁️ Code Tab

<div align="center">
<img src="assets/screenshots/source-viewer.png" alt="Source Viewer" width="280" />
</div>

- **HTML Source** — Syntax-highlighted HTML viewer with search, remote CSS/JS file loading, save to Downloads, fullscreen mode
- **JS Globals** — Every `window.*` global detected at runtime, including framework markers, config objects, feature flags
- **DOM Tree** — Collapsible interactive DOM tree with tag names, IDs, classes, attributes
- **Network Requests** — All requests captured during page load, filterable by type (script, XHR, image, font, media)
- **Console** — Live JS console with eval support — run JavaScript in the page context directly from the app

---

### 🕑 Scan History

Every scan is automatically saved to device storage and survives app restarts.

- **Persistent storage** — up to 50 scans saved as JSON files in internal storage (auto-pruned oldest)
- **← → navigation** — arrows appear in the header once you have multiple scans; tap to browse previous results
- **Full fidelity** — saved scans restore tech list, headers, cookies, TLS, APIs, and network requests
- **Save HTML source** — optional setting (off by default) to also persist raw HTML (~500KB extra per scan)

---

### 📋 One-Tap Copy & Export

- **Copy buttons** on every tab — Headers, Security Headers, Cookies, APIs, JS Globals, Network Requests, Console
- **Export report** — tap the share icon in the header after any scan to export a full styled HTML report covering all findings

---

### 🔁 HTTP Repeater

<div align="center">
<img src="assets/screenshots/repeater.png" alt="Repeater" width="280" />
</div>

Two modes for crafting and replaying HTTP requests:

**RAW mode**
- Editable raw HTTP request editor (method, path, headers, body)
- Full-screen request and response panes independently expandable
- Syntax-coloured response viewer

**WIZARD mode**
- Visual request builder: URL, method chips, header management, body editor
- Pre-populated from scan results — URL, method, and captured headers seeded automatically
- Collapsible summary card post-send with fullscreen response view
- Common headers quick-add chips (User-Agent, Accept, Content-Type, Authorization, etc.)

**REQ / RES Intel sub-tab**
- Split-screen read-only view of the original scan request and response
- Per-pane fullscreen expand
- Search/filter across all lines with highlight
- Scroll navigation (top, page up, page down, bottom)

---

## How the Scan Works

```
URL input / Chrome Share intent
         │
         ▼
┌─────────────────────┐
│  OkHttp request     │  ← Captures response headers, TLS handshake,
│  (network layer)    │    Set-Cookie, redirects
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Android WebView    │  ← Loads the real page in Chromium
│  (Chromium engine)  │    JS injection probes window globals,
│                     │    captures DOM, meta tags, script URLs
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Signal aggregation │  ← Merges headers + HTML + JS globals
│  + fingerprinting   │    Runs against webappanalyzer DB
└────────┬────────────┘
         │
         ▼
   Results displayed
   across Tech / Intel / Code / Repeat tabs
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + StateFlow |
| HTTP client | OkHttp |
| WebView engine | Android WebView (Chromium) |
| Fingerprint DB | enthec/webappanalyzer (bundled as assets) |
| Image loading | Coil |
| Min SDK | API 26 (Android 8.0) |
| No backend | Everything runs on-device |

---

## Installation

### Option A — Install APK directly

1. Download the latest APK from [Releases](https://github.com/naturalstate/ecliptic/releases)
2. Enable **Install from unknown sources** on your Android device
3. Install and open Ecliptic

### Option B — Build from source

```bash
git clone https://github.com/naturalstate/ecliptic.git
cd ecliptic

# Open in Android Studio, or build from CLI:
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk
```

Requires Android Studio Hedgehog or later, JDK 17+.

---

## Usage

**Basic scan**
1. Type or paste any URL into the input field
2. Tap **SCAN**
3. Browse results across the four tabs: **TECH**, **INTEL**, **CODE**, **REPEAT**

**Share from Chrome**
1. Open any website in Chrome
2. Tap Share → **Ecliptic**
3. App opens instantly and begins scanning

**Repeating a request**
1. After a scan, navigate to the **REPEAT** tab
2. Request is pre-populated from scan results
3. Edit headers, method, or body — tap **SEND**
4. View full response in the response pane

---

## Roadmap

- [x] Scan history — persistent across sessions with ← → navigation
- [x] Export scan report — full HTML report via share sheet
- [x] Copy to clipboard — one-tap copy on every tab
- [ ] Scan comparison — diff two saved scans side-by-side
- [ ] WebSocket and SSE detection (Network tab)
- [ ] HTTP/2 and HTTP/3 protocol detection
- [ ] Service Worker / PWA detection
- [ ] Build tool fingerprinting (Webpack, Vite, Parcel)
- [ ] WebAssembly detection
- [ ] Auth mechanism hints (JWT, OAuth, OIDC)
- [ ] Subresource Integrity audit
- [ ] cURL import in Repeater
- [ ] Play Store release

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first.

```bash
# Run a debug build before submitting
./gradlew assembleDebug
```

---

## License

[MIT](LICENSE) © Ryan Denham

---

<div align="center">

Built with ❤️ for developers and security researchers who live on their phones.

**[Report a Bug](https://github.com/naturalstate/ecliptic/issues) · [Request a Feature](https://github.com/naturalstate/ecliptic/issues) · [Releases](https://github.com/naturalstate/ecliptic/releases)**

</div>
