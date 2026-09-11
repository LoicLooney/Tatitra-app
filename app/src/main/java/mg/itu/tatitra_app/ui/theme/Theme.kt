package mg.itu.tatitra_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SchemaClair = lightColorScheme(
    primary = BleuTatitra,
    onPrimary = Color.White,
    primaryContainer = BleuTatitraClair,
    onPrimaryContainer = BleuTatitraFonce,
    secondary = BleuTatitraFonce,
    onSecondary = Color.White,
    tertiary = JauneTatitra,
    onTertiary = NoirTatitra,
    background = GrisSurface,
    onBackground = NoirTatitra,
    surface = Color.White,
    onSurface = NoirTatitra,
    surfaceVariant = BleuTatitraClair,
    onSurfaceVariant = GrisTexte,
    outline = GrisBordure,
    error = RougeRejet
)

private val SchemaSombre = darkColorScheme(
    primary = BleuTatitraNuit,
    onPrimary = BleuTatitraFonce,
    primaryContainer = BleuTatitraFonce,
    onPrimaryContainer = BleuTatitraClair,
    secondary = BleuTatitraClair,
    tertiary = JauneTatitra,
    background = NoirTatitra,
    onBackground = GrisSurface,
    surface = SurfaceNuit,
    onSurface = GrisSurface,
    outline = GrisTexte,
    error = RougeRejet
)

/**
 * Thème de l'application. La couleur dynamique Android 12+ est volontairement absente :
 * l'identité visuelle TATITRA doit rester identique d'un téléphone à l'autre pendant la démo.
 */
@Composable
fun TatitraappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) SchemaSombre else SchemaClair,
        typography = Typography,
        content = content
    )
}
