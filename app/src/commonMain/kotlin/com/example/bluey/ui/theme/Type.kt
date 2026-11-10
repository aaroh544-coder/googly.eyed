package com.example.bluey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.bluey.resources.Res
import com.example.bluey.resources.fredoka
import com.example.bluey.resources.ibm_plex_mono
import com.example.bluey.resources.ibm_plex_sans
import org.jetbrains.compose.resources.Font

@Immutable
data class BlueyFonts(
    val fredoka: FontFamily,
    val plexSans: FontFamily,
    val plexMono: FontFamily
)

internal val LocalBlueyFonts = staticCompositionLocalOf {
    BlueyFonts(FontFamily.Default, FontFamily.Default, FontFamily.Default)
}

val Fredoka: FontFamily
    @Composable @ReadOnlyComposable get() = LocalBlueyFonts.current.fredoka

val IBMPlexMono: FontFamily
    @Composable @ReadOnlyComposable get() = LocalBlueyFonts.current.plexMono

val IBMPlexSans: FontFamily
    @Composable @ReadOnlyComposable get() = LocalBlueyFonts.current.plexSans

@Composable
fun blueyFonts(): BlueyFonts {
    val fredokaFamily = FontFamily(
        Font(Res.font.fredoka, FontWeight.Normal),
        Font(Res.font.fredoka, FontWeight.Bold)
    )
    val monoFamily = FontFamily(
        Font(Res.font.ibm_plex_mono, FontWeight.Normal),
        Font(Res.font.ibm_plex_mono, FontWeight.Medium)
    )
    val sansFamily = FontFamily(
        Font(Res.font.ibm_plex_sans, FontWeight.Normal)
    )
    return remember(fredokaFamily, monoFamily, sansFamily) {
        BlueyFonts(fredoka = fredokaFamily, plexSans = sansFamily, plexMono = monoFamily)
    }
}

fun blueyTypography(fonts: BlueyFonts): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fonts.fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fonts.fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = fonts.fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fonts.plexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fonts.plexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fonts.plexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fonts.plexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    )
)
