package mg.itu.tatitra_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mg.itu.tatitra_app.ui.home.EcranAccueilRoute
import mg.itu.tatitra_app.ui.report.NouveauSignalementRoute
import mg.itu.tatitra_app.ui.reports.EcranDetailSignalementRoute
import mg.itu.tatitra_app.ui.reports.EcranMesSignalementsRoute

/**
 * Graphe de navigation de l'application (S5).
 *
 * « Mes signalements » / détail (J3) : données Room réelles (photo, statut, GPS).
 */
@Composable
fun TatitraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DestinationsTatitra.ACCUEIL,
        modifier = modifier
    ) {
        composable(DestinationsTatitra.ACCUEIL) {
            EcranAccueilRoute(
                onNouveauSignalement = {
                    navController.navigate(DestinationsTatitra.NOUVEAU_SIGNALEMENT)
                },
                onVoirMesSignalements = {
                    navController.navigate(DestinationsTatitra.SIGNALEMENTS)
                },
                onOuvrirSignalement = { idLocal ->
                    navController.navigate(DestinationsTatitra.detailSignalement(idLocal))
                }
            )
        }

        composable(DestinationsTatitra.NOUVEAU_SIGNALEMENT) {
            NouveauSignalementRoute(
                onRetour = { navController.popBackStack() },
                onSignalementEnregistre = {
                    // Retour à l'accueil : le nouveau signalement y apparaît en attente de synchronisation.
                    navController.popBackStack()
                }
            )
        }

        composable(DestinationsTatitra.SIGNALEMENTS) {
            EcranMesSignalementsRoute(
                onRetour = { navController.popBackStack() },
                onOuvrirSignalement = { idLocal ->
                    navController.navigate(DestinationsTatitra.detailSignalement(idLocal))
                }
            )
        }

        composable(
            route = DestinationsTatitra.DETAIL_SIGNALEMENT,
            arguments = listOf(
                navArgument(DestinationsTatitra.ARGUMENT_ID_LOCAL) { type = NavType.StringType }
            )
        ) { entree ->
            val idLocal = entree.arguments?.getString(DestinationsTatitra.ARGUMENT_ID_LOCAL).orEmpty()
            EcranDetailSignalementRoute(
                idLocal = idLocal,
                onRetour = { navController.popBackStack() }
            )
        }
    }
}
