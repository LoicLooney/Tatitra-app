package mg.itu.tatitra_app.ui.reports

import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.RoleResolution
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Règles d'affichage des actions de résolution côté citoyen (J5/J6).
 * C'est la traduction dans l'app de la validation croisée du §5.3 : celui qui a
 * proposé ne confirme pas lui-même.
 */
class DetailSignalementUiStateTest {

    private fun etat(
        statut: StatutSignalement,
        synchronise: Boolean = true,
        serverId: String? = "SIG-1",
        proposePar: String? = null
    ) = DetailSignalementUiState(
        signalement = Signalement(
            idLocal = "local-1",
            categorie = Categorie.ROUTE,
            description = "Chaussée dégradée",
            latitude = null,
            longitude = null,
            photoPath = null,
            photoUrl = null,
            statut = statut,
            synchronise = synchronise,
            serverId = serverId,
            dateCreation = 1_700_000_000_000L,
            isDemo = true,
            derniereErreurSync = null,
            resolutionProposeePar = proposePar
        ),
        charge = false
    )

    // --- Proposer une résolution ---------------------------------------------

    @Test
    fun `proposer est possible depuis PRIS_EN_CHARGE`() {
        assertTrue(etat(StatutSignalement.PRIS_EN_CHARGE).peutProposerResolution())
    }

    @Test
    fun `proposer est possible depuis REOUVERT_NON_RESOLU`() {
        assertTrue(etat(StatutSignalement.REOUVERT_NON_RESOLU).peutProposerResolution())
    }

    @Test
    fun `proposer est impossible depuis ENVOYE`() {
        assertFalse(etat(StatutSignalement.ENVOYE).peutProposerResolution())
    }

    @Test
    fun `proposer est impossible sur un signalement jamais synchronise`() {
        assertFalse(
            etat(StatutSignalement.PRIS_EN_CHARGE, synchronise = false, serverId = null)
                .peutProposerResolution()
        )
    }

    @Test
    fun `proposer est impossible sans etat charge`() {
        assertFalse(DetailSignalementUiState().peutProposerResolution())
    }

    // --- Confirmer / rouvrir --------------------------------------------------

    @Test
    fun `le citoyen peut confirmer quand c'est l'admin qui a propose`() {
        val etat = etat(
            StatutSignalement.RESOLUTION_A_CONFIRMER,
            proposePar = RoleResolution.ADMIN
        )

        assertTrue(etat.peutConfirmerOuRouvrir())
        assertFalse(etat.attenteConfirmationAdmin())
    }

    @Test
    fun `le citoyen ne peut pas confirmer sa propre proposition (validation croisee)`() {
        val etat = etat(
            StatutSignalement.RESOLUTION_A_CONFIRMER,
            proposePar = RoleResolution.CITOYEN
        )

        assertFalse(etat.peutConfirmerOuRouvrir())
        assertTrue("il doit attendre l'admin", etat.attenteConfirmationAdmin())
    }

    @Test
    fun `sans proposition connue, aucune action de confirmation n'est proposee`() {
        val etat = etat(StatutSignalement.RESOLUTION_A_CONFIRMER, proposePar = null)

        assertFalse(etat.peutConfirmerOuRouvrir())
        assertFalse(etat.attenteConfirmationAdmin())
    }

    @Test
    fun `un dossier deja resolu n'offre plus d'action`() {
        val etat = etat(StatutSignalement.RESOLU_CONFIRME, proposePar = RoleResolution.ADMIN)

        assertFalse(etat.peutProposerResolution())
        assertFalse(etat.peutConfirmerOuRouvrir())
        assertFalse(etat.attenteConfirmationAdmin())
    }
}
