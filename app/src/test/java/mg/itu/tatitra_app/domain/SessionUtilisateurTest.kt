package mg.itu.tatitra_app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUtilisateurTest {

    private fun session(
        email: String = "rakoto@exemple.mg",
        expireLeMs: Long = 0L
    ) = SessionUtilisateur(
        idUtilisateur = "u-1",
        email = email,
        jetonAcces = "jeton",
        jetonRafraichissement = "rafraichissement",
        expireLeMs = expireLeMs
    )

    @Test
    fun `le nom affiche reprend la partie locale de l'adresse`() {
        assertEquals("Rakoto", session(email = "rakoto@exemple.mg").nomAffiche)
    }

    @Test
    fun `les separateurs deviennent des espaces et chaque mot prend une majuscule`() {
        assertEquals("Rakoto Jean", session(email = "rakoto.jean@exemple.mg").nomAffiche)
        assertEquals("Jean Paul", session(email = "jean_paul@exemple.mg").nomAffiche)
        assertEquals("Marie Claire", session(email = "marie-claire@exemple.mg").nomAffiche)
    }

    @Test
    fun `une adresse sans partie locale exploitable retombe sur l'adresse entiere`() {
        assertEquals("@exemple.mg", session(email = "@exemple.mg").nomAffiche)
    }

    @Test
    fun `une session dont l'echeance est passee est expiree`() {
        val maintenant = 1_800_000_000_000L
        assertTrue(session(expireLeMs = maintenant - 1).estExpiree(maintenant))
        assertTrue("l'instant exact d'expiration compte comme expiré",
            session(expireLeMs = maintenant).estExpiree(maintenant))
        assertFalse(session(expireLeMs = maintenant + 1).estExpiree(maintenant))
    }
}
