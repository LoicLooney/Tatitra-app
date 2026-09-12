package mg.itu.tatitra_app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Les mêmes règles sont appliquées côté backend (validate-signalement.js) :
 * ces tests documentent le contrat commun.
 */
class ReglesSignalementTest {

    @Test
    fun `description trop courte refusee`() {
        assertFalse(ReglesSignalement.descriptionEstValide("Trou"))
    }

    @Test
    fun `description avec espaces autour comptee sans les espaces`() {
        val descriptionDeNeufCaracteres = "   Nid-poule   "
        assertFalse(ReglesSignalement.descriptionEstValide(descriptionDeNeufCaracteres))
    }

    @Test
    fun `description normale acceptee`() {
        assertTrue(ReglesSignalement.descriptionEstValide("Chaussée dégradée devant l'arrêt de bus"))
    }

    @Test
    fun `description trop longue refusee`() {
        val tropLongue = "a".repeat(ReglesSignalement.LONGUEUR_DESCRIPTION_MAX + 1)
        assertFalse(ReglesSignalement.descriptionEstValide(tropLongue))
    }

    @Test
    fun `position dans les bornes acceptee`() {
        assertTrue(ReglesSignalement.positionEstValide(-18.8792, 47.5079))
    }

    @Test
    fun `position hors bornes refusee`() {
        assertFalse(ReglesSignalement.positionEstValide(-120.0, 47.5079))
        assertFalse(ReglesSignalement.positionEstValide(-18.8792, 200.0))
    }

    @Test
    fun `position incomplete refusee`() {
        assertFalse(ReglesSignalement.positionEstValide(null, 47.5079))
        assertFalse(ReglesSignalement.positionEstValide(-18.8792, null))
    }

    @Test
    fun `categorie inconnue ne correspond a aucune valeur`() {
        assertNull(Categorie.depuisCode("INCONNUE"))
        assertEquals(Categorie.ROUTE, Categorie.depuisCode("ROUTE"))
    }

    @Test
    fun `statut inconnu retombe sur ENVOYE`() {
        assertEquals(StatutSignalement.ENVOYE, StatutSignalement.depuisCode("AUTRE_CHOSE"))
        assertEquals(StatutSignalement.ENVOYE, StatutSignalement.depuisCode(null))
        assertEquals(
            StatutSignalement.RESOLUTION_A_CONFIRMER,
            StatutSignalement.depuisCode("RESOLUTION_A_CONFIRMER")
        )
    }
}
