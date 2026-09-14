package mg.itu.tatitra_app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/** Onglet de la barre de navigation basse. */
private data class OngletPrincipal(
    val route: String,
    val libelle: String,
    val icone: ImageVector
)

private val ONGLETS = listOf(
    OngletPrincipal(DestinationsTatitra.ACCUEIL, "Accueil", Icons.Filled.Home),
    OngletPrincipal(DestinationsTatitra.SIGNALEMENTS, "Signalements", Icons.AutoMirrored.Filled.List)
)

/**
 * Coquille de l'application : barre de navigation basse + graphe de navigation.
 * La barre disparaît sur les écrans de saisie pour laisser toute la place au formulaire.
 */
@Composable
fun TatitraApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entreeCourante by navController.currentBackStackEntryAsState()
    val destinationCourante = entreeCourante?.destination
    val barreVisible = ONGLETS.any { onglet ->
        destinationCourante?.hierarchy?.any { it.route == onglet.route } == true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (barreVisible) {
                NavigationBar {
                    ONGLETS.forEach { onglet ->
                        val selectionne =
                            destinationCourante?.hierarchy?.any { it.route == onglet.route } == true
                        NavigationBarItem(
                            selected = selectionne,
                            onClick = { naviguerVersOnglet(navController, onglet.route) },
                            icon = { Icon(onglet.icone, contentDescription = null) },
                            label = { Text(onglet.libelle) }
                        )
                    }
                }
            }
        }
    ) { paddingInterne ->
        TatitraNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingInterne)
        )
    }
}

/** Évite d'empiler indéfiniment les onglets dans la back stack (S5). */
private fun naviguerVersOnglet(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
