package mg.itu.tatitra_app.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mg.itu.tatitra_app.data.FausseSupabaseAuthApi
import mg.itu.tatitra_app.data.FauxStockageSession
import mg.itu.tatitra_app.data.ReponseAuthSimulee
import mg.itu.tatitra_app.data.sessionServeur
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var stockage: FauxStockageSession
    private lateinit var api: FausseSupabaseAuthApi
    private lateinit var repository: AuthRepository

    @Before
    fun preparer() {
        stockage = FauxStockageSession()
        api = FausseSupabaseAuthApi()
        repository = AuthRepository(api, stockage)
    }

    // --- Connexion -----------------------------------------------------------

    @Test
    fun `une connexion reussie ouvre et enregistre la session`() = runTest {
        val resultat = repository.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(resultat is ResultatAuth.Succes)
        val session = stockage.sessionCourante()
        assertNotNull(session)
        assertEquals("rakoto@exemple.mg", session!!.email)
        assertEquals("jeton-acces", session.jetonAcces)
        assertEquals("u-1", session.idUtilisateur)
    }

    @Test
    fun `l'adresse est nettoyee de ses espaces avant l'envoi`() = runTest {
        repository.connecter("  rakoto@exemple.mg  ", "demo1234")

        assertEquals("rakoto@exemple.mg", api.connexionsRecues.single().email)
    }

    @Test
    fun `l'echeance du jeton est calculee a partir de expires_in`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Ok(sessionServeur(expiresIn = 60))
        val avant = System.currentTimeMillis()

        repository.connecter("rakoto@exemple.mg", "demo1234")

        val expiration = stockage.sessionCourante()!!.expireLeMs
        assertTrue("l'échéance doit être dans le futur", expiration > avant)
        assertTrue("environ 60 s", expiration - avant in 55_000..65_000)
    }

    @Test
    fun `des identifiants refuses donnent un message en francais`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Refus(
            400,
            """{"error":"invalid_grant","error_description":"Invalid login credentials"}"""
        )

        val resultat = repository.connecter("rakoto@exemple.mg", "mauvais1")

        assertTrue(resultat is ResultatAuth.Echec)
        assertEquals(
            "E-mail ou mot de passe incorrect.",
            (resultat as ResultatAuth.Echec).message
        )
        assertNull("aucune session ne doit être écrite", stockage.sessionCourante())
    }

    @Test
    fun `une adresse non confirmee est expliquee`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Refus(
            400,
            """{"msg":"Email not confirmed"}"""
        )

        val resultat = repository.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(
            (resultat as ResultatAuth.Echec).message.contains("confirm", ignoreCase = true)
        )
    }

    @Test
    fun `le reseau coupe ne remonte pas une erreur technique`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Panne()

        val resultat = repository.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(resultat is ResultatAuth.Echec)
        assertTrue(
            (resultat as ResultatAuth.Echec).message.contains("connexion", ignoreCase = true)
        )
    }

    @Test
    fun `trop de tentatives invite a patienter`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Refus(429, "{}")

        val resultat = repository.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(
            (resultat as ResultatAuth.Echec).message.contains("Patientez")
        )
    }

    @Test
    fun `un refus sans corps lisible reste comprehensible`() = runTest {
        api.reponseConnexion = ReponseAuthSimulee.Refus(503, "indisponible")

        val resultat = repository.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(resultat is ResultatAuth.Echec)
        assertTrue((resultat as ResultatAuth.Echec).message.contains("503"))
    }

    // --- Inscription ---------------------------------------------------------

    @Test
    fun `une inscription sans jeton signale que la confirmation est requise`() = runTest {
        // Cas d'un projet Supabase avec « Confirm email » activé : compte créé, pas de session.
        api.reponseInscription = ReponseAuthSimulee.Ok(
            sessionServeur(accessToken = null, refreshToken = null, expiresIn = null)
        )

        val resultat = repository.creerCompte("rakoto@exemple.mg", "demo1234")

        assertTrue(
            "ce n'est pas un échec : le compte existe",
            resultat is ResultatAuth.ConfirmationRequise
        )
        assertEquals(
            "rakoto@exemple.mg",
            (resultat as ResultatAuth.ConfirmationRequise).email
        )
        assertNull(stockage.sessionCourante())
    }

    @Test
    fun `une inscription avec jeton ouvre directement la session`() = runTest {
        val resultat = repository.creerCompte("rakoto@exemple.mg", "demo1234")

        assertTrue(resultat is ResultatAuth.Succes)
        assertNotNull(stockage.sessionCourante())
    }

    @Test
    fun `une adresse deja inscrite est signalee sans jargon`() = runTest {
        api.reponseInscription = ReponseAuthSimulee.Refus(
            422,
            """{"msg":"User already registered"}"""
        )

        val resultat = repository.creerCompte("rakoto@exemple.mg", "demo1234")

        assertTrue(
            (resultat as ResultatAuth.Echec).message.contains("existe déjà")
        )
    }

    @Test
    fun `un domaine fictif refuse par Supabase est explique en francais`() = runTest {
        // Supabase exige un domaine pourvu d'un enregistrement MX : « @exemple.mg » est
        // accepté par la validation locale mais refusé à l'inscription.
        api.reponseInscription = ReponseAuthSimulee.Refus(
            400,
            """{"code":"email_address_invalid","msg":"Email address \"a@exemple.mg\" is invalid"}"""
        )

        val resultat = repository.creerCompte("a@exemple.mg", "demo1234")

        val message = (resultat as ResultatAuth.Echec).message
        assertFalse("le message ne doit pas rester en anglais", message.contains("is invalid"))
        assertTrue(message.contains("domaine"))
    }

    // --- Mot de passe oublié -------------------------------------------------

    @Test
    fun `la reinitialisation envoie l'adresse et reste neutre`() = runTest {
        val resultat = repository.demanderReinitialisation("  rakoto@exemple.mg ")

        assertTrue(resultat is ResultatAuth.ConfirmationRequise)
        assertEquals("rakoto@exemple.mg", api.reinitialisationsRecues.single())
    }

    // --- Déconnexion ---------------------------------------------------------

    @Test
    fun `la deconnexion efface la session et previent le serveur`() = runTest {
        repository.connecter("rakoto@exemple.mg", "demo1234")

        repository.deconnecter()

        assertNull(stockage.sessionCourante())
        assertEquals("Bearer jeton-acces", api.deconnexionsRecues.single())
    }

    @Test
    fun `sans session ouverte la deconnexion n'appelle pas le serveur`() = runTest {
        repository.deconnecter()

        assertTrue(api.deconnexionsRecues.isEmpty())
        assertEquals(1, stockage.nombreEffacements)
    }

    // --- Mode démonstration --------------------------------------------------

    @Test
    fun `le mode demonstration est memorise`() = runTest {
        assertFalse(repository.observerModeDemonstration().first())

        repository.continuerEnDemonstration()

        assertTrue(repository.observerModeDemonstration().first())
        assertNull("le mode démo n'ouvre aucune session", stockage.sessionCourante())
    }

    @Test
    fun `se connecter met fin au mode demonstration`() = runTest {
        repository.continuerEnDemonstration()

        repository.connecter("rakoto@exemple.mg", "demo1234")

        assertFalse(stockage.enDemonstration())
    }

    // --- Authentification non configurée -------------------------------------

    @Test
    fun `sans configuration Supabase l'application oriente vers le mode demonstration`() = runTest {
        val sansApi = AuthRepository(api = null, preferences = stockage)

        assertFalse(sansApi.authentificationDisponible)
        val resultat = sansApi.connecter("rakoto@exemple.mg", "demo1234")

        assertTrue(resultat is ResultatAuth.Echec)
        assertTrue(
            (resultat as ResultatAuth.Echec).message.contains("démonstration", ignoreCase = true)
        )
    }

    @Test
    fun `sans configuration Supabase le mode demonstration reste possible`() = runTest {
        val sansApi = AuthRepository(api = null, preferences = stockage)

        sansApi.continuerEnDemonstration()

        assertTrue(sansApi.observerModeDemonstration().first())
    }
}
