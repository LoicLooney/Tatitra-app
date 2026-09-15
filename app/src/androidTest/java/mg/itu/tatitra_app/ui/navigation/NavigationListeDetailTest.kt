package mg.itu.tatitra_app.ui.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * J7 — back stack liste → détail → retour.
 * Graphe minimal (sans Room) pour isoler la navigation.
 */
@RunWith(AndroidJUnit4::class)
class NavigationListeDetailTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun listeVersDetailPuisRetour_restaureLaListe() {
        composeRule.setContent {
            GrapheListeDetail()
        }

        composeRule.onNodeWithTag("ecran_liste").assertIsDisplayed()
        composeRule.onNodeWithText("Ouvrir détail").performClick()
        composeRule.onNodeWithTag("ecran_detail").assertIsDisplayed()
        composeRule.onNodeWithText("Retour").performClick()
        composeRule.onNodeWithTag("ecran_liste").assertIsDisplayed()
    }

    @Test
    fun detailVersConfirmationPuisRetour_restaureLeDetail() {
        composeRule.setContent {
            GrapheListeDetail()
        }

        composeRule.onNodeWithText("Ouvrir détail").performClick()
        composeRule.onNodeWithText("Confirmer résolution").performClick()
        composeRule.onNodeWithTag("ecran_confirmation").assertIsDisplayed()
        composeRule.onNodeWithText("Retour").performClick()
        composeRule.onNodeWithTag("ecran_detail").assertIsDisplayed()
    }
}

@Composable
private fun GrapheListeDetail() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = DestinationsTatitra.SIGNALEMENTS
    ) {
        composable(DestinationsTatitra.SIGNALEMENTS) {
            EcranTest(
                tag = "ecran_liste",
                titre = "Liste",
                actions = listOf(
                    "Ouvrir détail" to {
                        navController.navigate(DestinationsTatitra.detailSignalement("id-demo")) {
                            launchSingleTop = true
                        }
                    }
                )
            )
        }
        composable(
            route = DestinationsTatitra.DETAIL_SIGNALEMENT,
            arguments = listOf(
                navArgument(DestinationsTatitra.ARGUMENT_ID_LOCAL) { type = NavType.StringType }
            )
        ) {
            EcranTest(
                tag = "ecran_detail",
                titre = "Détail",
                actions = listOf(
                    "Confirmer résolution" to {
                        navController.navigate(
                            DestinationsTatitra.confirmationResolution("id-demo")
                        ) {
                            launchSingleTop = true
                        }
                    },
                    "Retour" to { navController.popBackStack() }
                )
            )
        }
        composable(
            route = DestinationsTatitra.CONFIRMATION_RESOLUTION,
            arguments = listOf(
                navArgument(DestinationsTatitra.ARGUMENT_ID_LOCAL) { type = NavType.StringType }
            )
        ) {
            EcranTest(
                tag = "ecran_confirmation",
                titre = "Confirmation",
                actions = listOf("Retour" to { navController.popBackStack() })
            )
        }
    }
}

@Composable
private fun EcranTest(
    tag: String,
    titre: String,
    actions: List<Pair<String, () -> Unit>>
) {
    androidx.compose.foundation.layout.Column(Modifier = Modifier.testTag(tag)) {
        Text(titre)
        actions.forEach { (libelle, action) ->
            Button(onClick = action) { Text(libelle) }
        }
    }
}
