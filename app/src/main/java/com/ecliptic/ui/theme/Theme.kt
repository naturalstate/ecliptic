package com.ecliptic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EclipticDarkColors = darkColorScheme(
    primary            = Teal,
    onPrimary          = Navy,
    secondary          = Blue,
    onSecondary        = White,
    tertiary           = NavyLight,
    background         = Navy,          // 0x0A0A0A
    onBackground       = White,
    surface            = Surface1,      // 0x111111
    onSurface          = White,
    surfaceVariant     = Surface2,      // 0x1C1C1C
    onSurfaceVariant   = TextMuted,
    outline            = NavyLight,     // 0x2A2A2A
    error              = Red,
    onError            = White,
)

private val EclipticLightColors = lightColorScheme(
    primary            = Teal,
    onPrimary          = White,
    secondary          = Blue,
    onSecondary        = White,
    tertiary           = LightOutline,
    background         = LightBg,
    onBackground       = Surface1,
    surface            = LightSurface,
    onSurface          = Surface1,
    surfaceVariant     = LightSurface2,
    onSurfaceVariant   = LightTextMuted,
    outline            = LightOutline,
    error              = Red,
    onError            = White,
)

@Composable
fun EclipticTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EclipticDarkColors else EclipticLightColors,
        typography = Typography,
        content = content,
    )
}
