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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.ecliptic.BuildConfig
import com.ecliptic.ScanViewModel
import com.ecliptic.fingerprint.Technology
import com.ecliptic.scanner.DnsResult
import com.ecliptic.scanner.RawSignals
import com.ecliptic.scanner.RdapResult
import com.ecliptic.scanner.RepeaterResponse
import com.ecliptic.scanner.TlsResult
import org.json.JSONArray
import org.json.JSONObject
import com.ecliptic.scanner.configureWebView
import com.ecliptic.scanner.fetchDnsRecords
import com.ecliptic.scanner.fetchRdapInfo
import com.ecliptic.scanner.fetchTextContent
import com.ecliptic.ui.DevIconMapper
import com.ecliptic.ui.theme.Blue
import com.ecliptic.ui.theme.Green
import com.ecliptic.ui.theme.Navy
import com.ecliptic.ui.theme.NavyLight
import com.ecliptic.ui.theme.Red
import com.ecliptic.ui.theme.SpaceGrotesk
import com.ecliptic.ui.theme.Teal
import com.ecliptic.ui.theme.categoryAccentColor
import kotlinx.coroutines.launch

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

private val SECURITY_HEADERS = linkedMapOf(
    "content-security-policy"   to "CSP",
    "strict-transport-security" to "HSTS",
    "x-frame-options"           to "X-Frame-Options",
    "x-content-type-options"    to "X-Content-Type-Options",
    "referrer-policy"           to "Referrer-Policy",
    "permissions-policy"        to "Permissions-Policy",
    "cross-origin-opener-policy" to "COOP",
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

    var bottomTab    by remember { mutableIntStateOf(TAB_TECH) }
    var intelSubTab  by remember { mutableIntStateOf(0) }   // 0=HEADERS 1=COOKIES 2=NETWORK
    var codeSubTab   by remember { mutableIntStateOf(0) }   // 0=SOURCE  1=JS      2=DOM
    var showSettings by remember { mutableStateOf(false) }
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

    fun shareResults() {
        val sb = StringBuilder()
        sb.appendLine("Ecliptic scan: ${signals?.finalUrl ?: urlInput}")
        sb.appendLine()
        sb.appendLine("${techs.size} technologies detected")
        sb.appendLine()
        val grouped = techs.groupBy { it.categories.firstOrNull() ?: "Other" }.toSortedMap()
        for ((cat, list) in grouped) {
            sb.appendLine(cat)
            for (tech in list.sortedBy { it.name }) {
                val v = tech.version?.let { " $it" } ?: ""
                sb.appendLine("  • ${tech.name}$v")
            }
            sb.appendLine()
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
        }
        context.startActivity(Intent.createChooser(intent, "Share scan results"))
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer(rotationZ = 45f)
                            .background(Teal, RoundedCornerShape(1.5.dp))
                    )
                    Text(
                        text = "ECLIPTIC",
                        style = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.18.em,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasScanned && !scanning) {
                        IconButton(onClick = { shareResults() }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Share, "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
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

        // ── URL input (hidden in fullscreen) ──────────────────────────────────
        if (!isFullScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp, end = 16.dp,
                        top = if (isLandscape) 6.dp else 0.dp,
                        bottom = if (isLandscape) 4.dp else 0.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { viewModel.onUrlChanged(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("URL", fontFamily = SpaceGrotesk) },
                    singleLine = true,
                    enabled = !scanning,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { scan() }),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onUrlChanged("") }, enabled = !scanning, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                }
                            }
                            IconButton(onClick = { paste() }, enabled = !scanning, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.ContentPaste, "Paste", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = Teal,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = Teal,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp),
                )
                Button(
                    onClick = { scan() },
                    enabled = !scanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal, contentColor = Navy,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp).padding(bottom = 1.dp),
                ) {
                    if (scanning) {
                        CircularProgressIndicator(modifier = Modifier.size(17.dp), color = Navy, strokeWidth = 2.dp)
                    } else {
                        Text("SCAN", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Sub-tab chips (INTEL / CODE only, hidden in fullscreen) ──────────
        if (!isFullScreen && hasScanned && !scanning && (bottomTab == TAB_INTEL || bottomTab == TAB_CODE)) {
            val subLabels = if (bottomTab == TAB_INTEL) listOf("HEADERS", "COOKIES", "NETWORK")
                            else listOf("SOURCE", "JS", "DOM")
            val currentSub = if (bottomTab == TAB_INTEL) intelSubTab else codeSubTab
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                subLabels.forEachIndexed { i, label ->
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
                    "tech"    -> TechResultsContent(techs = techs, error = signals?.error, url = signals?.finalUrl ?: "", onTechClick = { selectedTech = it })
                    "intel_0" -> HeadersContent(requestHeaders = signals?.requestHeaders ?: emptyMap(), responseHeaders = signals?.headers ?: emptyMap())
                    "intel_1" -> CookiesContent(cookies = signals?.cookies ?: emptyMap())
                    "intel_2" -> NetworkContent(url = signals?.finalUrl ?: "", html = signals?.htmlSource ?: "", tls = signals?.tls)
                    "code_0"  -> SourceContent(html = signals?.htmlSource ?: "", scriptUrls = signals?.scriptUrls ?: emptyList(), cssUrls = signals?.cssUrls ?: emptyList(), isFullScreen = isFullScreen, onToggleFullScreen = { isFullScreen = !isFullScreen })
                    "code_1"  -> JsGlobalsContent(globals = signals?.globals ?: emptyMap(), jsProbe = signals?.jsProbe ?: emptyMap())
                    "code_2"  -> DomContent(domTreeJson = signals?.domTree ?: "", isFullScreen = isFullScreen, onToggleFullScreen = { isFullScreen = !isFullScreen })
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
                val techBadge = techs.size.takeIf { hasScanned && it > 0 }
                val intelBadge = (signals?.headers?.filterKeys { it != "_status" && it != "_header_error" }?.size ?: 0)
                    .takeIf { hasScanned && it > 0 }

                listOf(
                    Triple(TAB_TECH,   Icons.Filled.Layers,   "Tech"),
                    Triple(TAB_INTEL,  Icons.Filled.Shield,   "Intel"),
                    Triple(TAB_CODE,   Icons.Filled.Code,     "Code"),
                    Triple(TAB_REPEAT, Icons.AutoMirrored.Filled.Send, "Repeat"),
                ).forEach { (idx, icon, label) ->
                    val badge = when (idx) { TAB_TECH -> techBadge; TAB_INTEL -> intelBadge; else -> null }
                    NavigationBarItem(
                        selected = bottomTab == idx,
                        onClick = { bottomTab = idx },
                        icon = {
                            if (badge != null) {
                                BadgedBox(badge = {
                                    Badge(containerColor = Teal, contentColor = Navy) {
                                        Text("$badge", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }) {
                                    Icon(icon, label, modifier = Modifier.size(22.dp))
                                }
                            } else {
                                Icon(icon, label, modifier = Modifier.size(22.dp))
                            }
                        },
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

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(),
        ) {
            AboutContent(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
        }
    }

    selectedTech?.let { tech ->
        TechDetailSheet(tech = tech, onDismiss = { selectedTech = null })
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
            Text(text = url, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
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
                TextButton(
                    onClick = { runCatching { uriHandler.openUri("https://web.archive.org/web/*/$url") } },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) {
                    Text("Archive.org →", color = Blue.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        grouped.forEach { (category, list) ->
            item { CategoryHeader(category = category, count = list.size) }
            items(list.sortedBy { it.name }) { tech ->
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
private fun TechDetailSheet(tech: Technology, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState()
    val accentColor = categoryAccentColor(tech.categories.firstOrNull() ?: "") ?: Teal
    val iconTint = MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
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
    val slug = remember(techName) { DevIconMapper.slugFor(techName) }
    var showFallback by remember(slug) { mutableStateOf(slug == null) }

    if (showFallback) {
        TechIconFallback(techName = techName, tint = tint, modifier = modifier)
    } else {
        AsyncImage(
            model = DevIconMapper.iconUrl(slug!!),
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
        headerError?.let {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = Red.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text("⚠  $it", modifier = Modifier.padding(12.dp), color = Red, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        item {
            SecurityHeadersSection(responseHeaders = resHeaders)
            Spacer(modifier = Modifier.height(12.dp))
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
private fun CookiesContent(cookies: Map<String, String>) {
    if (cookies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No cookies detected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("COOKIES", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Teal.copy(alpha = 0.25f))
                Text("${cookies.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
        items(cookies.entries.toList().sortedBy { it.key }) { (name, value) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(text = name, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    if (value.isNotBlank()) {
                        Text(
                            text = value.take(120) + if (value.length > 120) "…" else "",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
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
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
) {
    var selectedFile by remember { mutableStateOf("__html__") }
    var remoteContents by remember { mutableStateOf(mapOf<String, String>()) }
    var remoteLoading by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatchIdx by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search…", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, null, modifier = Modifier.size(14.dp))
                    } }
                } else null,
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    cursorColor = Teal,
                ),
                shape = RoundedCornerShape(8.dp),
            )
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
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
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
                            fontSize = 10.sp,
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
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
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
private fun NetworkContent(url: String, html: String, tls: TlsResult? = null) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val domain = remember(url) {
        try { java.net.URI(url).host?.removePrefix("www.") } catch (_: Exception) { null }
    }
    val rdapDomain = remember(domain) {
        domain?.split('.')?.takeLast(2)?.joinToString(".")
    }

    var dnsResult by remember { mutableStateOf<DnsResult?>(null) }
    var rdapResult by remember { mutableStateOf<RdapResult?>(null) }
    var dnsLoading by remember { mutableStateOf(false) }
    var rdapLoading by remember { mutableStateOf(false) }

    val socialLinks = remember(html) { extractSocialLinks(html) }

    LaunchedEffect(domain) {
        if (domain == null) return@LaunchedEffect
        dnsLoading = true
        rdapLoading = true
        scope.launch {
            dnsResult = fetchDnsRecords(domain)
            dnsLoading = false
        }
        scope.launch {
            rdapResult = fetchRdapInfo(rdapDomain ?: domain)
            rdapLoading = false
        }
    }

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
            Text(domain, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
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
            rdapResult != null -> {
                val r = rdapResult!!
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            r.registrar?.let { NetKvRow("Registrar", it) }
                            r.registrant?.let { NetKvRow("Registrant", it) }
                            r.created?.let { NetKvRow("Created", it) }
                            r.expires?.let { NetKvRow("Expires", it) }
                            r.updated?.let { NetKvRow("Updated", it) }
                            if (r.status.isNotEmpty()) {
                                NetKvRow("Status", r.status.joinToString(", "))
                            }
                            if (r.nameServers.isNotEmpty()) {
                                NetKvRow("Name servers", r.nameServers.joinToString("\n"))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

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
    val items = remember(expandedIds) { flattenDom(root, expandedIds) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                    Text("<${item.tag}>", color = SH_TAG, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                    if (item.attrSummary.isNotBlank()) {
                        Text(
                            text = " ${item.attrSummary}",
                            color = SH_ATTR_NAME,
                            fontSize = 11.sp,
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
            Text("REQUEST", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em, modifier = Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = rawRequest,
                onValueChange = onRawRequestChanged,
                modifier = Modifier.fillMaxWidth().weight(0.42f),
                enabled = !isSendingRequest,
                colors = tfColors,
                shape = RoundedCornerShape(10.dp),
                textStyle = monoStyle,
                maxLines = Int.MAX_VALUE,
            )
            Spacer(Modifier.height(6.dp))
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
            Text("RESPONSE", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em, modifier = Modifier.padding(bottom = 4.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(0.58f)) {
                RepeaterResponsePanel(response = repeaterResponse, isSending = isSendingRequest)
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
                                items(listOf("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")) { m ->
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
                Surface(
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
                Spacer(Modifier.height(6.dp))
                Text("RESPONSE", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.em, modifier = Modifier.padding(bottom = 4.dp))
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

@Composable
private fun AboutContent(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "ECLIPTIC",
            style = TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 0.12.em,
                color = Teal,
            ),
        )
        Text(text = "v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Website Technology Scanner", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Detects CMS, frameworks, analytics, CDN, and 1,500+ other technologies using an embedded browser and pattern matching against the Wappalyzer open-source database.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(text = if (isDarkTheme) "Dark mode" else "Light mode", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onToggleTheme() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Navy,
                    checkedTrackColor = Teal,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(8.dp))

        AboutRow(label = "Fingerprint DB", value = "enthec/webappanalyzer")
        AboutRow(label = "Detection layers", value = "HTTP · HTML · JS · DOM · Cookies")
        AboutRow(label = "Min Android", value = "8.0 (API 26)")
    }
}


@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
    }
}
