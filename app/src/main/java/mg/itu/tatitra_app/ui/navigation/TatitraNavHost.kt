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
import mg.itu.tatitra_app.ui.reports.EcranConfirmationResolutionRoute
import mg.itu.tatitra_app.ui.reports.EcranDetailSignalementRoute
import mg.itu.tatitra_app.ui.reports.EcranMesSignalementsRoute

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
                    navController.navigate(DestinationsTatitra.detailSignalement(idLocal)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(DestinationsTatitra.NOUVEAU_SIGNALEMENT) {
            NouveauSignalementRoute(
                onRetour = { navController.popBackStack() },
                onSignalementEnregistre = {
                    navController.popBackStack()
                }
            )
        }

        composable(DestinationsTatitra.SIGNALEMENTS) {
            EcranMesSignalementsRoute(
                onRetour = { navController.popBackStack() },
                onOuvrirSignalement = { idLocal ->
                    navController.navigate(DestinationsTatitra.detailSignalement(idLocal)) {
                        launchSingleTop = true
                    }
                },
                onOuvrirConfirmation = { idLocal ->
                    navController.navigate(
                        DestinationsTatitra.confirmationResolution(idLocal)
                    ) {
                        launchSingleTop = true
                    }
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
                onRetour = { navController.popBackStack() },
                onOuvrirConfirmation = {
                    navController.navigate(
                        DestinationsTatitra.confirmationResolution(idLocal)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = DestinationsTatitra.CONFIRMATION_RESOLUTION,
            arguments = listOf(
                navArgument(DestinationsTatitra.ARGUMENT_ID_LOCAL) { type = NavType.StringType }
            )
        ) { entree ->
            val idLocal = entree.arguments?.getString(DestinationsTatitra.ARGUMENT_ID_LOCAL).orEmpty()
            EcranConfirmationResolutionRoute(
                idLocal = idLocal,
                onRetour = { navController.popBackStack() }
            )
        }
    }
}
