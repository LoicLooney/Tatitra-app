package mg.itu.tatitra_app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mg.itu.tatitra_app.ui.home.EcranAccueilRoute
import mg.itu.tatitra_app.ui.report.NouveauSignalementRoute

/**
 * Graphe de navigation de l'application (S5).
 *
 * Les écrans « Mes signalements » et « Détail » appartiennent au parcours de
 * consultation (Membre B) : leurs routes existent déjà pour que la navigation
 * complète soit testable, le contenu sera branché dans ui/reports/.
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
            EcranABrancher(
                titre = "Mes signalements",
                detail = "Liste des signalements locaux et synchronisés (parcours de consultation)."
            )
        }

        composable(
            route = DestinationsTatitra.DETAIL_SIGNALEMENT,
            arguments = listOf(
                navArgument(DestinationsTatitra.ARGUMENT_ID_LOCAL) { type = NavType.StringType }
            )
        ) { entree ->
            val idLocal = entree.arguments?.getString(DestinationsTatitra.ARGUMENT_ID_LOCAL).orEmpty()
            EcranABrancher(
                titre = "Détail du signalement",
                detail = "Signalement $idLocal : statut, photo, position et actions de résolution."
            )
        }
    }
}

/**
 * Écran d'attente affiché à la place d'un écran non encore développé.
 * À remplacer par les composables de ui/reports/ (Membre B).
 */
@Composable
private fun EcranABrancher(
    titre: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = titre, style = MaterialTheme.typography.titleLarge)
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Écran en cours de développement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
