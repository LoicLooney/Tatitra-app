package mg.itu.tatitra_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mg.itu.tatitra_app.ui.navigation.TatitraApp
import mg.itu.tatitra_app.ui.theme.TatitraappTheme

/**
 * Unique Activity de l'application : elle héberge le graphe de navigation Compose.
 * L'état métier vit dans les ViewModels et dans Room, jamais ici : une rotation
 * ou une recréation d'Activity ne perd donc aucune donnée (NF-11).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TatitraappTheme {
                TatitraApp()
            }
        }
    }
}
