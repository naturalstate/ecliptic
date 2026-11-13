package com.ecliptic.poc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.composed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.res.Configuration
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import androidx.core.content.FileProvider
import com.google.android.play.core.review.ReviewManagerFactory
import com.ecliptic.AppPreferences
import com.ecliptic.DEFAULT_FONT_SCALE
import com.ecliptic.DEFAULT_SCAN_TIMEOUT_S
import com.ecliptic.DEFAULT_UA
import com.ecliptic.BuildConfig
import com.ecliptic.ScanViewModel
import com.ecliptic.fingerprint.Technology
import com.ecliptic.scanner.DnsResult
import com.ecliptic.scanner.ConsoleMessage
import com.ecliptic.scanner.CookieInfo
import com.ecliptic.scanner.DetectedApi
import com.ecliptic.scanner.NetworkRequest
import com.ecliptic.scanner.RawSignals
import com.ecliptic.scanner.RdapResult
import com.ecliptic.scanner.EnumTier
import com.ecliptic.scanner.PortResult
import com.ecliptic.scanner.ReconData
import com.ecliptic.scanner.SecurityAnalyzer
import com.ecliptic.scanner.StagingHost
import com.ecliptic.scanner.ReconFiles
import com.ecliptic.scanner.Extractor
import com.ecliptic.scanner.RepeaterResponse
import com.ecliptic.scanner.SpiderStatus
import com.ecliptic.scanner.WebViewClients
import com.ecliptic.scanner.fetchAsn
import com.ecliptic.scanner.fetchReconFiles
import com.ecliptic.scanner.fetchTextContent
import com.ecliptic.scanner.generateHtmlReport
import com.ecliptic.scanner.TlsResult
import org.json.JSONArray
import org.json.JSONObject
import com.ecliptic.scanner.configureWebView
import com.ecliptic.scanner.fetchDnsRecords
import com.ecliptic.scanner.fetchRdapInfo
import com.ecliptic.ui.DevIconMapper
import com.ecliptic.ui.theme.Blue
import com.ecliptic.ui.theme.Green
import com.ecliptic.ui.theme.Navy
import com.ecliptic.ui.theme.NavyLight
import com.ecliptic.ui.theme.Amber
import com.ecliptic.ui.theme.Red
import com.ecliptic.ui.theme.SpaceGrotesk
import com.ecliptic.ui.theme.Teal
import com.ecliptic.ui.theme.categoryAccentColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.ecliptic.R
import kotlinx.coroutines.withTimeoutOrNull

// Bottom nav indices
private const val TAB_TECH   = 0
private const val TAB_INTEL  = 1
private const val TAB_CODE   = 2
private const val TAB_REPEAT = 3

private val COMMON_HEADERS = listOf(
    "User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language" to "en-US,en;q=0.9",
    "Accept-Encoding" to "gzip, deflate, br",
    "Connection" to "keep-alive",
    "Cookie" to "",
    "Referer" to "",
    "Authorization" to "Bearer ",
    "Content-Type" to "application/json",
)

private fun categoryPriority(category: String): Int = when {
    category.contains("CMS", ignoreCase = true)              -> 0
    category.contains("WordPress Plugin", ignoreCase = true) -> 1
    category.contains("Blog", ignoreCase = true)             -> 2
    category.contains("E-commerce", ignoreCase = true)       -> 2
    category.contains("Web server", ignoreCase = true)       -> 3
    category.contains("Programming", ignoreCase = true)      -> 4
    category.contains("Database", ignoreCase = true)         -> 5
    category.contains("Web framework", ignoreCase = true)    -> 6
    category.contains("JavaScript", ignoreCase = true)       -> 7
    category.contains("Mobile", ignoreCase = true)           -> 8
    category.contains("Security", ignoreCase = true)         -> 9
    category.contains("CDN", ignoreCase = true)              -> 10
    category.contains("Hosting", ignoreCase = true)          -> 11
    category.contains("Operating system", ignoreCase = true) -> 12
    category.contains("Cache", ignoreCase = true)            -> 13
    category.contains("Font", ignoreCase = true)             -> 14
    category.contains("Payment", ignoreCase = true)          -> 15
    category.contains("Analytics", ignoreCase = true)        -> 16
    category.contains("Tag manager", ignoreCase = true)      -> 17
    category.contains("Marketing", ignoreCase = true)        -> 18
    category.contains("Live chat", ignoreCase = true)        -> 19
    category.contains("Support", ignoreCase = true)          -> 20
    category.contains("Advertising", ignoreCase = true)      -> 30
    else                                                      -> 25
}

private fun techNamePriority(name: String): Int = when (name.lowercase()) {
    "wordpress", "shopify", "drupal", "joomla", "magento", "woocommerce",
    "craftcms", "craft cms", "prestashop", "opencart", "squarespace", "wix",
    "webflow", "ghost", "typo3", "contao", "expressionengine" -> 0
    else -> 10
}

private val SECURITY_HEADERS = linkedMapOf(
    "content-security-policy"   to "CSP",
    "strict-transport-security" to "HSTS",
    "x-frame-options"           to "X-Frame-Options",
    "x-content-type-options"    to "X-Content-Type-Options",
    "referrer-policy"           to "Referrer-Policy",
    "permissions-policy"        to "Permissions-Policy",
    "cross-origin-opener-policy" to "COOP",
)

// Common admin/default paths per detected technology (path or :port[/path])
private val TECH_ADMIN_PATHS: Map<String, List<String>> = mapOf(
    // CMS
    "WordPress"      to listOf("/wp-admin", "/wp-login.php", "/wp-content/", "/xmlrpc.php"),
    "WooCommerce"    to listOf("/wp-admin", "/wp-admin/admin.php?page=wc-admin"),
    "Drupal"         to listOf("/admin", "/user/login", "/admin/config"),
    "Joomla"         to listOf("/administrator", "/administrator/index.php"),
    "Magento"        to listOf("/admin", "/index.php/admin"),
    "PrestaShop"     to listOf("/admin", "/adminXXXX"),
    "OpenCart"       to listOf("/admin", "/admin/index.php"),
    "TYPO3"          to listOf("/typo3", "/typo3/backend.php"),
    "Shopify"        to listOf("/admin", "/admin/login"),
    "Wix"            to listOf("/dashboard"),
    "Squarespace"    to listOf("/config"),
    "Ghost"          to listOf("/ghost", "/ghost/signin"),
    "Webflow"        to listOf("/admin"),
    "Craft CMS"      to listOf("/admin", "/index.php?p=admin"),
    "CraftCMS"       to listOf("/admin", "/index.php?p=admin"),
    "Contao"         to listOf("/contao", "/contao/login"),
    // Database / admin panels
    "MySQL"          to listOf("/phpmyadmin", "/pma", "/mysql"),
    "phpMyAdmin"     to listOf("/phpmyadmin", "/pma", "/phpmyadmin/index.php"),
    "PostgreSQL"     to listOf("/phppgadmin", ":5432"),
    "MongoDB"        to listOf(":27017", ":28017"),
    "Redis"          to listOf(":6379"),
    "Elasticsearch"  to listOf(":9200", ":9200/_cat/indices", ":9200/_cluster/health"),
    "Kibana"         to listOf(":5601", ":5601/app/kibana"),
    "Grafana"        to listOf(":3000", ":3000/login"),
    "InfluxDB"       to listOf(":8086", ":8086/query"),
    "Adminer"        to listOf("/adminer", "/adminer.php", "/database.php"),
    // Web server status
    "Apache"         to listOf("/server-status", "/server-info"),
    "Nginx"          to listOf("/nginx_status"),
    // Hosting control panels
    "cPanel"         to listOf(":2082", ":2083", "/cpanel"),
    "Plesk"          to listOf(":8443", ":8880", "/login_up.php3"),
    "WHM"            to listOf(":2086", ":2087"),
    "DirectAdmin"    to listOf(":2222", "/CMD_LOGIN"),
    "Webmin"         to listOf(":10000"),
    "ISPConfig"      to listOf(":8080", ":8080/index.php"),
    // CI / Dev tools
    "Jenkins"        to listOf(":8080", ":8080/login", "/jenkins"),
    "GitLab"         to listOf("/users/sign_in", "/admin"),
    "Gitea"          to listOf(":3000", "/user/login"),
    "SonarQube"      to listOf(":9000", ":9000/sessions/new"),
    "RabbitMQ"       to listOf(":15672", ":15672/#/login"),
    "Portainer"      to listOf(":9000", ":9443"),
    "Traefik"        to listOf(":8080/dashboard", ":8080/api/rawdata"),
    // PHP
    "PHP"            to listOf("/phpmyadmin", "/info.php", "/phpinfo.php", "/test.php"),
    "Laravel"        to listOf("/admin", "/_debugbar", "/horizon", "/telescope"),
    "Symfony"        to listOf("/_profiler", "/app_dev.php"),
    "CodeIgniter"    to listOf("/admin"),
    // Python / Node / Java
    "Django"         to listOf("/admin", "/admin/login"),
    "Flask"          to listOf("/admin"),
    "Spring"         to listOf("/actuator", "/actuator/health", "/actuator/env"),
    "Spring Boot"    to listOf("/actuator", "/actuator/health", "/actuator/env"),
    "Express"        to listOf("/admin"),
    "Node.js"        to listOf(":3000", ":8000"),
    // Monitoring
    "Prometheus"     to listOf(":9090", ":9090/graph"),
    "Grafana"        to listOf(":3000", ":3000/login"),
    // Forums / CRM
    "phpBB"          to listOf("/admin/index.php", "/adm/"),
    "Discourse"      to listOf("/admin"),
    "vBulletin"      to listOf("/admincp"),
    "SMF"            to listOf("/admin.php"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocScanScreen(
    initialUrl: String? = null,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    viewModel: ScanViewModel = viewModel(),
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val state by viewModel.state.collectAsStateWithLifecycle()
    val urlInput = state.urlInput
    val scanning = state.isScanning
    val hasScanned = state.hasScanned
    val signals = state.signals
    val techs = state.techs
    val httpMethod = state.httpMethod
    val customHeaders = state.customHeaders
    val requestBody = state.requestBody
    val rawRequest = state.rawRequest
    val isRepeaterWizard = state.isRepeaterWizard
    val scanStage = state.scanStage
    val repeaterResponse = state.repeaterResponse
    val isSendingRequest = state.isSendingRequest
    val wizardMethod = state.wizardMethod
    val wizardUrl = state.wizardUrl
    val wizardHeaders = state.wizardHeaders
    val wizardBody = state.wizardBody
    val wizardSent = state.wizardSent
    val liveNetworkRequests = state.liveNetworkRequests
    val consoleMessages = state.consoleMessages
    val scanCount = state.scanCount
    val historySize = state.historySize
    val historyPosition = state.historyPosition

    var bottomTab    by remember { mutableIntStateOf(TAB_TECH) }
    var intelSubTab  by remember { mutableIntStateOf(0) }   // 0=HEADERS … 6=REQ/RES
    var codeSubTab   by remember { mutableIntStateOf(0) }   // 0=SOURCE 1=JS 2=DOM 3=NET 4=CONSOLE
    var showSettings by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    // Deep-scan JS results for the Strings tab, hoisted here (keyed per scan) so they
    // survive switching between Code subtabs, which disposes the subtab content.
    var stringsDeepText by remember(signals?.finalUrl) { mutableStateOf("") }
    // Seeded into the Source viewer's filter when "Find in source" is tapped on a Strings finding.
    var pendingSourceQuery by remember(signals?.finalUrl) { mutableStateOf("") }
    var selectedTech by remember { mutableStateOf<Technology?>(null) }
    var isFullScreen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val webView = remember { WebView(context).also { configureWebView(it) } }
    DisposableEffect(Unit) { onDispose { webView.destroy() } }

    LaunchedEffect(initialUrl) {
        if (initialUrl != null) viewModel.onUrlChanged(initialUrl)
    }
    LaunchedEffect(scanning) {
        if (!scanning && hasScanned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (bottomTab == TAB_REPEAT) bottomTab = TAB_TECH
            // Hand off live capture callbacks to ViewModel after scan completes
            val clients = webView.tag as? WebViewClients
            clients?.viewClient?.onRequest  = { req -> viewModel.appendNetworkRequest(req) }
            clients?.chromeClient?.onConsole = { msg -> viewModel.appendConsoleMessage(msg) }
        }
        if (scanning) {
            // Clear live callbacks while scan is running (scan captures into its own local list)
            val clients = webView.tag as? WebViewClients
            clients?.viewClient?.onRequest  = null
            clients?.chromeClient?.onConsole = null
        }
    }

    fun scan() {
        if (urlInput.isBlank()) return
        bottomTab = TAB_TECH
        keyboard?.hide()
        viewModel.scan(webView)
    }

    fun paste() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.let { viewModel.onUrlChanged(it) }
    }

    fun exportReport(includeFullHtml: Boolean = false, includeDomTree: Boolean = false, darkReport: Boolean = false) {
        val sig = signals ?: return
        val html = generateHtmlReport(sig, techs, BuildConfig.VERSION_NAME, includeFullHtml, includeDomTree, darkReport, recon = state.reconData)
        try {
            val dir = java.io.File(context.cacheDir, "reports").also { it.mkdirs() }
            val file = java.io.File(dir, "ecliptic_report_${System.currentTimeMillis()}.html")
            file.writeText(html)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ecliptic report — ${sig.finalUrl}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export report"))
        } catch (e: Exception) {
            // Fallback: share as plain text
            val sb = StringBuilder()
            sb.appendLine("Ecliptic scan: ${sig.finalUrl}")
            sb.appendLine("${techs.size} technologies detected")
            sb.appendLine()
            techs.groupBy { it.categories.firstOrNull() ?: "Other" }.toSortedMap()
                .forEach { (cat, list) ->
                    sb.appendLine(cat)
                    list.sortedBy { it.name }.forEach { t ->
                        sb.appendLine("  • ${t.name}${t.version?.let { " $it" } ?: ""}")
                    }
                }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
            }
            context.startActivity(Intent.createChooser(intent, "Share scan results"))
        }
    }

    // In-app review trigger — after 3rd and 30th successful scan
    LaunchedEffect(scanCount) {
        if (scanCount == 3 || scanCount == 30) {
            val activity = context as? Activity ?: return@LaunchedEffect
            val manager = ReviewManagerFactory.create(context)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) manager.launchReviewFlow(activity, task.result)
            }
        }
    }

    // Content key for AnimatedContent
    val contentKey = when (bottomTab) {
        TAB_REPEAT -> "repeat"
        TAB_TECH   -> when {
            scanning && !hasScanned -> "loading"
            !hasScanned             -> "empty"
            else                    -> "tech"
        }
        TAB_INTEL  -> if (!hasScanned) "empty" else "intel_$intelSubTab"
        TAB_CODE   -> if (!hasScanned) "empty" else "code_$codeSubTab"
        else       -> "empty"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().height(1.dp))

        // ── Compact header (hidden in fullscreen or landscape) ────────────────
        if (!isFullScreen && !isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 4.dp, top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // weight(1f) bounds the left block so a long host clips instead of
                // pushing the history arrows/actions on the right out of position.
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo_mark),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = "ECLIPTIC",
                            maxLines = 1,
                            softWrap = false,   // never wrap the wordmark, even when the action row is crowded
                            style = TextStyle(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                letterSpacing = 0.18.em,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    }
                    // Pinned project host — shown here on Intel/Code/Repeat (where there's
                    // no scan bar). On the Tech page the host lives in its own row below the
                    // scan bar, next to the project actions, so the header stays uncluttered.
                    if (hasScanned && bottomTab != TAB_TECH) {
                        val projectHost = remember(signals?.finalUrl, urlInput) {
                            val raw = signals?.finalUrl?.takeIf { it.isNotBlank() } ?: urlInput
                            runCatching {
                                val withScheme = if (raw.startsWith("http")) raw else "https://$raw"
                                java.net.URI(withScheme).host.orEmpty()
                            }.getOrDefault("")
                        }
                        if (projectHost.isNotBlank()) {
                            Text(
                                text = projectHost,
                                color = Teal,
                                fontSize = 11.sp,
                                fontFamily = SpaceGrotesk,
                                letterSpacing = 0.02.em,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 14.dp),
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (historySize > 1) {
                        // Tap to step one scan; hold to fast-advance through history.
                        HistoryArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous scan", historyPosition > 1) {
                            viewModel.navigateHistory(-1)
                        }
                        Text(
                            text = "$historyPosition / $historySize",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontFamily = SpaceGrotesk,
                        )
                        HistoryArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next scan", historyPosition < historySize) {
                            viewModel.navigateHistory(1)
                        }
                    }
                    // App-general actions only up here (New scan, Settings). The
                    // project-specific Share / Delete live in the project row on the Tech page.
                    if (hasScanned && !scanning) {
                        IconButton(onClick = { viewModel.onNewScan(); bottomTab = TAB_TECH }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "New scan", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { showSettings = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // ── Scan bar — morphs through edit → scanning → done ──────────────────
        if (!isFullScreen && (bottomTab == TAB_TECH || !hasScanned)) {
            // editingUrl lets the user reopen the input after a scan. Key it on the
            // history position (not the URL string) so moving to another scan — even a
            // re-scan of the SAME site — always drops out of edit mode; otherwise same-URL
            // snapshots share the key and the bar stays stuck open, blocking navigation.
            var editingUrl by remember(historyPosition, hasScanned) { mutableStateOf(false) }
            val barState = when {
                scanning -> "scanning"
                hasScanned && !editingUrl -> "done"
                else -> "edit"
            }
            AnimatedContent(
                targetState = barState,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(140)) },
                label = "scanbar",
                modifier = Modifier.fillMaxWidth().padding(
                    start = 16.dp, end = 16.dp,
                    top = if (isLandscape) 6.dp else 8.dp,
                    bottom = if (isLandscape) 4.dp else 0.dp,
                ),
            ) { s ->
                when (s) {
                    "scanning" -> ScanningBar(stage = scanStage)
                    "done" -> ScannedBar(
                        statusCode = signals?.headers?.get("_status"),
                        techCount = techs.size,
                        onEdit = { editingUrl = true },
                    )
                    else -> ScanInputBar(
                        value = urlInput,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        onClear = { viewModel.onUrlChanged(""); if (hasScanned) { viewModel.onNewScan(); bottomTab = TAB_TECH } },
                        onPaste = { paste() },
                        onScan = { editingUrl = false; scan() },
                    )
                }
            }
        }

        // ── Project row (Tech page): host + project-specific actions ──────────
        // Share and Delete act on the current scan, so they live with the host here
        // rather than crowding the app header.
        if (!isFullScreen && hasScanned && !scanning && bottomTab == TAB_TECH) {
            val projectHost = remember(signals?.finalUrl, urlInput) {
                val raw = signals?.finalUrl?.takeIf { it.isNotBlank() } ?: urlInput
                runCatching {
                    val withScheme = if (raw.startsWith("http")) raw else "https://$raw"
                    java.net.URI(withScheme).host.orEmpty()
                }.getOrDefault("")
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = projectHost.ifBlank { urlInput },
                    color = Teal,
                    fontSize = 13.sp,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.02.em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showShareSheet = true; viewModel.loadRecon() }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.Share, "Export report", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.Delete, "Delete this scan", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ── Sub-tab chips (INTEL / CODE only, hidden in fullscreen) ──────────
        if (!isFullScreen && hasScanned && !scanning && (bottomTab == TAB_INTEL || bottomTab == TAB_CODE)) {
            val subLabels = if (bottomTab == TAB_INTEL)
                listOf("HEADERS", "SEC HEADERS", "COOKIES", "APIS", "RECON", "PARAM", "SPIDER", "NETWORK", "REQ/RES")
            else listOf("SOURCE", "STRINGS", "JS", "DOM", "NET", "CONSOLE")
            val currentSub = if (bottomTab == TAB_INTEL) intelSubTab else codeSubTab
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(subLabels) { i, label ->
                    val sel = currentSub == i
                    Surface(
                        onClick = { if (bottomTab == TAB_INTEL) intelSubTab = i else codeSubTab = i },
                        shape = RoundedCornerShape(20.dp),
                        color = if (sel) Teal.copy(alpha = 0.12f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (sel) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (sel) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = SpaceGrotesk,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.07.em,
                        )
                    }
                }
            }
        } else if (!isFullScreen) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Content area ──────────────────────────────────────────────────────
        val canExpand = bottomTab == TAB_REPEAT || (bottomTab == TAB_CODE && codeSubTab != 1)
        PullToRefreshBox(
            isRefreshing = scanning && hasScanned && bottomTab != TAB_REPEAT,
            onRefresh = { if (!scanning && bottomTab != TAB_REPEAT) scan() },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = contentKey,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                modifier = Modifier.fillMaxSize(),
                label = "content",
            ) { key ->
                when (key) {
                    "repeat"  -> RepeaterContent(
                        rawRequest = rawRequest,
                        isRepeaterWizard = isRepeaterWizard,
                        urlInput = urlInput,
                        repeaterResponse = repeaterResponse,
                        isSendingRequest = isSendingRequest,
                        wizardMethod = wizardMethod,
                        wizardUrl = wizardUrl,
                        wizardHeaders = wizardHeaders,
                        wizardBody = wizardBody,
                        wizardSent = wizardSent,
                        isFullScreen = isFullScreen,
                        buildTemplate = { viewModel.buildTemplateForUrl(urlInput, httpMethod) },
                        onRawRequestChanged = { viewModel.onRawRequestChanged(it) },
                        onToggleRepeaterMode = { viewModel.onToggleRepeaterMode() },
                        onSendRaw = { viewModel.sendRepeaterRequest(it) },
                        onSendParts = { m, u, h, b -> viewModel.sendRepeaterRequestFromParts(m, u, h, b) },
                        onRepeaterReset = { viewModel.onRepeaterReset() },
                        onWizardMethodChanged = { viewModel.onWizardMethodChanged(it) },
                        onWizardUrlChanged = { viewModel.onWizardUrlChanged(it) },
                        onWizardHeaderAdd = { k, v -> viewModel.onWizardHeaderAdd(k, v) },
                        onWizardHeaderRemove = { viewModel.onWizardHeaderRemove(it) },
                        onWizardHeaderValueChanged = { i, v -> viewModel.onWizardHeaderValueChanged(i, v) },
                        onWizardBodyChanged = { viewModel.onWizardBodyChanged(it) },
                        onWizardSentChanged = { viewModel.onWizardSentChanged(it) },
                        onToggleFullScreen = { isFullScreen = !isFullScreen },
                    )
                    "loading" -> LoadingState(stage = scanStage)
                    "empty"   -> EmptyState()
                    // Wrapped in SelectionContainer so any value can be long-pressed
                    // and copied natively (no per-field copy icons). Panes that already
                    // provide selection (Security, Cookies, Recon, Req/Res) or have a
                    // dedicated copy button for a large block (Source, DOM) are left as-is.
                    "tech"    -> SelectionContainer { TechResultsContent(techs = techs, error = signals?.error, url = signals?.finalUrl ?: "", onTechClick = { selectedTech = it }) }
                    "intel_0" -> SelectionContainer { HeadersContent(requestHeaders = signals?.requestHeaders ?: emptyMap(), responseHeaders = signals?.headers ?: emptyMap()) }
                    "intel_1" -> SecurityHeadersContent(signals = signals, responseHeaders = signals?.headers ?: emptyMap())
                    "intel_2" -> CookiesContent(richCookies = signals?.richCookies ?: emptyList(), cookies = signals?.cookies ?: emptyMap())
                    "intel_3" -> SelectionContainer { ApisContent(apis = signals?.detectedApis ?: emptyList()) }
                    "intel_4" -> ReconContent(
                        url = signals?.finalUrl ?: urlInput,
                        data = state.reconData,
                        loading = state.reconLoading,
                        onLoad = { viewModel.loadRecon() },
                        onRefresh = { viewModel.loadRecon(force = true) },
                        portsLoading = state.portsLoading,
                        stagingLoading = state.stagingLoading,
                        subsLoading = state.subsLoading,
                        buildOriginLoading = state.buildOriginLoading,
                        onScanPorts = { viewModel.scanPorts() },
                        onEnumStaging = { tier -> viewModel.enumStaging(tier) },
                        onFindSubdomains = { viewModel.findSubdomains() },
                        onAnalyzeBuildOrigin = { viewModel.analyzeBuildOrigin() },
                    )
                    "intel_5" -> SelectionContainer { ParamContent(html = signals?.htmlSource ?: "") }
                    "intel_6" -> SpiderContent(viewModel = viewModel, url = signals?.finalUrl ?: urlInput, onSendToRepeater = { viewModel.prefillRepeater(it); bottomTab = TAB_REPEAT })
                    "intel_7" -> SelectionContainer { NetworkContent(url = signals?.finalUrl ?: "", html = signals?.htmlSource ?: "", tls = signals?.tls, dns = state.reconData?.dns, asn = state.reconData?.asn, loading = state.reconLoading, onLoad = { viewModel.loadRecon() }) }
                    "intel_8" -> ReqResContent(rawRequest = rawRequest, signals = signals, isFullScreen = isFullScreen, onToggleFullScreen = { isFullScreen = !isFullScreen })
                    "code_0"  -> SourceContent(html = signals?.htmlSource ?: "", scriptUrls = signals?.scriptUrls ?: emptyList(), cssUrls = signals?.cssUrls ?: emptyList(), initialQuery = pendingSourceQuery, isFullScreen = isFullScreen, onToggleFullScreen = { isFullScreen = !isFullScreen })
                    "code_1"  -> SelectionContainer { StringsContent(html = signals?.htmlSource ?: "", scriptUrls = signals?.scriptUrls ?: emptyList(), deepText = stringsDeepText, onDeepText = { stringsDeepText = it }, onOpenInSource = { pendingSourceQuery = it; codeSubTab = 0 }) }
                    "code_2"  -> SelectionContainer { JsGlobalsContent(globals = signals?.globals ?: emptyMap(), jsProbe = signals?.jsProbe ?: emptyMap()) }
                    "code_3"  -> DomContent(domTreeJson = signals?.domTree ?: "", isFullScreen = isFullScreen, onToggleFullScreen = { isFullScreen = !isFullScreen })
                    "code_4"  -> SelectionContainer { NetContent(scanRequests = signals?.networkRequests ?: emptyList(), liveRequests = liveNetworkRequests) }
                    "code_5"  -> ConsoleContent(messages = consoleMessages, evalJs = { cmd, cb -> webView.evaluateJavascript(cmd, cb) })
                    else      -> Box(modifier = Modifier.fillMaxSize())
                }
            }
        }

        // ── Bottom navigation (hidden in fullscreen) ──────────────────────────
        if (!isFullScreen) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
            ) {
                listOf(
                    Triple(TAB_TECH,   Icons.Filled.Layers,   "Tech"),
                    Triple(TAB_INTEL,  Icons.AutoMirrored.Filled.ManageSearch, "Intel"),
                    Triple(TAB_CODE,   Icons.Filled.Code,     "Code"),
                    Triple(TAB_REPEAT, Icons.AutoMirrored.Filled.Send, "Repeat"),
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = bottomTab == idx,
                        onClick = { bottomTab = idx },
                        icon = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                        label = {
                            Text(label, fontFamily = SpaceGrotesk, fontSize = 10.sp, fontWeight = if (bottomTab == idx) FontWeight.SemiBold else FontWeight.Normal)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Teal,
                            selectedTextColor = Teal,
                            indicatorColor = Teal.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        val delHost = remember(signals?.finalUrl, urlInput) {
            val raw = signals?.finalUrl?.takeIf { it.isNotBlank() } ?: urlInput
            runCatching { java.net.URI(if (raw.startsWith("http")) raw else "https://$raw").host.orEmpty() }.getOrDefault(raw)
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.Delete, null, tint = Red) },
            title = { Text("Delete this scan?", fontFamily = SpaceGrotesk) },
            text = {
                Text(
                    "Permanently removes the saved scan for ${delHost.ifBlank { "this site" }} from this device. This can't be undone.",
                    fontSize = 13.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.deleteCurrentScan() }) {
                    Text("Delete", color = Red, fontFamily = SpaceGrotesk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(),
        ) {
            SettingsSheet(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
        }
    }

    if (showShareSheet) {
        var incHtml by remember { mutableStateOf(false) }
        var incDom by remember { mutableStateOf(false) }
        var incDark by remember { mutableStateOf(false) }
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Export report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "A full, white-themed HTML report of this scan. Open it in Chrome to read, print, or save as PDF.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp,
                )
                @Composable
                fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = checked, onCheckedChange = onChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Navy, checkedTrackColor = Teal,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                    }
                }
                ToggleRow("Dark mode report", incDark) { incDark = it }
                ToggleRow("Include full HTML source", incHtml) { incHtml = it }
                ToggleRow("Include DOM tree", incDom) { incDom = it }
                Text(
                    "Technologies, APIs, headers, cookies, TLS, network, parameters, strings and social links are always included.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp,
                )
                Button(
                    onClick = { showShareSheet = false; exportReport(incHtml, incDom, incDark) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Navy),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate & Share", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    selectedTech?.let { tech ->
        TechDetailSheet(tech = tech, scanUrl = signals?.finalUrl ?: urlInput, onDismiss = { selectedTech = null })
    }
}

