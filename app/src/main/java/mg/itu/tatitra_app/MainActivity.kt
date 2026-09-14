package mg.itu.tatitra_app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mg.itu.tatitra_app.data.local.PreferencesDataStore
import mg.itu.tatitra_app.ui.navigation.TatitraApp
import mg.itu.tatitra_app.ui.theme.TatitraappTheme
import java.util.Locale

/**
 * Unique Activity de l'application : elle héberge le graphe de navigation Compose.
 * L'état métier vit dans les ViewModels et dans Room, jamais ici : une rotation
 * ou une recréation d'Activity ne perd donc aucune donnée (NF-11).
 *
 * J4 : la langue stockée dans DataStore est relue au démarrage.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val langue = (application as TatitraApplication)
                .container
                .preferencesRepository
                .observerLangue()
                .first()
            appliquerLocale(this@MainActivity, langue)
        }

        enableEdgeToEdge()
        setContent {
            TatitraappTheme {
                TatitraApp()
            }
        }
    }
}

/** Applique la locale préférée (fr / mg) pour la session courante. */
fun appliquerLocale(context: Context, codeLangue: String) {
    val locale = when (codeLangue) {
        PreferencesDataStore.LANGUE_MG -> Locale("mg", "MG")
        else -> Locale.FRANCE
    }
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
