package mg.itu.tatitra_app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ce que l'écran de connexion accepte, refuse, et comment il l'explique. */
class ReglesConnexionTest {

    @Test
    fun `une adresse bien formee est acceptee`() {
        assertTrue(ReglesConnexion.emailEstValide("rakoto@exemple.mg"))
        assertTrue(ReglesConnexion.emailEstValide("jean.paul+test@sous.domaine.fr"))
    }

    @Test
    fun `une adresse mal formee est refusee`() {
        assertFalse(ReglesConnexion.emailEstValide("rakoto"))
        assertFalse(ReglesConnexion.emailEstValide("rakoto@"))
        assertFalse(ReglesConnexion.emailEstValide("rakoto@exemple"))
        assertFalse(ReglesConnexion.emailEstValide("@exemple.mg"))
        assertFalse(ReglesConnexion.emailEstValide("un espace@exemple.mg"))
    }

    @Test
    fun `les espaces autour de l'adresse sont tolerés`() {
        assertTrue(ReglesConnexion.emailEstValide("  rakoto@exemple.mg  "))
    }

    @Test
    fun `un numero malgache est reconnu comme numero`() {
        assertTrue(ReglesConnexion.ressembleAUnNumero("034 12 345 67"))
        assertTrue(ReglesConnexion.ressembleAUnNumero("+261 34 12 345 67"))
        assertTrue(ReglesConnexion.ressembleAUnNumero("0341234567"))
    }

    @Test
    fun `une adresse n'est pas prise pour un numero`() {
        assertFalse(ReglesConnexion.ressembleAUnNumero("rakoto@exemple.mg"))
    }

    @Test
    fun `saisir un numero donne un message qui explique pourquoi`() {
        val message = ReglesConnexion.erreurIdentifiant("034 12 345 67")
        assertNotNull(message)
        assertTrue(
            "le message doit orienter vers l'e-mail, pas dire « invalide »",
            message!!.contains("e-mail", ignoreCase = true)
        )
    }

    @Test
    fun `un champ vide ne declenche aucun reproche`() {
        assertNull(ReglesConnexion.erreurIdentifiant(""))
        assertNull(ReglesConnexion.erreurMotDePasse(""))
    }

    @Test
    fun `une adresse valide ne declenche aucune erreur`() {
        assertNull(ReglesConnexion.erreurIdentifiant("rakoto@exemple.mg"))
    }

    @Test
    fun `un mot de passe trop court est refuse avec la longueur attendue`() {
        assertFalse(ReglesConnexion.motDePasseEstValide("12345"))
        val message = ReglesConnexion.erreurMotDePasse("12345")
        assertNotNull(message)
        assertTrue(message!!.contains("${ReglesConnexion.LONGUEUR_MOT_DE_PASSE_MIN}"))
    }

    @Test
    fun `six caracteres suffisent, comme chez Supabase`() {
        assertEquals(6, ReglesConnexion.LONGUEUR_MOT_DE_PASSE_MIN)
        assertTrue(ReglesConnexion.motDePasseEstValide("123456"))
        assertNull(ReglesConnexion.erreurMotDePasse("123456"))
    }

    @Test
    fun `le formulaire n'est complet qu'avec une adresse et un mot de passe valides`() {
        assertFalse(ReglesConnexion.formulaireEstComplet("", ""))
        assertFalse(ReglesConnexion.formulaireEstComplet("rakoto@exemple.mg", "court"))
        assertFalse(ReglesConnexion.formulaireEstComplet("034 12 345 67", "demo1234"))
        assertTrue(ReglesConnexion.formulaireEstComplet("rakoto@exemple.mg", "demo1234"))
    }
}