// ─── Loading / empty ──────────────────────────────────────────────────────────

@Composable
private fun LoadingState(stage: String = "") {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "alpha",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (stage.isNotBlank()) stage else "Scanning…",
            color = Teal,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        repeat(7) { SkeletonCard(alpha = alpha) }
    }
}

@Composable
private fun SkeletonCard(alpha: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.35f)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.28f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.28f)
                        .height(9.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.18f)),
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Scan any website",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Enter a URL above and tap SCAN to detect technologies",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
        }
    }
}

// ─── Tech results ─────────────────────────────────────────────────────────────

@Composable
private fun TechResultsContent(
    url: String,
    techs: List<Technology>,
    error: String?,
    onTechClick: (Technology) -> Unit,
) {
    val grouped = techs
        .groupBy { it.categories.firstOrNull() ?: "Other" }
        .entries
        .sortedWith(compareBy({ categoryPriority(it.key) }, { it.key }))
    val iconTint = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        error?.let {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "⚠  $it",
                        modifier = Modifier.padding(12.dp),
                        color = Red,
                        fontSize = 13.sp,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item {
            val uriHandler = LocalUriHandler.current
            // (URL shown once in the pinned header + the morphing scan bar; no repeat here.)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${techs.size} technologies detected",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = {
                        val sb = buildString {
                            techs.groupBy { it.categories.firstOrNull() ?: "Other" }
                                .toSortedMap()
                                .forEach { (cat, list) ->
                                    appendLine(cat)
                                    list.sortedBy { it.name }.forEach { t ->
                                        appendLine("  • ${t.name}${t.version?.let { " $it" } ?: ""}")
                                    }
                                    appendLine()
                                }
                        }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("techs", sb.trim()))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        grouped.forEach { (category, list) ->
            item { CategoryHeader(category = category, count = list.size) }
            items(list.sortedWith(compareBy({ techNamePriority(it.name) }, { it.name }))) { tech ->
                TechRow(tech = tech, iconTint = iconTint, onClick = { onTechClick(tech) })
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun CategoryHeader(category: String, count: Int) {
    val color = categoryAccentColor(category) ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = category.uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.em,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.25f), thickness = 1.dp)
        Text(text = "$count", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun TechRow(
    tech: Technology,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val accentColor = categoryAccentColor(tech.categories.firstOrNull() ?: "") ?: Teal
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .background(accentColor),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TechIcon(techName = tech.name, tint = iconTint, modifier = Modifier.size(24.dp))
                Text(
                    text = tech.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    tech.version?.let {
                        Text(text = it, color = accentColor, fontSize = 12.sp)
                    }
                    if (tech.confidence < 100) {
                        Surface(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = "${tech.confidence}%",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Tech detail bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TechDetailSheet(tech: Technology, scanUrl: String = "", onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    val accentColor = categoryAccentColor(tech.categories.firstOrNull() ?: "") ?: Teal
    val iconTint = MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
      SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Icon + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TechIcon(techName = tech.name, tint = iconTint, modifier = Modifier.size(44.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tech.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    tech.version?.let {
                        Text(text = "Version $it", fontSize = 13.sp, color = accentColor)
                    }
                }
            }

            // Category chips
            if (tech.categories.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tech.categories.take(3).forEach { cat ->
                        val catColor = categoryAccentColor(cat) ?: MaterialTheme.colorScheme.onSurfaceVariant
                        Surface(color = catColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = cat,
                                color = catColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Confidence
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Confidence", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(
                        text = if (tech.confidence >= 100) "Direct match" else "Implied  ${tech.confidence}%",
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                LinearProgressIndicator(
                    progress = { tech.confidence / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
            }

            // Detection info
            tech.detectionMethod?.let { method ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = method.uppercase(),
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.em,
                        )
                        tech.matchedValue?.let { value ->
                            Text(
                                text = value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }

            // Website button
            tech.website?.takeIf { it.isNotBlank() }?.let { url ->
                Button(
                    onClick = { runCatching { uriHandler.openUri(url) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = url.removePrefix("https://").removePrefix("http://").trimEnd('/'),
                        fontSize = 13.sp,
                    )
                }
            }

            // Common admin/default paths
            val adminPaths = TECH_ADMIN_PATHS[tech.name] ?: emptyList()
            if (adminPaths.isNotEmpty() && scanUrl.isNotBlank()) {
                val baseOrigin = remember(scanUrl) {
                    try {
                        val uri = java.net.URI(scanUrl)
                        "${uri.scheme}://${uri.host}"
                    } catch (_: Exception) { null }
                }
                if (baseOrigin != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Text(
                        "COMMON PATHS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.em,
                        color = accentColor,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        adminPaths.forEach { path ->
                            val fullUrl = "$baseOrigin$path"
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri(fullUrl) } },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        path,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(Icons.Filled.OpenInBrowser, null, tint = accentColor, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
      }
    }
}

// ─── History arrow (tap once, or hold to repeat) ───────────────────────────────

/**
 * Fires [onClick] once on press, then repeatedly while held after a short delay.
 *
 * The repeat loop runs inside the gesture's own coroutine (not an external scope), so it
 * is cancelled the instant the pointer is released OR the gesture is torn down — e.g. when
 * [enabled] flips as the arrow hits a history boundary. A previous version launched the
 * loop on rememberCoroutineScope(); when enabled changed mid-hold, tryAwaitRelease() was
 * abandoned, the loop leaked, and it kept stepping history forever (arrows got "stuck").
 */
private fun Modifier.repeatingClickable(
    enabled: Boolean,
    initialDelayMs: Long = 400,
    repeatMs: Long = 90,
    onClick: () -> Unit,
): Modifier = composed {
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(onPress = {
            onClick()
            // Still held after the initial delay? Repeat until released or cancelled.
            // tryAwaitRelease() returns on release; withTimeoutOrNull returns null on hold.
            var done = withTimeoutOrNull(initialDelayMs) { tryAwaitRelease() } != null
            while (!done) {
                onClick()
                done = withTimeoutOrNull(repeatMs) { tryAwaitRelease() } != null
            }
        })
    }
}

@Composable
private fun HistoryArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onStep: () -> Unit,
) {
    Box(
        modifier = Modifier.size(36.dp).repeatingClickable(enabled) { onStep() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, contentDescription,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── Scan bar states (edit → scanning → done) ──────────────────────────────────

@Composable
private fun ScanInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onPaste: () -> Unit,
    onScan: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text("example.com", fontFamily = SpaceGrotesk, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(Teal),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { onScan() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    }
                }
                IconButton(onClick = onPaste, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.ContentPaste, "Paste", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
        Button(
            onClick = onScan,
            colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Navy),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            modifier = Modifier.height(52.dp),
        ) {
            Text("SCAN", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ScanningBar(stage: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        color = Teal.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Teal.copy(alpha = 0.5f)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Teal, strokeWidth = 2.dp)
                Text(stage.ifBlank { "Scanning…" }, color = Teal, fontFamily = SpaceGrotesk, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            // Indeterminate scanline along the bottom edge.
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter),
                color = Teal,
                trackColor = Teal.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun ScannedBar(statusCode: String?, techCount: Int, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onEdit() },
        shape = RoundedCornerShape(10.dp),
        color = Teal.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Teal.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Check, null, tint = Teal, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                buildString {
                    append("Scan complete")
                    statusCode?.takeIf { it.isNotBlank() && it != "—" }?.let { append(" · HTTP $it") }
                    append(" · $techCount ${if (techCount == 1) "tech" else "techs"}")
                },
                color = Teal, fontFamily = SpaceGrotesk, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.Edit, "Edit URL", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("Edit", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = SpaceGrotesk, fontSize = 11.sp)
        }
    }
}

// ─── Tech icon ────────────────────────────────────────────────────────────────

@Composable
private fun TechIcon(
    techName: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val iconUrl = remember(techName) { DevIconMapper.iconUrlFor(techName) }
    var showFallback by remember(iconUrl) { mutableStateOf(iconUrl == null) }

    if (showFallback) {
        TechIconFallback(techName = techName, tint = tint, modifier = modifier)
    } else {
        AsyncImage(
            model = iconUrl!!,
            contentDescription = null,
            modifier = modifier,
            colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
            onError = { showFallback = true },
        )
    }
}

@Composable
private fun TechIconFallback(
    techName: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(tint.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = techName.firstOrNull()?.uppercase() ?: "?",
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─── Tab copy button helper ───────────────────────────────────────────────────

@Composable
private fun TabCopyButton(modifier: Modifier = Modifier, buildText: () -> String) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("ecliptic", buildText()))
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier.size(32.dp),
    ) {
        Icon(Icons.Filled.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
    }
}

// ─── Headers tab ──────────────────────────────────────────────────────────────

@Composable
private fun HeadersContent(
    requestHeaders: Map<String, String>,
    responseHeaders: Map<String, String>,
) {
    val status = responseHeaders["_status"]
    val resHeaders = responseHeaders.filterKeys { it != "_status" && it != "_header_error" }
    val headerError = responseHeaders["_header_error"]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TabCopyButton {
                    buildString {
                        if (resHeaders.isNotEmpty()) {
                            appendLine("=== RESPONSE HEADERS ===")
                            resHeaders.entries.sortedBy { it.key }.forEach { (k, v) -> appendLine("$k: $v") }
                        }
                        if (requestHeaders.isNotEmpty()) {
                            appendLine()
                            appendLine("=== REQUEST HEADERS ===")
                            requestHeaders.entries.sortedBy { it.key }.forEach { (k, v) -> appendLine("$k: $v") }
                        }
                    }.trim()
                }
            }
        }
        headerError?.let {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = Red.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text("⚠  $it", modifier = Modifier.padding(12.dp), color = Red, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        item {
            HeaderSectionTitle(
                title = "RESPONSE HEADERS",
                badge = status,
                badgeColor = when {
                    status == null        -> MaterialTheme.colorScheme.onSurfaceVariant
                    status.startsWith("2") -> Teal
                    status.startsWith("3") -> Blue
                    else                   -> Red
                },
            )
        }
        items(resHeaders.entries.toList().sortedBy { it.key }) { (key, value) ->
            HeaderRow(name = key, value = value)
        }
        if (requestHeaders.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HeaderSectionTitle(title = "REQUEST HEADERS", badge = null, badgeColor = Teal)
            }
            items(requestHeaders.entries.toList().sortedBy { it.key }) { (key, value) ->
                HeaderRow(name = key, value = value)
            }
        }
    }
}

@Composable
private fun SecurityHeadersSection(responseHeaders: Map<String, String>) {
    val presentCount = SECURITY_HEADERS.keys.count { it in responseHeaders }
    val totalCount = SECURITY_HEADERS.size

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "SECURITY",
                color = if (presentCount == totalCount) Green else Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.em,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = (if (presentCount == totalCount) Green else Red).copy(alpha = 0.25f),
            )
            Text(
                text = "$presentCount / $totalCount",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(10.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SECURITY_HEADERS.entries.forEachIndexed { i, (key, label) ->
                    val present = key in responseHeaders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (present) "✓" else "✗",
                            color = if (present) Green else Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp),
                        )
                        Text(
                            text = label,
                            color = if (present) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (!present) {
                            Surface(
                                color = Red.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = "MISSING",
                                    color = Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    if (i < SECURITY_HEADERS.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSectionTitle(
    title: String,
    badge: String?,
    badgeColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
        badge?.let {
            Surface(color = badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = it,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = Teal.copy(alpha = 0.25f))
    }
}

@Composable
private fun HeaderRow(name: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
        }
    }
}

// ─── Cookies tab ──────────────────────────────────────────────────────────────

@Composable
private fun CookiesContent(richCookies: List<CookieInfo>, cookies: Map<String, String>) {
    val context = LocalContext.current
    var db by remember { mutableStateOf<com.ecliptic.fingerprint.CookieDatabase?>(null) }
    LaunchedEffect(Unit) { db = com.ecliptic.fingerprint.CookieDatabase.get(context) }
    var selected by remember { mutableStateOf<Pair<CookieInfo, com.ecliptic.fingerprint.CookieDatabase.Entry?>?>(null) }

    selected?.let { (c, known) ->
        if (known != null) {
            ReferenceSheet(
                title = c.name,
                body = known.description,
                rows = listOf(
                    "Set by" to known.platform,
                    "Category" to known.category,
                    "Retention" to known.retention,
                ),
                url = com.ecliptic.fingerprint.CookieDatabase.SOURCE_URL,
                source = com.ecliptic.fingerprint.CookieDatabase.SOURCE_NAME,
                onDismiss = { selected = null },
            )
        } else {
            ReferenceSheet(
                title = c.name,
                body = "Not found in the Open Cookie Database. It's likely proprietary — set by this site's own application rather than a known third-party platform. Inspect its value and attributes to judge its purpose.",
                url = com.ecliptic.fingerprint.CookieDatabase.SOURCE_URL,
                source = com.ecliptic.fingerprint.CookieDatabase.SOURCE_NAME,
                onDismiss = { selected = null },
            )
        }
    }

    val displayCookies = richCookies.ifEmpty {
        cookies.entries.map { (k, v) -> CookieInfo(name = k, value = v) }
    }
    if (displayCookies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No cookies detected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("COOKIES", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Teal.copy(alpha = 0.25f))
                Text("${displayCookies.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                TabCopyButton {
                    displayCookies.sortedBy { it.name }.joinToString("\n\n") { c ->
                        buildString {
                            append("${c.name}=${c.value}")
                            val flags = buildList {
                                if (c.httpOnly) add("HttpOnly")
                                if (c.secure) add("Secure")
                                c.sameSite?.let { add("SameSite=$it") }
                                c.domain?.let { add("Domain=$it") }
                                c.path?.let { add("Path=$it") }
                                c.expires?.let { add("Expires=$it") }
                            }
                            if (flags.isNotEmpty()) append("; ${flags.joinToString("; ")}")
                        }
                    }
                }
            }
        }
        items(displayCookies.sortedBy { it.name }) { c ->
            val known = remember(c.name, db) { db?.lookup(c.name) }
            Surface(
                modifier = Modifier.fillMaxWidth().then(if (db != null) Modifier.clickable { selected = c to known } else Modifier),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Name + flags row
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(c.name, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        if (c.httpOnly) CookieFlagChip("HttpOnly")
                        if (c.secure)   CookieFlagChip("Secure")
                        c.sameSite?.let { CookieFlagChip(it) }
                    }
                    // Known-cookie classification (Open Cookie Database)
                    if (db != null) {
                        if (known != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                CookieClassChip("${known.platform} · ${known.category}", Teal)
                                Text("ⓘ", color = Teal.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        } else {
                            CookieClassChip("proprietary / unknown", Amber)
                        }
                    }
                    // Value
                    SelectionContainer {
                        Text(
                            text = c.value,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    // Attributes
                    val attrs = buildList {
                        c.domain?.let { add("Domain: $it") }
                        c.path?.let { add("Path: $it") }
                        c.expires?.let { add("Expires: $it") }
                        c.maxAge?.let { add("Max-Age: $it") }
                    }
                    if (attrs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            attrs.forEach { attr ->
                                Text(attr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CookieFlagChip(label: String) {
    Surface(color = Teal.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), color = Teal, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CookieClassChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.13f), shape = RoundedCornerShape(4.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── Security Headers tab ─────────────────────────────────────────────────────

private val SECURITY_HEADER_KEYS = listOf(
    "content-security-policy"            to "Content-Security-Policy",
    "strict-transport-security"          to "Strict-Transport-Security",
    "x-frame-options"                    to "X-Frame-Options",
    "x-content-type-options"             to "X-Content-Type-Options",
    "referrer-policy"                    to "Referrer-Policy",
    "permissions-policy"                 to "Permissions-Policy",
    "x-xss-protection"                   to "X-XSS-Protection",
    "cross-origin-opener-policy"         to "Cross-Origin-Opener-Policy",
    "cross-origin-embedder-policy"       to "Cross-Origin-Embedder-Policy",
    "cross-origin-resource-policy"       to "Cross-Origin-Resource-Policy",
)

@Composable
private fun SecurityHeadersContent(signals: RawSignals?, responseHeaders: Map<String, String>) {
    val normalised = responseHeaders.mapKeys { it.key.lowercase() }
    val present  = SECURITY_HEADER_KEYS.filter { (k, _) -> k in normalised }
    val missing  = SECURITY_HEADER_KEYS.filter { (k, _) -> k !in normalised }
    val score    = present.size
    val total    = SECURITY_HEADER_KEYS.size
    val findings = remember(signals) { signals?.let { SecurityAnalyzer.analyze(it) } ?: emptyList() }
    var ref by remember { mutableStateOf<com.ecliptic.fingerprint.SecurityReference.Ref?>(null) }

    ref?.let { r ->
        ReferenceSheet(title = r.title, body = r.summary, url = r.url, source = r.source, onDismiss = { ref = null })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TabCopyButton {
                    buildString {
                        appendLine("Security Headers: $score / $total")
                        appendLine()
                        if (present.isNotEmpty()) {
                            appendLine("PRESENT:")
                            present.forEach { (k, label) -> appendLine("  ✓ $label: ${normalised[k] ?: ""}") }
                            appendLine()
                        }
                        if (missing.isNotEmpty()) {
                            appendLine("MISSING:")
                            missing.forEach { (_, label) -> appendLine("  ✗ $label") }
                        }
                    }.trim()
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("$score / $total", color = if (score >= 7) Green else if (score >= 4) Teal else Red, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("security headers present", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { score.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (score >= 7) Green else if (score >= 4) Teal else Red,
                trackColor = MaterialTheme.colorScheme.surface,
            )
            Spacer(Modifier.height(12.dp))
        }
        // ── Analysis: concrete misconfigurations (framing, CSP, CORS, SRI, …) ──
        if (findings.isNotEmpty()) {
            item {
                Text("ANALYSIS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.12.em)
                Spacer(Modifier.height(4.dp))
            }
            items(findings) { f ->
                val amber = Color(0xFFD9920A)
                val accent = when (f.severity) { "alert" -> Red; "warn" -> amber; "ok" -> Green; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                val mark = when (f.severity) { "alert" -> "⚠"; "warn" -> "⚠"; "ok" -> "✓"; else -> "ⓘ" }
                val learn = com.ecliptic.fingerprint.SecurityReference.forText("${f.title} ${f.detail}")
                Surface(
                    modifier = Modifier.fillMaxWidth().then(if (learn != null) Modifier.clickable { ref = learn } else Modifier),
                    color = accent.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$mark  ${f.title}", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (learn != null) Text("ⓘ", color = accent.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                        Text(f.detail, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
        if (present.isNotEmpty()) {
            item {
                Text("PRESENT", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.12.em)
                Spacer(Modifier.height(4.dp))
            }
            items(present) { (key, label) ->
                val value = normalised[key] ?: ""
                val learn = com.ecliptic.fingerprint.SecurityReference.forHeader(key)
                Surface(
                    modifier = Modifier.fillMaxWidth().then(if (learn != null) Modifier.clickable { ref = learn } else Modifier),
                    color = Green.copy(alpha = 0.07f), shape = RoundedCornerShape(8.dp),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, color = Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (learn != null) Text("ⓘ", color = Green.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                        SelectionContainer {
                            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        if (missing.isNotEmpty()) {
            item {
                Text("MISSING", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.12.em)
                Spacer(Modifier.height(4.dp))
            }
            items(missing) { (key, label) ->
                val learn = com.ecliptic.fingerprint.SecurityReference.forHeader(key)
                Surface(
                    modifier = Modifier.fillMaxWidth().then(if (learn != null) Modifier.clickable { ref = learn } else Modifier),
                    color = Red.copy(alpha = 0.06f), shape = RoundedCornerShape(8.dp),
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        if (learn != null) Text("ⓘ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─── APIs tab ─────────────────────────────────────────────────────────────────

@Composable
private fun ApisContent(apis: List<DetectedApi>) {
    if (apis.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No APIs detected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }
    // Group by API type, preserving the detector's ordering (concrete endpoints first).
    val grouped: List<Pair<String, List<DetectedApi>>> =
        apis.groupBy { it.type }.toList()
    val endpointCount = apis.count { it.endpoint != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$endpointCount endpoint${if (endpointCount == 1) "" else "s"} · ${grouped.size} type${if (grouped.size == 1) "" else "s"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                TabCopyButton {
                    grouped.joinToString("\n\n") { (type, items) ->
                        buildString {
                            appendLine("# $type")
                            items.forEach { api ->
                                append(api.endpoint ?: "(${api.evidence})")
                                appendLine()
                            }
                        }.trimEnd()
                    }
                }
            }
        }
        grouped.forEach { (type, items) ->
            item(key = "api_type_$type") {
                val accent = if (type == "WebSocket") Green else Teal
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(type, color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Surface(color = accent.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    "${items.size}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        items.forEach { api ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (api.endpoint != null) {
                                    Text(
                                        api.endpoint, color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp,
                                    )
                                }
                                Text(
                                    api.evidence, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp, lineHeight = 13.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Spider tab ─────────────────────────────────────────────────────────────

@Composable
private fun SpiderContent(viewModel: ScanViewModel, url: String, onSendToRepeater: (String) -> Unit) {
    val spider by viewModel.spiderState.collectAsStateWithLifecycle()
    val status = spider.status

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Controls ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (status) {
                SpiderStatus.RUNNING -> {
                    SpiderButton("Pause", Icons.Filled.Pause) { viewModel.pauseSpider() }
                    SpiderButton("Stop", Icons.Filled.Stop, danger = true) { viewModel.stopSpider() }
                }
                SpiderStatus.PAUSED -> {
                    SpiderButton("Resume", Icons.Filled.PlayArrow) { viewModel.resumeSpider() }
                    SpiderButton("Stop", Icons.Filled.Stop, danger = true) { viewModel.stopSpider() }
                }
                else -> {
                    SpiderButton(if (status == SpiderStatus.IDLE) "Start" else "Restart", Icons.Filled.PlayArrow) {
                        viewModel.startSpider(url)
                    }
                    if (spider.discovered.isNotEmpty()) {
                        SpiderButton("Clear", Icons.Filled.Clear) { viewModel.resetSpider() }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (status == SpiderStatus.RUNNING) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Teal, strokeWidth = 2.dp)
            }
        }

        // ── Status line ──
        val statusText = when (status) {
            SpiderStatus.IDLE    -> "Idle — press Start to crawl this site"
            SpiderStatus.RUNNING -> "Crawling… ${spider.visited} visited · ${spider.queued} queued"
            SpiderStatus.PAUSED  -> "Paused — ${spider.discovered.size} URLs found"
            SpiderStatus.DONE    -> "Done — ${spider.discovered.size} URLs" +
                if (spider.fromSitemap) " (seeded from sitemap.xml)" else ""
            SpiderStatus.STOPPED -> "Stopped — ${spider.discovered.size} URLs found"
        }
        Text(statusText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        spider.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        if (status == SpiderStatus.RUNNING && spider.currentUrl.isNotBlank()) {
            Text(
                spider.currentUrl,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

        // ── URL list ──
        if (spider.discovered.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No URLs yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${spider.discovered.size} URL${if (spider.discovered.size == 1) "" else "s"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                TabCopyButton { spider.discovered.joinToString("\n") }
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(spider.discovered) { u ->
                    UrlActionRow(u, onSendToRepeater = onSendToRepeater)
                }
            }
        }
    }
}

/**
 * A monospace URL/line that, on tap, offers app-wide actions: open in browser,
 * copy the full URL, and (optionally) send it to the Repeater.
 */
@Composable
private fun UrlActionRow(
    url: String,
    modifier: Modifier = Modifier,
    onSendToRepeater: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            url,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp,
            modifier = modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 4.dp),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Open in browser") },
                leadingIcon = { Icon(Icons.Filled.OpenInBrowser, null, modifier = Modifier.size(16.dp)) },
                onClick = { expanded = false; runCatching { uriHandler.openUri(if (url.startsWith("http")) url else "https://$url") } },
            )
            DropdownMenuItem(
                text = { Text("Copy URL") },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp)) },
                onClick = {
                    expanded = false
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("url", url))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
            )
            if (onSendToRepeater != null) {
                DropdownMenuItem(
                    text = { Text("Send to Repeater") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp)) },
                    onClick = { expanded = false; onSendToRepeater(url) },
                )
            }
        }
    }
}

/**
 * Modal viewer for a remote file (JS/CSS/HTML/etc). Fetches the content, shows the
 * full filename + URL with open-in-browser / copy actions, and supports pinch-zoom.
 * Reused from Net, Source and Spider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileViewerSheet(url: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    var content by remember(url) { mutableStateOf<String?>(null) }
    var zoom by remember { mutableStateOf(1f) }
    LaunchedEffect(url) { content = fetchTextContent(url) }
    val fname = remember(url) { url.substringAfterLast('/').substringBefore('?').ifBlank { url } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fname, color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace, maxLines = 2, modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { runCatching { uriHandler.openUri(url) } }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.OpenInBrowser, "Open in browser", tint = Teal, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("url", url))
                    Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Filled.ContentCopy, "Copy URL", tint = Teal, modifier = Modifier.size(18.dp))
                }
            }
            Text(url, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))
            val c = content
            when {
                c == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Teal) }
                c.isBlank() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Empty or failed to load", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                else -> {
                    val lines = remember(c) { c.lines() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectTransformGestures { _, _, z, _ -> zoom = (zoom * z).coerceIn(0.6f, 3f) }
                        },
                        contentPadding = PaddingValues(bottom = 40.dp),
                    ) {
                        itemsIndexed(lines) { i, line ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.Top) {
                                Text(
                                    "${i + 1}", modifier = Modifier.width(40.dp).padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    fontSize = (10 * zoom).sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End,
                                )
                                Text(
                                    line, modifier = Modifier.weight(1f).padding(end = 12.dp),
                                    fontFamily = FontFamily.Monospace, fontSize = (11 * zoom).sp,
                                    lineHeight = (16 * zoom).sp, color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpiderButton(label: String, icon: ImageVector, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) MaterialTheme.colorScheme.error else Teal
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, label, modifier = Modifier.size(15.dp), tint = color)
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Param tab (Intel) ────────────────────────────────────────────────────────

@Composable
private fun ParamContent(html: String) {
    val params = remember(html) { Extractor.findParams(html) }
    if (params.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No parameters found in source", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${params.size} parameter${if (params.size == 1) "" else "s"} from URLs & forms",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f),
                )
                TabCopyButton { params.joinToString("\n") { if (it.second.isBlank()) it.first else "${it.first}=${it.second}" } }
            }
        }
        items(params) { (name, value) ->
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(name, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                    if (value.isNotBlank()) {
                        Text("= $value", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ─── Strings tab (Code) ───────────────────────────────────────────────────────

@Composable
private fun StringsContent(
    html: String,
    scriptUrls: List<String>,
    deepText: String,
    onDeepText: (String) -> Unit,
    onOpenInSource: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var deepLoading by remember { mutableStateOf(false) }
    val deepScanned = deepText.isNotBlank()
    var selected by remember { mutableStateOf<Extractor.StringFinding?>(null) }

    val findings = remember(html, deepText) {
        (Extractor.findStrings(html, origin = "HTML") +
            if (deepText.isNotBlank()) Extractor.findStrings(deepText, origin = "JS") else emptyList()).distinct()
    }
    val grouped = findings.groupBy { it.category }.toList()

    selected?.let { f ->
        StringDetailSheet(finding = f, onOpenInSource = onOpenInSource, onDismiss = { selected = null })
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "${findings.size} found" + if (deepScanned) " · incl. JS" else " · HTML only",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f),
            )
            if (scriptUrls.isNotEmpty() && !deepScanned) {
                Surface(
                    onClick = {
                        deepLoading = true
                        scope.launch {
                            val sb = StringBuilder()
                            scriptUrls.take(8).forEach { sb.append(fetchTextContent(it)).append('\n') }
                            onDeepText(sb.toString())
                            deepLoading = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Teal.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Teal),
                    enabled = !deepLoading,
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (deepLoading) CircularProgressIndicator(Modifier.size(14.dp), color = Teal, strokeWidth = 2.dp)
                        else Icon(Icons.Filled.Search, null, tint = Teal, modifier = Modifier.size(14.dp))
                        Text(if (deepLoading) "Scanning JS…" else "Deep scan JS", color = Teal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            TabCopyButton { findings.joinToString("\n") { "[${it.category}] ${it.value}" } }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        if (findings.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    if (scriptUrls.isNotEmpty() && !deepScanned) "Nothing in the main HTML.\nTap \"Deep scan JS\" to check script files."
                    else "No interesting strings found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                grouped.forEach { (cat, items) ->
                    item(key = "sh_$cat") {
                        val accent = when (items.first().severity) {
                            "alert" -> MaterialTheme.colorScheme.error
                            "warn" -> Color(0xFFF59E0B)
                            else -> Teal
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(cat, color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${items.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    items(items) { f ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selected = f }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                f.value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp,
                                modifier = Modifier.weight(1f),
                            )
                            if (f.origin.isNotBlank()) {
                                Text(
                                    f.origin,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 9.sp, fontFamily = SpaceGrotesk,
                                    modifier = Modifier.padding(start = 8.dp, top = 1.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detail sheet for a single Strings finding: shows its category, severity, origin
 * (HTML vs JS), the full value, and actions — copy the value, find it in the Source
 * viewer, or open it in a browser when it looks like a URL/email.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringDetailSheet(
    finding: Extractor.StringFinding,
    onOpenInSource: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    val accent = when (finding.severity) {
        "alert" -> MaterialTheme.colorScheme.error
        "warn" -> Amber
        else -> Teal
    }
    val looksLinkable = finding.category == "Email" ||
        finding.value.startsWith("http", ignoreCase = true) ||
        Regex("""^[a-z0-9.\-]+\.[a-z]{2,}(/|$)""", RegexOption.IGNORE_CASE).containsMatchIn(finding.value)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(finding.category, color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Surface(color = accent.copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp)) {
                    Text(finding.severity.uppercase(), color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(Modifier.weight(1f))
                if (finding.origin.isNotBlank()) {
                    Text("found in ${finding.origin}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = SpaceGrotesk)
                }
            }
            Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                SelectionContainer {
                    Text(
                        finding.value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 17.sp,
                        modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()).padding(12.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpiderButton("Copy value", Icons.Filled.ContentCopy) {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(finding.category, finding.value))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
                SpiderButton("Find in source", Icons.Filled.Search) {
                    onOpenInSource(finding.value.trim().take(60))
                    onDismiss()
                }
            }
            if (looksLinkable) {
                SpiderButton("Open in browser", Icons.Filled.OpenInBrowser) {
                    val target = if (finding.category == "Email") "mailto:${finding.value}"
                        else if (finding.value.startsWith("http", true)) finding.value else "https://${finding.value}"
                    runCatching { uriHandler.openUri(target) }
                }
            }
        }
    }
}

/**
 * Generic "learn more" sheet: a title, a plain-language explanation, optional
 * key/value rows, and an optional reference link that opens in a browser. Used by
 * the Sec Headers definitions and the Cookie classifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceSheet(
    title: String,
    body: String,
    rows: List<Pair<String, String>> = emptyList(),
    url: String? = null,
    source: String? = null,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (rows.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    rows.forEach { (k, v) ->
                        if (v.isNotBlank()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$k:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.width(84.dp))
                            Text(v, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            if (body.isNotBlank()) {
                Text(body, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, lineHeight = 19.sp)
            }
            if (url != null) {
                SpiderButton("Read on ${source ?: "the web"}", Icons.Filled.OpenInBrowser) {
                    runCatching { uriHandler.openUri(url) }
                }
            }
        }
    }
}

// ─── Recon tab ────────────────────────────────────────────────────────────────

@Composable
private fun ReconContent(
    url: String,
    data: ReconData?,
    loading: Boolean,
    onLoad: () -> Unit,
    onRefresh: () -> Unit,
    portsLoading: Boolean,
    stagingLoading: Boolean,
    subsLoading: Boolean,
    buildOriginLoading: Boolean,
    onScanPorts: () -> Unit,
    onEnumStaging: (EnumTier) -> Unit,
    onFindSubdomains: () -> Unit,
    onAnalyzeBuildOrigin: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val domain = remember(url) {
        try { java.net.URI(url).host?.removePrefix("www.") } catch (_: Exception) { null }
    }

    // Recon is fetched once per scan and cached in the ViewModel; trigger the first
    // load lazily, and let the user refresh. No re-fetch on every tab open.
    LaunchedEffect(url) { if (url.isNotBlank()) onLoad() }

    val reconFiles   = data?.files
    val rdapResult   = data?.rdap
    val reconLoading = loading
    val rdapLoading  = loading

    var robotsExpanded   by remember { mutableStateOf(false) }
    var securityExpanded by remember { mutableStateOf(false) }
    var sitemapExpanded  by remember { mutableStateOf(false) }
    var portsInfoExpanded by remember { mutableStateOf(false) }

    if (url.isBlank()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No URL scanned", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(domain ?: url, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    data?.takeIf { it.fetchedAt > 0L }?.let {
                        Text(
                            "Cached " + android.text.format.DateFormat.format("MMM d, HH:mm", it.fetchedAt),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.size(36.dp)) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Teal, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, "Refresh recon", tint = Teal, modifier = Modifier.size(18.dp))
                        }
                    }
                    TabCopyButton {
                        buildString {
                            appendLine("Archive: https://web.archive.org/web/*/$url")
                        appendLine()
                        rdapResult?.let { r ->
                            appendLine("WHOIS / REGISTRAR")
                            r.registrar?.let { appendLine("Registrar: $it") }
                            r.registrant?.let { appendLine("Registrant: $it") }
                            r.created?.let { appendLine("Created: $it") }
                            r.expires?.let { appendLine("Expires: $it") }
                            r.updated?.let { appendLine("Updated: $it") }
                            if (r.status.isNotEmpty()) appendLine("Status: ${r.status.joinToString(", ")}")
                            if (r.nameServers.isNotEmpty()) appendLine("Name servers:\n${r.nameServers.joinToString("\n")}")
                            appendLine()
                        }
                        reconFiles?.robotsTxt?.let {
                            appendLine("ROBOTS.TXT")
                            appendLine(it)
                            appendLine()
                        }
                        reconFiles?.securityTxt?.let {
                            appendLine("SECURITY.TXT")
                            appendLine(it)
                            appendLine()
                        }
                        reconFiles?.sitemapXml?.let {
                            appendLine("SITEMAP.XML")
                            appendLine(it.lines().take(50).joinToString("\n"))
                        }
                        }.trim()
                    }
                }
            }
        }

        // ── Build origin (LLM / vibe-coding authorship estimate) ───────────────
        item { NetSectionHeader("BUILD ORIGIN", null, loading || buildOriginLoading) }
        item {
            val bo = data?.buildOrigin
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bo == null) {
                        Text(if (loading) "Analyzing…" else "Not analyzed", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    } else {
                        val accent = when (bo.verdict) {
                            com.ecliptic.scanner.BuildOriginAnalyzer.Verdict.PLATFORM -> Amber
                            com.ecliptic.scanner.BuildOriginAnalyzer.Verdict.HEAVY -> Amber
                            com.ecliptic.scanner.BuildOriginAnalyzer.Verdict.MIXED -> Teal
                            com.ecliptic.scanner.BuildOriginAnalyzer.Verdict.MINIMAL -> Teal
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(bo.label, color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (bo.confidence != "n/a") {
                                Surface(color = accent.copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp)) {
                                    Text(bo.confidence.uppercase(), color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        if (bo.evidence.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                bo.evidence.forEach { e ->
                                    Text("• $e", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
                                }
                            }
                        }
                        if (bo.note.isNotBlank()) {
                            Text(bo.note, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f), fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpiderButton(
                            if (bo?.analyzedJs == true) "Re-analyze JS" else "Analyze bundled JS",
                            Icons.Filled.Search,
                        ) { if (!buildOriginLoading) onAnalyzeBuildOrigin() }
                        if (buildOriginLoading) CircularProgressIndicator(Modifier.size(16.dp), color = Teal, strokeWidth = 2.dp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Archive.org link
        item { NetSectionHeader("ARCHIVE", null, false) }
        item {
            val archiveUrl = "https://web.archive.org/web/*/$url"
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri(archiveUrl) } },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("View on Wayback Machine", color = Teal, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.OpenInBrowser, null, tint = Teal, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // WHOIS / RDAP
        item { NetSectionHeader("WHOIS / REGISTRAR", null, rdapLoading) }
        when {
            rdapLoading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Teal, strokeWidth = 2.dp)
                }
            }
            rdapResult?.error != null -> item {
                Surface(color = Red.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text("⚠  ${rdapResult!!.error}", modifier = Modifier.padding(12.dp), color = Red, fontSize = 12.sp)
                }
            }
            rdapResult != null -> item {
                val r = rdapResult!!
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.registrar?.let { NetKvRow("Registrar", it) }
                        r.registrant?.let { NetKvRow("Registrant", it) }
                        r.created?.let  { NetKvRow("Created", it) }
                        r.expires?.let  { NetKvRow("Expires", it) }
                        r.updated?.let  { NetKvRow("Updated", it) }
                        if (r.status.isNotEmpty()) NetKvRow("Status", r.status.joinToString(", "))
                        if (r.nameServers.isNotEmpty()) NetKvRow("Name servers", r.nameServers.joinToString("\n"))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── DNS / email security (SPF / DMARC / DKIM / DNSSEC / CAA) ────────────
        item { NetSectionHeader("DNS SECURITY", null, loading) }
        item {
            val em = data?.email
            if (em == null) {
                Text(if (loading) "Checking…" else "Not checked", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            } else {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecCheckRow("SPF", em.spf != null, em.spf ?: "No SPF record")
                        SecCheckRow("DMARC", em.dmarc != null, em.dmarc ?: "No DMARC record")
                        SecCheckRow("DKIM", em.dkimSelector != null, em.dkimSelector?.let { "Selector: $it" } ?: "No common selector found")
                        SecCheckRow("DNSSEC", em.dnssec == true, if (em.dnssec == true) "Signed (DNSKEY present)" else "Not signed")
                        SecCheckRow("CAA", em.caa.isNotEmpty(), if (em.caa.isNotEmpty()) em.caa.joinToString("\n") else "No CAA records")
                        data?.httpsRedirect?.let { r ->
                            SecCheckRow("HTTP→HTTPS", r, if (r) "Redirects to HTTPS" else "Does NOT redirect to HTTPS")
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Alt ports (manual TCP probe) ───────────────────────────────────────
        item { NetSectionHeader("ALT PORTS", null, portsLoading) }
        item {
            val ports = data?.openPorts ?: emptyList()
            val checked = data?.portsChecked == true
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        portsLoading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Teal, strokeWidth = 2.dp)
                            Text("Probing ${com.ecliptic.scanner.DEFAULT_SCAN_PORTS.size} common ports…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        checked && ports.isEmpty() -> Text("No open alt ports found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        checked -> ports.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(":${p.port}", color = Teal, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                Text(p.service, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("open", color = Teal, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        else -> Text(
                            "Probes the project host for services on common non-standard ports (8000, 8443, 9000…). Only run against hosts you own or are authorized to test.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp,
                        )
                    }
                    if (!portsLoading) {
                        Button(
                            onClick = onScanPorts,
                            colors = ButtonDefaults.buttonColors(containerColor = Teal.copy(alpha = 0.14f), contentColor = Teal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Filled.WifiTethering, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(if (checked) "Rescan ports" else "Scan common ports", fontSize = 12.sp, fontFamily = SpaceGrotesk)
                        }
                    }
                    // Disclose exactly which ports are probed.
                    Text(
                        text = if (portsInfoExpanded) "Hide the ${com.ecliptic.scanner.DEFAULT_SCAN_PORTS.size} ports checked ▲"
                               else "Which ${com.ecliptic.scanner.DEFAULT_SCAN_PORTS.size} ports are checked? ▼",
                        color = Teal, fontSize = 11.sp, fontFamily = SpaceGrotesk,
                        modifier = Modifier.clickable { portsInfoExpanded = !portsInfoExpanded },
                    )
                    if (portsInfoExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            com.ecliptic.scanner.SCAN_PORT_INFO.forEach { (port, service) ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(":$port", color = Teal, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(52.dp))
                                    Text(service, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Subdomains (above Staging; the two share data) ─────────────────────
        val stagingHostSet = (data?.staging ?: emptyList()).map { it.host }.toSet()
        val subsUnion = ((data?.subdomains ?: emptyList()) + stagingHostSet).distinct().sorted()
        val subsChecked = data?.subdomainsChecked == true
        item { NetSectionHeader("SUBDOMAINS", subsUnion.size.takeIf { it > 0 }, loading || subsLoading) }
        item {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (subsUnion.isEmpty()) {
                        Text(
                            if (loading || subsLoading) "Enumerating…"
                            else "None yet. A check pulls from certificate-transparency logs (crt.sh) and the scanned page source. Staging hosts you find also appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp,
                        )
                    }
                    Button(
                        onClick = onFindSubdomains,
                        enabled = !subsLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal.copy(alpha = 0.14f), contentColor = Teal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        if (subsLoading) CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Teal, strokeWidth = 2.dp)
                        else Icon(Icons.Filled.Search, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(if (subsUnion.isNotEmpty() || subsChecked) "Re-check subdomains" else "Find subdomains", fontSize = 12.sp, fontFamily = SpaceGrotesk)
                    }
                }
            }
        }
        items(subsUnion) { sub ->
            val isStaging = sub in stagingHostSet || com.ecliptic.scanner.looksLikeStaging(sub)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri("https://$sub") } },
                color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp),
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(sub, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    if (isStaging) Text("staging", color = Amber, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Staging / pre-prod host enumeration (manual, tiered) ───────────────
        item { NetSectionHeader("STAGING HOSTS", null, stagingLoading) }
        item {
            val hosts = data?.staging ?: emptyList()
            val checked = data?.stagingChecked == true
            // Subdomains that look like staging environments, surfaced here too.
            val stagingFromSubs = (data?.subdomains ?: emptyList())
                .filter { com.ecliptic.scanner.looksLikeStaging(it) && it !in stagingHostSet }
            val coveredTier = data?.stagingTier?.let { runCatching { EnumTier.valueOf(it) }.getOrNull() }
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Resolves common staging/pre-prod names (staging.…, dev.…, uat.…) plus known-host defaults. DNS lookups only. A higher depth adds to the list — it never re-checks or clears what's found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EnumTier.entries.forEach { tier ->
                            val covered = coveredTier != null && coveredTier.ordinal >= tier.ordinal
                            val clickable = !stagingLoading && !covered
                            Surface(
                                onClick = { if (clickable) onEnumStaging(tier) },
                                enabled = clickable,
                                shape = RoundedCornerShape(8.dp),
                                color = if (covered) Teal.copy(alpha = 0.18f) else Teal.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Teal.copy(alpha = if (covered) 0.7f else 0.5f)),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        if (covered) Icon(Icons.Filled.Check, null, tint = Teal, modifier = Modifier.size(11.dp))
                                        Text(tier.label, color = Teal, fontSize = 12.sp, fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("~${tier.count}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                    when {
                        stagingLoading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Teal, strokeWidth = 2.dp)
                            Text("Enumerating…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        checked && hosts.isEmpty() -> Text("No staging hosts resolved.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        checked -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            hosts.forEach { h ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(h.host, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                    if (h.source == "provider") {
                                        Text("default", color = Amber, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    h.ip?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                                }
                            }
                        }
                    }
                    // Staging-looking hosts discovered via subdomains/source (no DNS brute needed).
                    if (stagingFromSubs.isNotEmpty()) {
                        Text("From subdomains:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            stagingFromSubs.forEach { h ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(h, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                    Text("subdomain", color = Teal, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // robots.txt
        item { NetSectionHeader("ROBOTS.TXT", null, reconLoading) }
        when {
            reconLoading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Teal, strokeWidth = 2.dp)
                }
            }
            reconFiles?.robotsTxt == null -> item {
                Text("Not found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            else -> item {
                val txt = reconFiles!!.robotsTxt!!
                val preview = if (robotsExpanded) txt else txt.lines().take(8).joinToString("\n")
                Surface(modifier = Modifier.fillMaxWidth().clickable { robotsExpanded = !robotsExpanded }, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        SelectionContainer { Text(preview, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp) }
                        if (txt.lines().size > 8) {
                            Text(if (robotsExpanded) "Show less" else "Show all (${txt.lines().size} lines)", color = Teal, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // security.txt
        item { NetSectionHeader("SECURITY.TXT", null, reconLoading) }
        if (!reconLoading) {
            if (reconFiles?.securityTxt == null) {
                item { Text("Not found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
            } else {
                val txt = reconFiles!!.securityTxt!!
                item {
                    val preview = if (securityExpanded) txt else txt.lines().take(8).joinToString("\n")
                    Surface(modifier = Modifier.fillMaxWidth().clickable { securityExpanded = !securityExpanded }, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            SelectionContainer { Text(preview, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp) }
                            if (txt.lines().size > 8) {
                                Text(if (securityExpanded) "Show less" else "Show all (${txt.lines().size} lines)", color = Teal, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // sitemap.xml
        item { NetSectionHeader("SITEMAP.XML", null, reconLoading) }
        if (!reconLoading) {
            if (reconFiles?.sitemapXml == null) {
                item { Text("Not found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
            } else {
                val txt = reconFiles!!.sitemapXml!!
                item {
                    val preview = if (sitemapExpanded) txt else txt.lines().take(8).joinToString("\n")
                    Surface(modifier = Modifier.fillMaxWidth().clickable { sitemapExpanded = !sitemapExpanded }, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            SelectionContainer { Text(preview, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp) }
                            if (txt.lines().size > 8) {
                                Text(if (sitemapExpanded) "Show less" else "Show all (${txt.lines().size} lines)", color = Teal, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Trust signals (keyless; derived from recon) ────────────────────────
        val trust = SecurityAnalyzer.trustSignals(data)
        item { NetSectionHeader("TRUST SIGNALS", null, loading) }
        if (trust.isEmpty()) {
            item { Text(if (loading) "Evaluating…" else "Not checked", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
        } else {
            items(trust) { f ->
                val amber = Color(0xFFD9920A)
                val accent = when (f.severity) { "alert" -> Red; "warn" -> amber; "ok" -> Green; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                val mark = when (f.severity) { "alert", "warn" -> "⚠"; "ok" -> "✓"; else -> "ⓘ" }
                Surface(modifier = Modifier.fillMaxWidth(), color = accent.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("$mark  ${f.title}", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(f.detail, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── IP neighbors (reverse-IP) ──────────────────────────────────────────
        val neighbors = data?.ipNeighbors ?: emptyList()
        item { NetSectionHeader("IP NEIGHBORS", neighbors.size.takeIf { it > 0 }, loading) }
        val ipAddr = data?.dns?.aRecords?.firstOrNull()
        if (ipAddr != null) {
            // Target reference, so the neighbor list reads in context.
            item {
                Text(
                    "${domain ?: "Target"} is hosted at $ipAddr",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            item {
                when {
                    data?.ipChecked != true -> Text(
                        if (loading) "Looking up neighbors…" else "Could not determine — reverse-IP lookup unavailable (rate-limited or blocked).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                    )
                    data?.dedicatedIp == true -> Surface(color = Teal.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "Dedicated IP — only ${domain ?: "this site"} resolves to $ipAddr. Reverse-IP found no other sites co-hosted here, so the list below is empty by design.",
                            modifier = Modifier.padding(12.dp), color = Teal, fontSize = 12.sp, lineHeight = 16.sp,
                        )
                    }
                    else -> Surface(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        Text("Shared IP — ${neighbors.size} other site(s) resolve to $ipAddr.", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }
                }
            }
        } else if (!loading) {
            item { Text("No A record to check", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
        }
        items(neighbors.take(60)) { n ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri("https://$n") } },
                color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp),
            ) {
                Text(n, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Similar / look-alike domains (registered typosquats) ───────────────
        val sims = data?.similarDomains ?: emptyList()
        item { NetSectionHeader("SIMILAR DOMAINS", sims.size.takeIf { it > 0 }, loading) }
        if (sims.isEmpty()) {
            item { Text(if (loading) "Checking look-alikes…" else "No registered look-alikes found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
        } else {
            item { Text("Registered look-alike domains (possible typosquats):", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp)) }
            items(sims) { s ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri("https://${s.domain}") } },
                    color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.domain, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Icon(Icons.Filled.OpenInBrowser, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ─── REQ / RES tab ────────────────────────────────────────────────────────────

@Composable
private fun ReqResContent(
    rawRequest: String,
    signals: RawSignals?,
    isFullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
) {
    val rawResponse = remember(signals) {
        if (signals == null) return@remember ""
        buildString {
            appendLine("HTTP/1.1 200 OK")
            signals.headers.forEach { (k, v) -> appendLine("$k: $v") }
            appendLine()
            append(signals.htmlSource.take(50_000))
        }
    }

    var searchQuery  by remember { mutableStateOf("") }
    var expandedPane by remember { mutableStateOf<String?>(null) } // "req" | "res" | null
    val reqListState = rememberLazyListState()
    val resListState = rememberLazyListState()
    val scope        = rememberCoroutineScope()

    val reqLines = remember(rawRequest, searchQuery) {
        val lines = rawRequest.lines()
        if (searchQuery.isBlank()) lines.mapIndexed { i, l -> i to l }
        else lines.mapIndexed { i, l -> i to l }.filter { (_, l) -> l.contains(searchQuery, ignoreCase = true) }
    }
    val resLines = remember(rawResponse, searchQuery) {
        val lines = rawResponse.lines()
        if (searchQuery.isBlank()) lines.mapIndexed { i, l -> i to l }
        else lines.mapIndexed { i, l -> i to l }.filter { (_, l) -> l.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Shared search bar
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                decorationBox = { inner ->
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Box { if (searchQuery.isEmpty()) Text("Filter…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp); inner() }
                    }
                },
            )
        }

        // Request pane — hidden when response is expanded
        if (expandedPane != "res") {
            Column(modifier = Modifier.weight(1f)) {
                ReqResPaneHeader(
                    label = "REQUEST",
                    labelColor = Teal,
                    expanded = expandedPane == "req",
                    listState = reqListState,
                    lines = reqLines,
                    scope = scope,
                    copyText = { rawRequest },
                    onToggleExpand = {
                        val next = if (expandedPane == "req") null else "req"
                        if (next != null && !isFullScreen) onToggleFullScreen()
                        else if (next == null && isFullScreen) onToggleFullScreen()
                        expandedPane = next
                    },
                )
                HorizontalDivider(color = Teal.copy(alpha = 0.2f))
                if (rawRequest.isBlank()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No request captured", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    SelectionContainer {
                        LazyColumn(state = reqListState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                            items(reqLines) { (origIdx, line) ->
                                val color = if (origIdx == 0) Teal else if (line.contains(':')) Blue else MaterialTheme.colorScheme.onSurface
                                val highlighted = if (searchQuery.isNotBlank() && line.contains(searchQuery, ignoreCase = true)) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                Box(modifier = Modifier.fillMaxWidth().background(highlighted)) {
                                    Text(line, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (expandedPane == null) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Response pane — hidden when request is expanded
        if (expandedPane != "req") {
            Column(modifier = Modifier.weight(1f)) {
                ReqResPaneHeader(
                    label = "RESPONSE",
                    labelColor = Blue,
                    expanded = expandedPane == "res",
                    listState = resListState,
                    lines = resLines,
                    scope = scope,
                    copyText = { rawResponse },
                    onToggleExpand = {
                        val next = if (expandedPane == "res") null else "res"
                        if (next != null && !isFullScreen) onToggleFullScreen()
                        else if (next == null && isFullScreen) onToggleFullScreen()
                        expandedPane = next
                    },
                )
                HorizontalDivider(color = Blue.copy(alpha = 0.2f))
                if (rawResponse.isBlank() || signals == null) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No response captured", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    SelectionContainer {
                        LazyColumn(state = resListState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                            items(resLines) { (origIdx, line) ->
                                val color = if (origIdx == 0) Green else if (line.contains(':')) Blue else MaterialTheme.colorScheme.onSurface
                                val highlighted = if (searchQuery.isNotBlank() && line.contains(searchQuery, ignoreCase = true)) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                Box(modifier = Modifier.fillMaxWidth().background(highlighted)) {
                                    Text(line, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReqResPaneHeader(
    label: String,
    labelColor: Color,
    expanded: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    lines: List<Pair<Int, String>>,
    scope: kotlinx.coroutines.CoroutineScope,
    copyText: () -> String = { "" },
    onToggleExpand: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
        Row {
            IconButton(onClick = { scope.launch { listState.scrollToItem(0) } }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.VerticalAlignTop, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { scope.launch { listState.scrollToItem(maxOf(0, listState.firstVisibleItemIndex - 10)) } }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { scope.launch { listState.scrollToItem(minOf(lines.lastIndex, listState.firstVisibleItemIndex + 10)) } }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = { scope.launch { if (lines.isNotEmpty()) listState.scrollToItem(lines.lastIndex) } }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.VerticalAlignBottom, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText(label, copyText()))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.ContentCopy, null, tint = labelColor, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                Icon(if (expanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = labelColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Source tab ───────────────────────────────────────────────────────────────

private val SH_COMMENT   = Color(0xFF6A9955)
private val SH_TAG       = Color(0xFF569CD6)
private val SH_ATTR_NAME = Color(0xFF9CDCFE)
private val SH_ATTR_VAL  = Color(0xFFCE9178)
private val SH_TEXT      = Color(0xFFCCCCCC)
private val SH_PUNCT     = Color(0xFF888888)

private fun highlightHtmlLine(line: String): AnnotatedString = buildAnnotatedString {
    if (line.contains("<!--")) {
        withStyle(SpanStyle(color = SH_COMMENT)) { append(line) }
        return@buildAnnotatedString
    }
    var i = 0
    while (i < line.length) {
        if (line[i] == '<') {
            val end = line.indexOf('>', i + 1).let { if (it < 0) line.length - 1 else it }
            appendHtmlTag(line.substring(i, end + 1))
            i = end + 1
        } else {
            val next = line.indexOf('<', i).let { if (it < 0) line.length else it }
            withStyle(SpanStyle(color = SH_TEXT)) { append(line.substring(i, next)) }
            i = next
        }
    }
}

private fun AnnotatedString.Builder.appendHtmlTag(tag: String) {
    if (tag.length < 2) { withStyle(SpanStyle(color = SH_PUNCT)) { append(tag) }; return }
    if (tag[1] == '!' || tag[1] == '?') { withStyle(SpanStyle(color = SH_TAG)) { append(tag) }; return }
    withStyle(SpanStyle(color = SH_PUNCT)) { append("<") }
    var i = 1
    if (i < tag.length && tag[i] == '/') { withStyle(SpanStyle(color = SH_PUNCT)) { append("/") }; i++ }
    val nameStart = i
    while (i < tag.length && tag[i] != ' ' && tag[i] != '>' && tag[i] != '/' && tag[i] != '\t') i++
    if (i > nameStart) withStyle(SpanStyle(color = SH_TAG)) { append(tag.substring(nameStart, i)) }
    while (i < tag.length) {
        when {
            tag[i] == '>' -> { withStyle(SpanStyle(color = SH_PUNCT)) { append(">") }; i++ }
            tag[i] == '/' -> { withStyle(SpanStyle(color = SH_PUNCT)) { append("/") }; i++ }
            tag[i] == ' ' || tag[i] == '\t' -> {
                val ws = i; while (i < tag.length && (tag[i] == ' ' || tag[i] == '\t')) i++
                append(tag.substring(ws, i))
            }
            tag[i] == '"' -> {
                val qEnd = tag.indexOf('"', i + 1).let { if (it < 0) tag.length - 1 else it }
                withStyle(SpanStyle(color = SH_ATTR_VAL)) { append(tag.substring(i, qEnd + 1)) }
                i = qEnd + 1
            }
            tag[i] == '\'' -> {
                val qEnd = tag.indexOf('\'', i + 1).let { if (it < 0) tag.length - 1 else it }
                withStyle(SpanStyle(color = SH_ATTR_VAL)) { append(tag.substring(i, qEnd + 1)) }
                i = qEnd + 1
            }
            tag[i] == '=' -> { withStyle(SpanStyle(color = SH_PUNCT)) { append("=") }; i++ }
            else -> {
                val attrStart = i
                while (i < tag.length && tag[i] != '=' && tag[i] != ' ' && tag[i] != '>' && tag[i] != '/' && tag[i] != '\t') i++
                withStyle(SpanStyle(color = SH_ATTR_NAME)) { append(tag.substring(attrStart, i)) }
            }
        }
    }
}

private fun saveSourceFile(context: Context, content: String, name: String? = null): String {
    return try {
        val filename = name ?: "ecliptic_source_${System.currentTimeMillis()}.html"
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/plain")
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: return "Save failed"
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            "Saved to Downloads/$filename"
        } else {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            java.io.File(dir, filename).writeText(content)
            "Saved: $filename"
        }
    } catch (e: Exception) {
        "Save failed: ${e.message}"
    }
}

private fun applySearchHighlight(base: AnnotatedString, query: String, isCurrentMatch: Boolean): AnnotatedString {
    if (query.length < 2) return base
    return buildAnnotatedString {
        append(base)
        val lower = base.text.lowercase()
        val queryLower = query.lowercase()
        val bgColor = if (isCurrentMatch) Color(0xFFFFD700) else Color(0xFF8B7500).copy(alpha = 0.4f)
        val fgColor = if (isCurrentMatch) Color.Black else Color.Unspecified
        var start = 0
        while (true) {
            val idx = lower.indexOf(queryLower, start)
            if (idx < 0) break
            addStyle(SpanStyle(background = bgColor, color = fgColor), idx, idx + query.length)
            start = idx + query.length
        }
    }
}

private fun highlightJsLine(line: String): AnnotatedString = buildAnnotatedString {
    val trimmed = line.trimStart()
    when {
        trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*") ->
            withStyle(SpanStyle(color = SH_COMMENT)) { append(line) }
        else -> withStyle(SpanStyle(color = SH_TEXT)) { append(line) }
    }
}

private fun highlightCssLine(line: String): AnnotatedString = buildAnnotatedString {
    val trimmed = line.trimStart()
    when {
        trimmed.startsWith("/*") || trimmed.startsWith("*") || trimmed.startsWith("//") ->
            withStyle(SpanStyle(color = SH_COMMENT)) { append(line) }
        trimmed.endsWith("{") || trimmed == "}" || trimmed == "}," ->
            withStyle(SpanStyle(color = SH_TAG)) { append(line) }
        trimmed.contains(":") && !trimmed.startsWith("@") -> {
            val colonIdx = line.indexOf(':')
            withStyle(SpanStyle(color = SH_ATTR_NAME)) { append(line.substring(0, colonIdx + 1)) }
            withStyle(SpanStyle(color = SH_ATTR_VAL)) { append(line.substring(colonIdx + 1)) }
        }
        trimmed.startsWith("@") -> withStyle(SpanStyle(color = SH_TAG)) { append(line) }
        else -> withStyle(SpanStyle(color = SH_TEXT)) { append(line) }
    }
}

@Composable
private fun SourceFileChip(label: String, selected: Boolean, loading: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(10.dp), color = Teal, strokeWidth = 1.5.dp)
            Text(
                text = label,
                color = if (selected) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SourceContent(
    html: String,
    scriptUrls: List<String> = emptyList(),
    cssUrls: List<String> = emptyList(),
    initialQuery: String = "",
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
) {
    var selectedFile by remember { mutableStateOf("__html__") }
    var remoteContents by remember { mutableStateOf(mapOf<String, String>()) }
    var remoteLoading by remember { mutableStateOf(setOf<String>()) }
    // Seeded from a Strings "Find in source" jump; re-seeds whenever a new value arrives.
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var currentMatchIdx by remember { mutableIntStateOf(0) }
    var codeZoom by remember { mutableStateOf(1f) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()

    val isHtml = selectedFile == "__html__"
    val isCss = selectedFile in cssUrls
    val currentContent = if (isHtml) html else remoteContents[selectedFile] ?: ""
    val lines = remember(currentContent) { currentContent.lines() }
    val truncated = currentContent.length >= 499990

    val matchIndices = remember(searchQuery, lines) {
        if (searchQuery.length < 2) emptyList()
        else lines.indices.filter { lines[it].contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(matchIndices) { currentMatchIdx = 0 }

    LaunchedEffect(currentMatchIdx, matchIndices) {
        if (matchIndices.isNotEmpty()) listState.animateScrollToItem(matchIndices[currentMatchIdx])
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // File selector
        if (scriptUrls.isNotEmpty() || cssUrls.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item { SourceFileChip("HTML", isHtml) { selectedFile = "__html__" } }
                items(cssUrls.take(20)) { url ->
                    val label = url.substringAfterLast("/").substringBefore("?").take(22).ifBlank { "style.css" }
                    SourceFileChip(label, selectedFile == url, url in remoteLoading) {
                        selectedFile = url
                        if (url !in remoteContents && url !in remoteLoading) {
                            remoteLoading = remoteLoading + url
                            scope.launch {
                                val content = fetchTextContent(url)
                                remoteContents = remoteContents + (url to content)
                                remoteLoading = remoteLoading - url
                            }
                        }
                    }
                }
                items(scriptUrls.take(30)) { url ->
                    val label = url.substringAfterLast("/").substringBefore("?").take(22).ifBlank { "script.js" }
                    SourceFileChip(label, selectedFile == url, url in remoteLoading) {
                        selectedFile = url
                        if (url !in remoteContents && url !in remoteLoading) {
                            remoteLoading = remoteLoading + url
                            scope.launch {
                                val content = fetchTextContent(url)
                                remoteContents = remoteContents + (url to content)
                                remoteLoading = remoteLoading - url
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        }

        // Search row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Compact search field (stock OutlinedTextField has a ~56dp min height
            // that reads as too tall here).
            Surface(
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text("Search…", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground),
                            cursorBrush = SolidColor(Teal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            if (matchIndices.isNotEmpty()) {
                Text(
                    "${currentMatchIdx + 1}/${matchIndices.size}",
                    color = Teal, fontSize = 11.sp,
                    modifier = Modifier.width(48.dp),
                )
                IconButton(
                    onClick = { currentMatchIdx = (currentMatchIdx - 1 + matchIndices.size) % matchIndices.size },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = Teal, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { currentMatchIdx = (currentMatchIdx + 1) % matchIndices.size },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = Teal, modifier = Modifier.size(20.dp))
                }
            } else if (searchQuery.length >= 2) {
                Text("No results", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            IconButton(onClick = onToggleFullScreen, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    if (isFullScreen) "Exit fullscreen" else "Fullscreen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Nav + action toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${lines.size} lines${if (truncated) " · 200KB limit" else ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.VerticalAlignTop, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = {
                scope.launch {
                    val pg = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                    listState.animateScrollToItem((listState.firstVisibleItemIndex - pg).coerceAtLeast(0))
                }
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = {
                scope.launch {
                    val pg = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                    listState.animateScrollToItem((listState.firstVisibleItemIndex + pg).coerceAtMost((lines.size - 1).coerceAtLeast(0)))
                }
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { scope.launch { listState.animateScrollToItem((lines.size - 1).coerceAtLeast(0)) } }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.VerticalAlignBottom, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Source", currentContent))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.ContentCopy, null, tint = Teal, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = {
                val fname = if (!isHtml) selectedFile.substringAfterLast("/").substringBefore("?").takeIf { it.isNotBlank() } else null
                Toast.makeText(context, saveSourceFile(context, currentContent, fname), Toast.LENGTH_LONG).show()
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Save, null, tint = Teal, modifier = Modifier.size(18.dp))
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Full filename + actions for remote files (chips above are truncated)
        if (!isHtml) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedFile.substringAfterLast('/').substringBefore('?').ifBlank { selectedFile },
                        color = Teal, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                        maxLines = 1, modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { runCatching { uriHandler.openUri(selectedFile) } }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.OpenInBrowser, "Open in browser", tint = Teal, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("url", selectedFile))
                        Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.ContentCopy, "Copy URL", tint = Teal, modifier = Modifier.size(16.dp))
                    }
                }
                Text(selectedFile, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        }

        // Content area
        when {
            !isHtml && selectedFile in remoteLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
            !isHtml && selectedFile !in remoteContents -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Tap file chip above to load", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            currentContent.isBlank() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No source captured", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, z, _ -> codeZoom = (codeZoom * z).coerceIn(0.6f, 3f) }
                    },
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(lines) { index, line ->
                    val isCurrentMatch = matchIndices.getOrNull(currentMatchIdx) == index
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.width(40.dp).padding(end = 8.dp),
                            color = if (isCurrentMatch) Teal
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            fontSize = (10 * codeZoom).sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                        )
                        Text(
                            text = remember(line, searchQuery, isCurrentMatch) {
                                val base = when {
                                    isHtml -> highlightHtmlLine(line)
                                    isCss  -> highlightCssLine(line)
                                    else   -> highlightJsLine(line)
                                }
                                applySearchHighlight(base, searchQuery, isCurrentMatch)
                            },
                            modifier = Modifier.weight(1f).padding(end = 16.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = (11 * codeZoom).sp,
                            lineHeight = (16 * codeZoom).sp,
                        )
                    }
                }
            }
        }
    }
}

// ─── JS globals tab ───────────────────────────────────────────────────────────

@Composable
private fun JsGlobalsContent(
    globals: Map<String, Any?>,
    jsProbe: Map<String, String>,
) {
    if (globals.isEmpty() && jsProbe.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No JavaScript globals detected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TabCopyButton {
                    buildString {
                        if (globals.isNotEmpty()) {
                            appendLine("=== GLOBALS ===")
                            globals.entries.sortedBy { it.key }.forEach { (k, v) -> appendLine("$k: ${v ?: "true"}") }
                        }
                        if (jsProbe.isNotEmpty()) {
                            if (globals.isNotEmpty()) appendLine()
                            appendLine("=== JS PROPERTIES ===")
                            jsProbe.entries.sortedBy { it.key }.forEach { (k, v) -> appendLine("$k: $v") }
                        }
                    }.trim()
                }
            }
        }
        if (globals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("DETECTED GLOBALS", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Teal.copy(alpha = 0.25f))
                    Text("${globals.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            items(globals.entries.sortedBy { it.key }) { (key, value) ->
                JsGlobalRow(key = key, value = value?.toString())
            }
        }

        if (jsProbe.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("JS PROPERTIES", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Blue.copy(alpha = 0.25f))
                    Text("${jsProbe.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            items(jsProbe.entries.sortedBy { it.key }) { (key, value) ->
                JsGlobalRow(key = key, value = value)
            }
        }
    }
}

@Composable
private fun JsGlobalRow(key: String, value: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = key,
                color = Teal,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            val display = value?.takeIf { it.isNotBlank() && it != "true" && it != "detected" }
            if (display != null) {
                Text(
                    text = display.take(40),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

// ─── Network tab (DNS / WHOIS / Social) ───────────────────────────────────────

private data class SocialLink(val platform: String, val url: String)

private val SOCIAL_PATTERNS = listOf(
    "Twitter / X"  to Regex("""https?://(?:www\.)?(?:twitter|x)\.com/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "Facebook"     to Regex("""https?://(?:www\.)?facebook\.com/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "Instagram"    to Regex("""https?://(?:www\.)?instagram\.com/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "LinkedIn"     to Regex("""https?://(?:www\.)?linkedin\.com/(?:company|in)/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "YouTube"      to Regex("""https?://(?:www\.)?youtube\.com/(?:@|c/|channel/)[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "GitHub"       to Regex("""https?://(?:www\.)?github\.com/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "TikTok"       to Regex("""https?://(?:www\.)?tiktok\.com/@[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
    "Pinterest"    to Regex("""https?://(?:www\.)?pinterest\.com/[^/"'\s?#]+""", RegexOption.IGNORE_CASE),
)

private fun extractSocialLinks(html: String): List<SocialLink> {
    val seen = mutableSetOf<String>()
    val results = mutableListOf<SocialLink>()
    for ((platform, pattern) in SOCIAL_PATTERNS) {
        for (match in pattern.findAll(html)) {
            val url = match.value.trimEnd('/')
            if (url in seen) continue
            // Skip share/sharer/intent links
            if (url.contains("share") || url.contains("intent") || url.contains("sharer")) continue
            seen += url
            results += SocialLink(platform, url)
        }
    }
    return results
}

@Composable
private fun NetworkContent(
    url: String,
    html: String,
    tls: TlsResult? = null,
    dns: DnsResult? = null,
    asn: String? = null,
    loading: Boolean = false,
    onLoad: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val domain = remember(url) {
        try { java.net.URI(url).host?.removePrefix("www.") } catch (_: Exception) { null }
    }

    // DNS/ASN come from the cached recon fetched once by the ViewModel; trigger the
    // first load lazily (shared with the Recon tab — no duplicate network calls).
    LaunchedEffect(url) { if (url.isNotBlank()) onLoad() }

    val dnsResult = dns
    val dnsLoading = loading && dns == null

    val socialLinks = remember(html) { extractSocialLinks(html) }

    if (domain == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No URL scanned", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(domain, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                TabCopyButton {
                    buildString {
                        tls?.let { t ->
                            appendLine("TLS / CERTIFICATE")
                            t.subject?.let { appendLine("Subject: $it") }
                            t.issuer?.let { appendLine("Issuer: $it") }
                            if (t.validFrom != null && t.validTo != null) appendLine("Valid: ${t.validFrom} → ${t.validTo}")
                            t.tlsVersion?.let { appendLine("Protocol: $it") }
                            t.cipherSuite?.let { appendLine("Cipher: $it") }
                            if (t.sans.isNotEmpty()) appendLine("SANs: ${t.sans.joinToString(", ")}")
                            appendLine()
                        }
                        dnsResult?.let { d ->
                            appendLine("DNS RECORDS")
                            if (d.aRecords.isNotEmpty()) appendLine("A:\n${d.aRecords.joinToString("\n")}")
                            if (d.cnameRecords.isNotEmpty()) appendLine("CNAME:\n${d.cnameRecords.joinToString("\n")}")
                            if (d.mxRecords.isNotEmpty()) appendLine("MX:\n${d.mxRecords.joinToString("\n")}")
                            if (d.nsRecords.isNotEmpty()) appendLine("NS:\n${d.nsRecords.joinToString("\n")}")
                            if (d.txtRecords.isNotEmpty()) appendLine("TXT:\n${d.txtRecords.joinToString("\n")}")
                            appendLine()
                        }
                        asn?.let { appendLine("ASN: $it\n") }
                        if (socialLinks.isNotEmpty()) {
                            appendLine("SOCIAL LINKS")
                            socialLinks.forEach { appendLine("${it.platform}: ${it.url}") }
                        }
                    }.trim()
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // TLS Certificate
        if (url.startsWith("https", ignoreCase = true)) {
            item { NetSectionHeader("TLS / CERTIFICATE", null, false) }
            when {
                tls == null -> item {
                    Text("No certificate info captured", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                tls.error != null -> item {
                    Surface(color = Red.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        Text("⚠  ${tls.error}", modifier = Modifier.padding(12.dp), color = Red, fontSize = 12.sp)
                    }
                }
                else -> item {
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tls.subject?.let { NetKvRow("Subject", it) }
                            tls.issuer?.let { NetKvRow("Issuer", it) }
                            if (tls.validFrom != null && tls.validTo != null) {
                                NetKvRow("Valid", "${tls.validFrom} → ${tls.validTo}")
                            }
                            tls.tlsVersion?.let { NetKvRow("Protocol", it) }
                            tls.cipherSuite?.let { NetKvRow("Cipher", it) }
                            if (tls.sans.isNotEmpty()) {
                                NetKvRow("SANs", tls.sans.take(6).joinToString("\n"))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // Social links (no network needed — parsed from HTML)
        if (socialLinks.isNotEmpty()) {
            item { NetSectionHeader("SOCIAL LINKS", socialLinks.size, false) }
            items(socialLinks) { link ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri(link.url) } },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DevIconMapper.iconUrlFor(link.platform)?.let { ic ->
                            AsyncImage(
                                model = ic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(Teal, BlendMode.SrcIn),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(link.platform, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(
                                link.url.removePrefix("https://").removePrefix("http://").take(60),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                        }
                        Icon(Icons.Filled.OpenInBrowser, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // ASN
        asn?.let { asnVal ->
            item { NetSectionHeader("ASN", null, false) }
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                    Text(asnVal, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // DNS records
        item { NetSectionHeader("DNS RECORDS", null, dnsLoading) }
        when {
            dnsLoading -> item {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Teal, strokeWidth = 2.dp)
                }
            }
            dnsResult?.error != null -> item {
                Surface(color = Red.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text("⚠  ${dnsResult!!.error}", modifier = Modifier.padding(12.dp), color = Red, fontSize = 12.sp)
                }
            }
            dnsResult != null -> {
                val d = dnsResult!!
                val allEmpty = d.aRecords.isEmpty() && d.cnameRecords.isEmpty() && d.mxRecords.isEmpty() && d.nsRecords.isEmpty() && d.txtRecords.isEmpty()
                if (allEmpty) {
                    item { Text("No DNS records found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
                } else {
                    if (d.aRecords.isNotEmpty()) {
                        item { DnsTypeHeader("A  —  IPv4") }
                        items(d.aRecords) { DnsValueRow(it) }
                    }
                    if (d.cnameRecords.isNotEmpty()) {
                        item { DnsTypeHeader("CNAME") }
                        items(d.cnameRecords) { DnsValueRow(it) }
                    }
                    if (d.mxRecords.isNotEmpty()) {
                        item { DnsTypeHeader("MX  —  Mail") }
                        items(d.mxRecords) { DnsValueRow(it) }
                    }
                    if (d.nsRecords.isNotEmpty()) {
                        item { DnsTypeHeader("NS  —  Name servers") }
                        items(d.nsRecords) { DnsValueRow(it) }
                    }
                    if (d.txtRecords.isNotEmpty()) {
                        item { DnsTypeHeader("TXT") }
                        items(d.txtRecords) { DnsValueRow(it, mono = true) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetSectionHeader(title: String, count: Int?, loading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Teal.copy(alpha = 0.25f))
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Teal, strokeWidth = 1.5.dp)
        } else if (count != null) {
            Text("$count", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun NetKvRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(0.35f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, modifier = Modifier.weight(0.65f))
    }
}

@Composable
private fun SecCheckRow(label: String, pass: Boolean, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            if (pass) Icons.Filled.Check else Icons.Filled.Close,
            null,
            tint = if (pass) Green else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp).padding(top = 1.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(78.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 15.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DnsTypeHeader(label: String) {
    Text(
        text = label,
        color = Blue,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun DnsValueRow(value: String, mono: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            lineHeight = 16.sp,
        )
    }
}

// ─── DOM tree tab ─────────────────────────────────────────────────────────────

private data class DomNode(
    val tag: String,
    val id: String?,
    val cls: String?,
    val attrs: Map<String, String>,
    val children: List<DomNode>,
    val moreChildren: Int,
    val nodeId: Int,
)

private data class FlatDomItem(
    val nodeId: Int,
    val depth: Int,
    val tag: String,
    val attrSummary: String,
    val hasChildren: Boolean,
    val moreChildren: Int,
)

private fun parseDomNode(obj: JSONObject, counter: IntArray): DomNode {
    val nodeId = counter[0]++
    val tag = obj.optString("t", "?")
    val id = obj.optString("id").takeIf { it.isNotBlank() }
    val cls = obj.optString("c").takeIf { it.isNotBlank() }
    val attrsObj = obj.optJSONObject("a")
    val attrs = if (attrsObj != null) buildMap { attrsObj.keys().forEach { put(it, attrsObj.optString(it)) } } else emptyMap()
    val childArr = obj.optJSONArray("ch")
    val children = if (childArr != null) {
        (0 until childArr.length()).mapNotNull {
            try { parseDomNode(childArr.getJSONObject(it), counter) } catch (_: Exception) { null }
        }
    } else emptyList()
    return DomNode(tag, id, cls, attrs, children, obj.optInt("more", 0), nodeId)
}

private fun parseDomJson(json: String): DomNode? = try {
    parseDomNode(JSONObject(json), IntArray(1) { 0 })
} catch (_: Exception) { null }

private fun flattenDom(node: DomNode, expandedIds: Set<Int>, depth: Int = 0): List<FlatDomItem> {
    val result = mutableListOf<FlatDomItem>()
    val parts = mutableListOf<String>()
    node.id?.let { parts.add("#$it") }
    node.cls?.split(" ")?.filter { it.isNotBlank() }?.take(2)?.forEach { parts.add(".$it") }
    node.attrs.entries.take(2).forEach { (k, v) -> parts.add("$k=\"${v.take(25)}\"") }
    result.add(FlatDomItem(node.nodeId, depth, node.tag, parts.joinToString(" "), node.children.isNotEmpty() || node.moreChildren > 0, node.moreChildren))
    if (node.nodeId in expandedIds) {
        node.children.forEach { result.addAll(flattenDom(it, expandedIds, depth + 1)) }
        if (node.moreChildren > 0) {
            result.add(FlatDomItem(-(node.nodeId + 1), depth + 1, "…", "+${node.moreChildren} more", false, 0))
        }
    }
    return result
}

@Composable
private fun DomContent(domTreeJson: String, isFullScreen: Boolean = false, onToggleFullScreen: () -> Unit = {}) {
    if (domTreeJson.isBlank()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("DOM tree not captured — rescan to populate", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }
    val root = remember(domTreeJson) { parseDomJson(domTreeJson) }
    if (root == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Failed to parse DOM tree", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }
    val defaultExpanded = remember(root) {
        buildSet { add(root.nodeId); root.children.forEach { add(it.nodeId) } }
    }
    var expandedIds by remember(root) { mutableStateOf(defaultExpanded) }
    var domZoom by remember { mutableStateOf(1f) }
    val items = remember(expandedIds) { flattenDom(root, expandedIds) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTransformGestures { _, _, z, _ -> domZoom = (domZoom * z).coerceIn(0.6f, 3f) }
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("DOM TREE", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { expandedIds = buildSet { fun addAll(n: DomNode) { add(n.nodeId); n.children.forEach { addAll(it) } }; addAll(root) } },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) { Text("Expand all", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
                    TextButton(
                        onClick = { expandedIds = emptySet() },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) { Text("Collapse all", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
                    IconButton(onClick = onToggleFullScreen, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            if (isFullScreen) "Exit fullscreen" else "Fullscreen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
        items(items, key = { it.nodeId }) { item ->
            if (item.tag == "…") {
                Text(
                    text = item.attrSummary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = (item.depth * 14 + 20).dp, top = 2.dp, bottom = 2.dp),
                )
            } else {
                val expanded = item.nodeId in expandedIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.hasChildren) {
                            expandedIds = if (expanded) expandedIds - item.nodeId else expandedIds + item.nodeId
                        }
                        .padding(start = (item.depth * 14).dp, top = 1.dp, bottom = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(20.dp)) {
                        if (item.hasChildren) {
                            Text(if (expanded) "▾" else "▸", color = Teal, fontSize = 10.sp)
                        }
                    }
                    Text("<${item.tag}>", color = SH_TAG, fontSize = (11 * domZoom).sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                    if (item.attrSummary.isNotBlank()) {
                        Text(
                            text = " ${item.attrSummary}",
                            color = SH_ATTR_NAME,
                            fontSize = (11 * domZoom).sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

// ─── Network tab ─────────────────────────────────────────────────────────────

private val NET_TYPE_COLOR = mapOf(
    "script"     to Color(0xFF7C4DFF),
    "stylesheet" to Color(0xFF0091EA),
    "xhr"        to Color(0xFF00BCD4),
    "fetch"      to Color(0xFF00BCD4),
    "image"      to Color(0xFF43A047),
    "font"       to Color(0xFFFF6F00),
    "media"      to Color(0xFFE91E63),
    "other"      to Color(0xFF607D8B),
)

private val NET_FILTER_TYPES = listOf("ALL", "script", "stylesheet", "xhr", "image", "font", "media", "other")

@Composable
private fun NetContent(
    scanRequests: List<NetworkRequest>,
    liveRequests: List<NetworkRequest>,
) {
    val all = remember(scanRequests.size, liveRequests.size) {
        (scanRequests + liveRequests).distinctBy { it.url + it.type }
    }
    var selectedType by remember { mutableStateOf("ALL") }
    var query by remember { mutableStateOf("") }
    var viewerUrl by remember { mutableStateOf<String?>(null) }

    val filtered = remember(all, selectedType, query) {
        all.filter {
            (selectedType == "ALL" || it.type == selectedType) &&
                (query.isBlank() || it.url.contains(query, ignoreCase = true))
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            placeholder = { Text("Filter URLs…", fontSize = 13.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
            textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            leadingIcon = { Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp)) },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(NET_FILTER_TYPES) { type ->
                val isSelected = type == selectedType
                val count = if (type == "ALL") all.size else all.count { it.type == type }
                if (type != "ALL" && count == 0) return@items
                val chipColor = if (type == "ALL") Teal else (NET_TYPE_COLOR[type] ?: Teal)
                Surface(
                    onClick = { selectedType = type },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) chipColor.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) chipColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            type,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (count > 0) {
                            Text(
                                "$count",
                                fontSize = 10.sp,
                                color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            TabCopyButton { all.joinToString("\n") { "[${it.type.uppercase()}] ${it.url}" } }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    if (all.isEmpty()) "No network requests captured" else "No matches",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            ) {
                itemsIndexed(filtered) { _, req ->
                    NetRequestRow(req) { viewerUrl = req.url }
                }
            }
        }
    }
    viewerUrl?.let { FileViewerSheet(it) { viewerUrl = null } }
}

@Composable
private fun NetRequestRow(req: NetworkRequest, onOpen: () -> Unit) {
    val typeColor = NET_TYPE_COLOR[req.type] ?: Color(0xFF607D8B)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = typeColor.copy(alpha = 0.15f),
        ) {
            Text(
                req.type.take(5).uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = typeColor,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            )
        }
        if (req.method != "GET") {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Teal.copy(alpha = 0.15f),
            ) {
                Text(
                    req.method,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Teal,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                )
            }
        }
        Text(
            text = req.url,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

// ─── Console tab ─────────────────────────────────────────────────────────────

private sealed class ConsoleEntry {
    data class PageMsg(val msg: ConsoleMessage, val id: Long = System.nanoTime()) : ConsoleEntry()
    data class UserCmd(val input: String, val id: Long = System.nanoTime()) : ConsoleEntry()
    data class CmdResult(val result: String, val isError: Boolean, val id: Long = System.nanoTime()) : ConsoleEntry()
}

private val CONSOLE_BG      = Color(0xFF0D1117)
private val CONSOLE_SURFACE = Color(0xFF161B22)
private val CONSOLE_BORDER  = Color(0xFF30363D)

private data class ConsolePreset(val label: String, val cmd: String)

private val CONSOLE_PRESETS = listOf(
    ConsolePreset("title",        "document.title"),
    ConsolePreset("forms",        """JSON.stringify([...document.querySelectorAll('form')].map(f=>({id:f.id,action:f.action,method:f.method,fields:[...f.elements].map(e=>e.name)})),null,2)"""),
    ConsolePreset("links",        """JSON.stringify([...document.querySelectorAll('a[href]')].map(a=>a.href).filter(h=>h.startsWith(location.origin)),null,2)"""),
    ConsolePreset("cookies",      "document.cookie"),
    ConsolePreset("localStorage", "JSON.stringify({...localStorage})"),
    ConsolePreset("sessionStore", "JSON.stringify({...sessionStorage})"),
    ConsolePreset("Next.js",      """JSON.stringify({buildId:window.__NEXT_DATA__?.buildId,page:window.__NEXT_DATA__?.page,query:window.__NEXT_DATA__?.query})"""),
    ConsolePreset("WordPress",    """JSON.stringify({wpApiSettings:window.wpApiSettings,woocommerce:window.woocommerce_params,ajaxUrl:window.ajaxurl})"""),
    ConsolePreset("Shopify",      """JSON.stringify({theme:window.Shopify?.theme,currency:window.Shopify?.currency,routes:window.Shopify?.routes})"""),
    ConsolePreset("Vue.js",       "JSON.stringify(window.Vue?.version)"),
    ConsolePreset("Nuxt.js",      "JSON.stringify(window.__NUXT__?.config)"),
    ConsolePreset("scripts",      """JSON.stringify([...document.querySelectorAll('script[src]')].map(s=>s.src))"""),
    ConsolePreset("externals",    """JSON.stringify([...document.querySelectorAll('link[rel="stylesheet"],script[src],img[src]')].map(e=>e.href||e.src).filter(u=>u&&!u.startsWith(location.origin)),null,2)"""),
    ConsolePreset("meta",         """JSON.stringify([...document.querySelectorAll('meta')].map(m=>({name:m.name||m.property||m.httpEquiv,content:m.content})).filter(m=>m.name),null,2)"""),
    ConsolePreset("timing",       """JSON.stringify(performance.getEntriesByType('resource').map(e=>({name:e.name,type:e.initiatorType,duration:Math.round(e.duration)})).slice(0,20),null,2)"""),
    ConsolePreset("JWT/tokens",   """[localStorage.getItem('token'),localStorage.getItem('jwt'),localStorage.getItem('access_token'),sessionStorage.getItem('token'),document.cookie].filter(Boolean).join('\n')"""),
    ConsolePreset("hidden",       """JSON.stringify([...document.querySelectorAll('input[type="hidden"]')].map(i=>({name:i.name,value:i.value})),null,2)"""),
    ConsolePreset("fetch?",       "typeof fetch"),
    ConsolePreset("CSP",          """document.querySelector('meta[http-equiv="Content-Security-Policy"]')?.content||'Not in meta'"""),
    ConsolePreset("comments",     """[...document.querySelectorAll('*')].flatMap(n=>[...n.childNodes]).filter(n=>n.nodeType===8).map(n=>n.textContent.trim()).filter(Boolean).join('\n')"""),
)

private fun consoleColor(level: String) = when (level.lowercase()) {
    "error" -> Color(0xFFFF5555)
    "warn"  -> Color(0xFFFFB86C)
    "info"  -> Color(0xFF8BE9FD)
    "debug" -> Color(0xFF6272A4)
    else    -> Color(0xFFCDD9E5)
}

@Composable
private fun ConsoleContent(
    messages: List<ConsoleMessage>,
    evalJs: (String, (String?) -> Unit) -> Unit,
) {
    val entries = remember { mutableStateOf<List<ConsoleEntry>>(emptyList()) }
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    var lastCmd by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        val already = entries.value.count { it is ConsoleEntry.PageMsg }
        val newMsgs = messages.drop(already)
        if (newMsgs.isNotEmpty()) {
            entries.value = entries.value + newMsgs.map { ConsoleEntry.PageMsg(it) }
        }
    }

    LaunchedEffect(entries.value.size) {
        if (entries.value.isNotEmpty()) listState.animateScrollToItem(entries.value.size - 1)
    }

    fun runCmd(cmd: String) {
        if (cmd.isBlank()) return
        lastCmd = cmd
        input = ""
        entries.value = entries.value + ConsoleEntry.UserCmd(cmd)
        // Wrap in eval() so statements and multi-expression work, not just expressions
        val escaped = cmd
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t")
        val wrapped = """(function(){try{var __r=eval("$escaped");return __r===undefined?'undefined':__r===null?'null':typeof __r==='object'?JSON.stringify(__r,null,2):String(__r);}catch(e){return 'Uncaught '+e;}})()"""
        evalJs(wrapped) { raw ->
            val result = raw ?: "undefined"
            val clean = result
                .removeSurrounding("\"")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            val isErr = clean.startsWith("Uncaught")
            entries.value = entries.value + ConsoleEntry.CmdResult(clean, isErr)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CONSOLE_BG),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Page JS context — same as Firefox DevTools console",
                fontSize = 10.sp,
                color = Color(0xFF6272A4),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 5.dp),
            )
            TabCopyButton(modifier = Modifier.padding(end = 4.dp)) {
                entries.value.joinToString("\n") { e ->
                    when (e) {
                        is ConsoleEntry.PageMsg -> "[${e.msg.level.uppercase()}] ${e.msg.message}"
                        is ConsoleEntry.UserCmd -> "> ${e.input}"
                        is ConsoleEntry.CmdResult -> e.result
                    }
                }
            }
        }
        HorizontalDivider(color = CONSOLE_BORDER)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            if (entries.value.isEmpty()) {
                item {
                    Text(
                        "Waiting for console output…",
                        color = Color(0xFF6272A4),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
            itemsIndexed(entries.value, key = { _, e ->
                when (e) {
                    is ConsoleEntry.PageMsg   -> e.id
                    is ConsoleEntry.UserCmd   -> e.id
                    is ConsoleEntry.CmdResult -> e.id
                }
            }) { _, entry ->
                when (entry) {
                    is ConsoleEntry.PageMsg -> {
                        val color = consoleColor(entry.msg.level)
                        val prefix = when (entry.msg.level.lowercase()) {
                            "error" -> "[ERR] "
                            "warn"  -> "[WRN] "
                            "info"  -> "[INF] "
                            "debug" -> "[DBG] "
                            else    -> "[LOG] "
                        }
                        SelectionContainer {
                            Text(
                                text = prefix + entry.msg.message,
                                color = color,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(vertical = 1.dp),
                            )
                        }
                    }
                    is ConsoleEntry.UserCmd -> {
                        Text(
                            text = "> ${entry.input}",
                            color = Teal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                    is ConsoleEntry.CmdResult -> {
                        SelectionContainer {
                            Text(
                                text = entry.result,
                                color = if (entry.isError) Color(0xFFFF5555) else Color(0xFFCDD9E5),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(start = 12.dp, top = 1.dp, bottom = 1.dp),
                            )
                        }
                    }
                }
            }
        }
        // Preset command chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CONSOLE_SURFACE),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(CONSOLE_PRESETS) { preset ->
                Surface(
                    onClick = { input = preset.cmd },
                    shape = RoundedCornerShape(4.dp),
                    color = CONSOLE_BG,
                    border = BorderStroke(1.dp, CONSOLE_BORDER),
                ) {
                    Text(
                        preset.label,
                        color = Color(0xFF8BE9FD),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = CONSOLE_BORDER)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CONSOLE_SURFACE)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = { if (lastCmd.isNotBlank()) input = lastCmd },
                modifier = Modifier.size(28.dp),
                enabled = lastCmd.isNotBlank(),
            ) {
                Icon(
                    Icons.Filled.ArrowUpward,
                    "Previous command",
                    tint = if (lastCmd.isNotBlank()) Teal else Color(0xFF3A3A4A),
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                ">",
                color = Teal,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = Color(0xFFCDD9E5),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { runCmd(input.trim()) }),
                singleLine = true,
                cursorBrush = SolidColor(Teal),
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) {
                            Text(
                                "Type JS expression…",
                                color = Color(0xFF6272A4),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        inner()
                    }
                },
            )
            TextButton(
                onClick = { runCmd(input.trim()) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text("Run", color = Teal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Repeat tab ───────────────────────────────────────────────────────────────

private val tfColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Teal,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    cursorColor = Teal,
    focusedLabelColor = Teal,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
private val monoStyle @Composable get() = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)

@Composable
private fun RepeaterContent(
    rawRequest: String,
    isRepeaterWizard: Boolean,
    urlInput: String,
    repeaterResponse: RepeaterResponse?,
    isSendingRequest: Boolean,
    wizardMethod: String,
    wizardUrl: String,
    wizardHeaders: List<Pair<String, String>>,
    wizardBody: String,
    wizardSent: Boolean,
    isFullScreen: Boolean,
    buildTemplate: () -> String,
    onRawRequestChanged: (String) -> Unit,
    onToggleRepeaterMode: () -> Unit,
    onSendRaw: (String) -> Unit,
    onSendParts: (String, String, Map<String, String>, String) -> Unit,
    onRepeaterReset: () -> Unit,
    onWizardMethodChanged: (String) -> Unit,
    onWizardUrlChanged: (String) -> Unit,
    onWizardHeaderAdd: (String, String) -> Unit,
    onWizardHeaderRemove: (Int) -> Unit,
    onWizardHeaderValueChanged: (Int, String) -> Unit,
    onWizardBodyChanged: (String) -> Unit,
    onWizardSentChanged: (Boolean) -> Unit,
    onToggleFullScreen: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (rawRequest.isBlank()) onRawRequestChanged(buildTemplate())
    }

    var wizardNewKey by remember { mutableStateOf("") }
    var wizardNewValue by remember { mutableStateOf("") }
    var rawExpandedPane by remember { mutableStateOf<String?>(null) }  // "req" | "res" | null
    var wizResponseExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
        // Mode toggle row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(false to "RAW", true to "WIZARD").forEach { (wiz, label) ->
                val sel = isRepeaterWizard == wiz
                Surface(
                    onClick = { if (!sel) onToggleRepeaterMode() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (sel) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (sel) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = if (sel) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.07.em,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleFullScreen, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    if (isFullScreen) "Exit fullscreen" else "Fullscreen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        if (!isRepeaterWizard) {
            // ── RAW mode: editable request (top) + response (bottom) ─────────
            if (rawExpandedPane != "res") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("REQUEST", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                    IconButton(
                        onClick = {
                            val next = if (rawExpandedPane == "req") null else "req"
                            if (next != null && !isFullScreen) onToggleFullScreen()
                            else if (next == null && isFullScreen) onToggleFullScreen()
                            rawExpandedPane = next
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(if (rawExpandedPane == "req") Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = Teal, modifier = Modifier.size(16.dp))
                    }
                }
                OutlinedTextField(
                    value = rawRequest,
                    onValueChange = onRawRequestChanged,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    enabled = !isSendingRequest,
                    colors = tfColors,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = monoStyle,
                    maxLines = Int.MAX_VALUE,
                )
                Spacer(Modifier.height(6.dp))
            }
            if (rawExpandedPane == null) {
                Button(
                    onClick = { onSendRaw(rawRequest) },
                    enabled = !isSendingRequest && rawRequest.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal, contentColor = Navy,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    if (isSendingRequest) {
                        CircularProgressIndicator(Modifier.size(14.dp), color = Navy, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Sending…", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("SEND", fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            if (rawExpandedPane != "req") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("RESPONSE", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                    IconButton(
                        onClick = {
                            val next = if (rawExpandedPane == "res") null else "res"
                            if (next != null && !isFullScreen) onToggleFullScreen()
                            else if (next == null && isFullScreen) onToggleFullScreen()
                            rawExpandedPane = next
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(if (rawExpandedPane == "res") Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = Blue, modifier = Modifier.size(16.dp))
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    RepeaterResponsePanel(response = repeaterResponse, isSending = isSendingRequest)
                }
            }
        } else {
            // ── WIZARD mode ───────────────────────────────────────────────────
            if (!wizardSent) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    // URL
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("URL", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                            OutlinedTextField(
                                value = wizardUrl,
                                onValueChange = onWizardUrlChanged,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = tfColors,
                                shape = RoundedCornerShape(8.dp),
                                textStyle = monoStyle,
                            )
                        }
                    }
                    // Method chips
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("METHOD", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "QUERY")) { m ->
                                    val sel = wizardMethod == m
                                    Surface(
                                        onClick = { onWizardMethodChanged(m) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sel) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (sel) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    ) {
                                        Text(m, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = if (sel) Teal else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                    // Headers
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("HEADERS", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                            // Quick-add chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                                items(COMMON_HEADERS) { (name, defaultVal) ->
                                    val alreadyAdded = wizardHeaders.any { it.first.equals(name, ignoreCase = true) }
                                    Surface(
                                        onClick = { if (!alreadyAdded) onWizardHeaderAdd(name, defaultVal) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (alreadyAdded) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (alreadyAdded) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                    ) {
                                        Text(
                                            text = name,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            color = if (alreadyAdded) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }
                                }
                            }
                            wizardHeaders.forEachIndexed { i, (k, v) ->
                                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(k, color = Teal, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                            BasicTextField(
                                                value = v,
                                                onValueChange = { onWizardHeaderValueChanged(i, it) },
                                                textStyle = monoStyle.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                decorationBox = { inner ->
                                                    Box(Modifier.fillMaxWidth()) {
                                                        if (v.isEmpty()) Text("(empty)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                        inner()
                                                    }
                                                },
                                            )
                                        }
                                        IconButton(onClick = { onWizardHeaderRemove(i) }, Modifier.size(28.dp)) {
                                            Icon(Icons.Filled.Delete, null, tint = Red, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                            // Add header row
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(wizardNewKey, { wizardNewKey = it }, Modifier.weight(1f), label = { Text("Name", fontSize = 11.sp) }, singleLine = true, colors = tfColors, shape = RoundedCornerShape(8.dp), textStyle = monoStyle)
                                OutlinedTextField(wizardNewValue, { wizardNewValue = it }, Modifier.weight(1f), label = { Text("Value", fontSize = 11.sp) }, singleLine = true, colors = tfColors, shape = RoundedCornerShape(8.dp), textStyle = monoStyle)
                                IconButton(
                                    onClick = { if (wizardNewKey.isNotBlank()) { onWizardHeaderAdd(wizardNewKey, wizardNewValue); wizardNewKey = ""; wizardNewValue = "" } },
                                    enabled = wizardNewKey.isNotBlank(),
                                ) { Icon(Icons.Filled.Add, null, tint = if (wizardNewKey.isNotBlank()) Teal else MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                    // Body (always visible — allow on any method)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("BODY", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                            OutlinedTextField(
                                value = wizardBody,
                                onValueChange = onWizardBodyChanged,
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                label = { Text("Request body (optional)") },
                                colors = tfColors,
                                shape = RoundedCornerShape(8.dp),
                                textStyle = monoStyle,
                                maxLines = Int.MAX_VALUE,
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        onSendParts(wizardMethod, wizardUrl, wizardHeaders.toMap(), wizardBody)
                        onWizardSentChanged(true)
                    },
                    enabled = !isSendingRequest && wizardUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Navy, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    if (isSendingRequest) {
                        CircularProgressIndicator(Modifier.size(14.dp), color = Navy, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Sending…", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("SEND", fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, fontSize = 13.sp)
                    }
                }
            } else {
                // Collapsed wizard: compact summary card + response
                if (!wizResponseExpanded) Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(color = Teal.copy(alpha = 0.18f), shape = RoundedCornerShape(4.dp)) {
                                    Text(wizardMethod, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Text(wizardUrl.removePrefix("https://").removePrefix("http://"), color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1, modifier = Modifier.weight(1f))
                            }
                            Text(
                                text = "${wizardHeaders.size} header${if (wizardHeaders.size != 1) "s" else ""}${if (wizardBody.isNotBlank()) " · body" else ""}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                            )
                        }
                        IconButton(onClick = { onWizardSentChanged(false) }, Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Settings, "Edit request", tint = Teal, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onRepeaterReset() }, Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, "New request", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (!wizResponseExpanded) Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("RESPONSE", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                    IconButton(
                        onClick = {
                            val next = !wizResponseExpanded
                            if (next && !isFullScreen) onToggleFullScreen()
                            else if (!next && isFullScreen) onToggleFullScreen()
                            wizResponseExpanded = next
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(if (wizResponseExpanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = Blue, modifier = Modifier.size(16.dp))
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    RepeaterResponsePanel(response = repeaterResponse, isSending = isSendingRequest)
                }
            }
        }
    }
}

@Composable
private fun RepeaterResponsePanel(
    response: RepeaterResponse?,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    var showFullBody by remember(response) { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isSending -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = Teal, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        Text("Sending…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            response == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Send a request to see the response",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
            response.error != null -> {
                Surface(Modifier.fillMaxWidth(), color = Red.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                    SelectionContainer {
                        Text("Error: ${response.error}", color = Red, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            else -> {
                val statusColor = when {
                    response.statusCode in 200..299 -> Green
                    response.statusCode in 300..399 -> Blue
                    response.statusCode in 400..499 -> Red.copy(alpha = 0.85f)
                    response.statusCode >= 500 -> Red
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val bodyText = if (showFullBody) response.fullBody else response.body
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    SelectionContainer {
                        Column {
                            // Status line
                            Text(response.statusLine, color = statusColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            // Response headers
                            response.headers.forEach { (k, v) ->
                                Text("$k: $v", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(Modifier.height(8.dp))
                    if (bodyText.isNotBlank()) {
                        SelectionContainer {
                            Text(bodyText, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                        }
                        if (response.isBodyTruncated && !showFullBody) {
                            Spacer(Modifier.height(4.dp))
                            TextButton(onClick = { showFullBody = true }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                                Text(
                                    "Show all (${response.fullBody.length / 1024}KB)",
                                    color = Teal,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    } else {
                        Text("(empty body)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ─── Settings bottom sheet ────────────────────────────────────────────────────

private val UA_PRESETS = listOf(
    "Android Chrome" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
    "Chrome Desktop" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Firefox Desktop" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
    "Safari (macOS)" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15",
    "Googlebot" to "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
    "Custom" to "",
)

private val TIMEOUT_OPTIONS = listOf(10, 15, 20, 30)

@Composable
private fun SettingsSheet(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val userAgent by AppPreferences.userAgentFlow(context).collectAsStateWithLifecycle(DEFAULT_UA)
    val followRedirects by AppPreferences.followRedirectsFlow(context).collectAsStateWithLifecycle(true)
    val scanTimeout by AppPreferences.scanTimeoutFlow(context).collectAsStateWithLifecycle(DEFAULT_SCAN_TIMEOUT_S)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            listOf("SETTINGS", "HELP", "ABOUT").forEachIndexed { i, label ->
                val selected = i == selectedTab
                TextButton(
                    onClick = { selectedTab = i },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.08.em,
                    )
                }
                if (selected) {
                    // underline indicator — drawn via box below
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        when (selectedTab) {
            0 -> {
                val saveHtmlSource by AppPreferences.saveHtmlSourceFlow(context).collectAsStateWithLifecycle(false)
                val fontScale by AppPreferences.fontScaleFlow(context).collectAsStateWithLifecycle(DEFAULT_FONT_SCALE)
                SettingsTabContent(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    userAgent = userAgent,
                    followRedirects = followRedirects,
                    scanTimeout = scanTimeout,
                    saveHtmlSource = saveHtmlSource,
                    fontScale = fontScale,
                    onUaChange = { scope.launch { AppPreferences.setUserAgent(context, it) } },
                    onFollowRedirectsChange = { scope.launch { AppPreferences.setFollowRedirects(context, it) } },
                    onTimeoutChange = { scope.launch { AppPreferences.setScanTimeout(context, it) } },
                    onSaveHtmlSourceChange = { scope.launch { AppPreferences.setSaveHtmlSource(context, it) } },
                    onFontScaleChange = { scope.launch { AppPreferences.setFontScale(context, it) } },
                )
            }
            1 -> HelpTabContent()
            2 -> AboutTabContent()
        }
    }
}

@Composable
private fun SettingsTabContent(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    userAgent: String,
    followRedirects: Boolean,
    scanTimeout: Int,
    saveHtmlSource: Boolean,
    fontScale: Float,
    onUaChange: (String) -> Unit,
    onFollowRedirectsChange: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onSaveHtmlSourceChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
) {
    val selectedPreset = UA_PRESETS.indexOfFirst { it.second == userAgent }
        .let { if (it < 0) UA_PRESETS.lastIndex else it } // "Custom" if no match

    var customUaText by remember(userAgent) {
        mutableStateOf(if (selectedPreset == UA_PRESETS.lastIndex) userAgent else "")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Dark mode
        SettingRow(label = "Appearance") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp),
                )
                Text(if (isDarkTheme) "Dark" else "Light", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Navy, checkedTrackColor = Teal,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        }

        // Text size (applies app-wide via a font-scale override)
        Spacer(Modifier.height(8.dp))
        SettingRow(label = "Text size") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Small" to 0.9f, "Default" to 1.0f, "Large" to 1.15f).forEach { (name, scale) ->
                    val sel = kotlin.math.abs(fontScale - scale) < 0.01f
                    Surface(
                        onClick = { onFontScaleChange(scale) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (sel) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (sel) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            name, fontSize = 11.sp,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (sel) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

        // User Agent
        Text("User Agent", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(UA_PRESETS) { i, (name, ua) ->
                val isSelected = i == selectedPreset
                Surface(
                    onClick = {
                        if (i != UA_PRESETS.lastIndex) {
                            onUaChange(ua)
                            customUaText = ""
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Text(
                        name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        if (selectedPreset == UA_PRESETS.lastIndex || (selectedPreset >= 0 && selectedPreset < UA_PRESETS.lastIndex).not()) {
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = customUaText,
                onValueChange = {
                    customUaText = it
                    onUaChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Custom user-agent string…", fontSize = 12.sp) },
                textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
                maxLines = 3,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

        // Follow redirects
        SettingRow(label = "Follow redirects") {
            Switch(
                checked = followRedirects,
                onCheckedChange = onFollowRedirectsChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Navy, checkedTrackColor = Teal,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
        Text(
            "When disabled, scan stops at the first redirect and fingerprints that response.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

        // Scan timeout
        Text("Scan timeout (current: ${scanTimeout}s)", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TIMEOUT_OPTIONS.forEach { t ->
                val isSelected = t == scanTimeout
                Surface(
                    onClick = { onTimeoutChange(t) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Teal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Text(
                        "${t}s",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Teal else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

        // Save HTML source
        SettingRow(label = "Save HTML source") {
            Switch(
                checked = saveHtmlSource,
                onCheckedChange = onSaveHtmlSourceChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Navy, checkedTrackColor = Teal,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
        Text(
            "Include raw HTML in saved scans (~500KB extra per scan). Off by default.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun HelpTabContent() {
    val helpItems = listOf(
        "No results after scan" to "The site may block automated browsers or require authentication. Try a different URL or disable redirect following.",
        "Console SyntaxError" to "Use single-line JS expressions: document.title, window.location.href. Multi-statement eval now supported — try: let x = 5; x",
        "Certificate / TLS errors" to "The site has an invalid or self-signed certificate. The scan continues with limited data; TLS section will show the error.",
        "App slows after many scans" to "Tap New Scan (+) to reset WebView state between sessions.",
        "Share to Ecliptic missing" to "Open another app's share sheet, scroll to find Ecliptic. If missing, the app may need to be the target of a share intent at least once.",
        "Shopify / WooCommerce not detected" to "Run the scan on the store's main product or home page, not a blog sub-path. Platform fingerprints exist in page globals.",
        "White background flash" to "Occurs when switching from WebView-heavy screens. Fixed in v0.3.x — update if still happening.",
        "Report export opens wrong app" to "The .html report file is shared via the system sheet. Tap 'Open with…' and choose Chrome or Firefox for best rendering.",
        "Report to PDF" to "Open the .html report in Chrome → tap ⋮ → Print → Save as PDF.",
        "User-Agent not applied" to "The User-Agent is applied on the next scan. If you changed it, run a new scan.",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        helpItems.forEach { (q, a) ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(q, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(a, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AboutTabContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "ECLIPTIC",
            style = TextStyle(
                fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
                fontSize = 28.sp, letterSpacing = 0.12.em, color = Teal,
            ),
        )
        Text(text = "v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Text("Website Technology Scanner", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Detects CMS, frameworks, analytics, CDN, and 1,500+ other technologies using an embedded browser and pattern matching against the Wappalyzer open-source database.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(Modifier.height(8.dp))
        AboutInfoRow("Fingerprint DB", "enthec/webappanalyzer")
        AboutInfoRow("Detection layers", "HTTP · HTML · JS · DOM · Cookies · Network")
        AboutInfoRow("Min Android", "8.0 (API 26)")
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(Modifier.height(12.dp))

        Text(
            "PRIVACY",
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.em, color = Teal,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "No accounts, no analytics, no telemetry. Scans are stored only on " +
                "this device. Scanning a site contacts that site plus DNS, RDAP/WHOIS " +
                "and IP-info lookups, and loads technology icons and fonts from public CDNs.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
        )
        Spacer(Modifier.height(10.dp))
        AboutLinkRow("Privacy Policy", "https://naturalstate.github.io/ecliptic/privacy.html")
        Spacer(Modifier.height(8.dp))
        AboutLinkRow("Website", "https://naturalstate.github.io/ecliptic/")
        Spacer(Modifier.height(8.dp))
        AboutLinkRow("GitHub", "https://github.com/naturalstate/ecliptic")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AboutLinkRow(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { runCatching { uriHandler.openUri(url) } },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.OpenInBrowser, null, tint = Teal, modifier = Modifier.size(16.dp))
            Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
    }
}
