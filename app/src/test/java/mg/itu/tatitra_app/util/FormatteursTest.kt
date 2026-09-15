package mg.itu.tatitra_app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lecture des dates renvoyées par l'API et compte à rebours de la règle J+7.
 *
 * Le backend sérialise `date_limite_confirmation` au format ISO 8601 UTC
 * (« 2026-09-18T10:00:00.000Z ») : c'est ce format-là qui doit être lu sans erreur,
 * sinon l'écran de confirmation n'affiche aucun délai restant.
 */
class FormatteursTest {

    private val echeance = "2026-09-18T10:00:00.000Z"

    // --- Lecture des dates ---------------------------------------------------

    @Test
    fun `lit le format renvoye par l'API (millisecondes et Z)`() {
        val millis = parserDateIsoEnMillis(echeance)

        assertNotNull(millis)
        assertEquals(1789725600000L, millis)
    }

    @Test
    fun `lit aussi un decalage horaire explicite`() {
        val avecDecalage = parserDateIsoEnMillis("2026-09-18T13:00:00+03:00")

        assertEquals(parserDateIsoEnMillis(echeance), avecDecalage)
    }

    @Test
    fun `lit le format sans millisecondes`() {
        assertNotNull(parserDateIsoEnMillis("2026-09-18T10:00:00Z"))
    }

    @Test
    fun `lit un horodatage PostgreSQL separe par un espace`() {
        assertNotNull(parserDateIsoEnMillis("2026-09-18 10:00:00.000Z"))
    }

    @Test
    fun `une valeur absente ou illisible ne fait pas planter l'ecran`() {
        assertNull(parserDateIsoEnMillis(null))
        assertNull(parserDateIsoEnMillis(""))
        assertNull(parserDateIsoEnMillis("   "))
        assertNull(parserDateIsoEnMillis("pas une date"))
        assertNull(parserDateIsoEnMillis("2026-13-45T99:99:99Z"))
    }

    @Test
    fun `formaterDateIso affiche un tiret quand la date est absente`() {
        assertEquals("—", formaterDateIso(null))
        assertEquals("—", formaterDateIso("illisible"))
    }

    // --- Compte à rebours J+7 ------------------------------------------------

    @Test
    fun `compte a rebours - il reste presque 7 jours juste apres la proposition`() {
        val maintenant = parserDateIsoEnMillis("2026-09-11T10:00:00.000Z")!!

        val restant = calculerCompteARebours(echeance, maintenant)!!

        assertEquals(7, restant.jours)
        assertEquals(0, restant.heures)
        assertTrue(!restant.expire)
    }

    @Test
    fun `compte a rebours - decompose correctement jours heures minutes secondes`() {
        val maintenant = parserDateIsoEnMillis("2026-09-16T07:30:15.000Z")!!

        val restant = calculerCompteARebours(echeance, maintenant)!!

        assertEquals(2, restant.jours)
        assertEquals(2, restant.heures)
        assertEquals(29, restant.minutes)
        assertEquals(45, restant.secondes)
        assertEquals("2 j 02 h 29 min 45 s", restant.libelleCourt)
    }

    @Test
    fun `compte a rebours - echeance depassee, le delai est marque comme expire`() {
        val maintenant = parserDateIsoEnMillis("2026-09-19T10:00:00.000Z")!!

        val restant = calculerCompteARebours(echeance, maintenant)!!

        assertTrue(restant.expire)
        assertEquals("Délai dépassé", restant.libelleCourt)
    }

    @Test
    fun `compte a rebours - l'instant exact de l'echeance compte comme expire`() {
        val restant = calculerCompteARebours(echeance, parserDateIsoEnMillis(echeance)!!)!!

        assertTrue(restant.expire)
    }

    @Test
    fun `compte a rebours - sans echeance, il n'y a rien a afficher`() {
        assertNull(calculerCompteARebours(null))
        assertNull(calculerCompteARebours("illisible"))
    }

    // --- Coordonnées ---------------------------------------------------------

    @Test
    fun `coordonnees absentes affichees en clair`() {
        assertEquals("Non renseignée", formaterCoordonnees(null, 47.5079))
        assertEquals("Non renseignée", formaterCoordonnees(-18.8792, null))
    }

    @Test
    fun `coordonnees affichees avec six decimales`() {
        assertEquals("-18,879200, 47,507900", formaterCoordonnees(-18.8792, 47.5079))
    }
}
