# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# Keep line numbers and hide source file names for readable crash traces
# while still obfuscating. Helps triage any release-only issues.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── OkHttp / Okio ──────────────────────────────────────────────────────────
# OkHttp references optional TLS providers that aren't on Android; silence R8.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Coil (image loading) ───────────────────────────────────────────────────
-dontwarn coil.**

# ── Kotlin / coroutines ────────────────────────────────────────────────────
-dontwarn kotlinx.coroutines.**

# The app parses JSON manually via org.json (no reflection), so no model-class
# keep rules are required. Compose, OkHttp and Coil ship their own consumer
# rules; the above only suppresses benign missing-class warnings.