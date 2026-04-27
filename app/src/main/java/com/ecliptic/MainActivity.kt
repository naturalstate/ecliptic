package com.ecliptic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ecliptic.poc.PocScanScreen
import com.ecliptic.ui.theme.EclipticTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var sharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedUrl = extractUrlFromIntent(intent)

        setContent {
            val darkTheme by AppPreferences.darkThemeFlow(applicationContext)
                .collectAsStateWithLifecycle(initialValue = true)

            // Keep window background in sync with theme — prevents flash on resume
            SideEffect {
                val bgColor = if (darkTheme) 0xFF0A0A0A.toInt() else 0xFFF5F5F5.toInt()
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
            }

            EclipticTheme(darkTheme = darkTheme) {
                PocScanScreen(
                    initialUrl = sharedUrl,
                    isDarkTheme = darkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            AppPreferences.setDarkTheme(applicationContext, !darkTheme)
                        }
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUrl = extractUrlFromIntent(intent)
    }

    private fun extractUrlFromIntent(intent: Intent): String? = when (intent.action) {
        Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
        Intent.ACTION_VIEW -> intent.dataString
        else -> null
    }
}
