package mg.itu.tatitra_app.data.repository

import kotlinx.coroutines.test.runTest
import mg.itu.tatitra_app.data.FausseTatitraApi
import mg.itu.tatitra_app.data.FauxSignalementDao
import mg.itu.tatitra_app.data.ReponseSimulee
import mg.itu.tatitra_app.data.entiteLocale
import mg.itu.tatitra_app.data.reponseServeur
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.StatutSignalement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Cœur de l'offline-first : ce que fait le Repository quand le réseau marche,
 * quand il est coupé, et quand le serveur refuse.
 */
class SignalementRepositoryTest {

    private lateinit var dao: FauxSignalementDao
    private lateinit var api: FausseTatitraApi
    private lateinit var repository: SignalementRepository

    @Before
    fun preparer() {
        dao = FauxSignalementDao()
        api = FausseTatitraApi()
        repository = SignalementRepository(dao, api)
    }

    // --- Création locale -----------------------------------------------------

    @Test
    fun `enregistrerLocal ecrit en base sans appeler le reseau`() = runTest {
        val idLocal = repository.enregistrerLocal(
            categorie = Categorie.ROUTE,
            description = "  Chaussée dégradée devant l'arrêt  ",
            latitude = -18.8792,
            longitude = 47.5079,
            photoPath = null,
            isDemo = true
        )

        val ligne = dao.recupererParId(idLocal)
        assertNotNull(ligne)
        assertEquals(StatutSignalement.EN_ATTENTE_SYNC.name, ligne?.statut)
        assertFalse(ligne!!.synchronise)
        assertEquals("Chaussée dégradée devant l'arrêt", ligne.description)
        assertTrue("aucun appel réseau attendu", api.creationsRecues.isEmpty())
    }

    // --- Synchronisation : cas nominal ---------------------------------------

    @Test
    fun `synchroniserEnAttente envoie et marque la ligne comme synchronisee`() = runTest {
        dao.inserer(entiteLocale())
        api.reponseCreation = ReponseSimulee.Ok(reponseServeur(id = "SIG-42", statut = "ENVOYE"))

        val resultat = repository.synchroniserEnAttente()

        assertEquals(1, resultat.nombreEnvoyes)
        assertEquals(0, resultat.nombreEchecs)
        assertFalse(resultat.doitReessayer)

        val ligne = dao.recupererParId("local-1")
        assertTrue(ligne!!.synchronise)
        assertEquals("SIG-42", ligne.serverId)
        assertEquals(StatutSignalement.ENVOYE.name, ligne.statut)
        assertNull(ligne.derniereErreurSync)
    }

    @Test
    fun `synchroniserEnAttente envoie le idLocal comme clientId (cle d'idempotence)`() = runTest {
        dao.inserer(entiteLocale(idLocal = "local-abc"))

        repository.synchroniserEnAttente()

        assertEquals("local-abc", api.creationsRecues.single().clientId)
    }

    @Test
    fun `sans rien en attente, la synchronisation ne fait aucun appel`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1", statut = "ENVOYE"))

        val resultat = repository.synchroniserEnAttente()

        assertEquals(0, resultat.nombreEnvoyes)
        assertFalse(resultat.aTravaille)
        assertTrue(api.creationsRecues.isEmpty())
    }

    // --- Synchronisation : réseau coupé --------------------------------------

    @Test
    fun `reseau coupe la donnee reste en local et la reprise est demandee`() = runTest {
        dao.inserer(entiteLocale())
        api.reponseCreation = ReponseSimulee.Panne()

        val resultat = repository.synchroniserEnAttente()

        assertEquals(0, resultat.nombreEnvoyes)
        assertEquals(1, resultat.nombreEchecs)
        assertTrue("une panne réseau doit être réessayée", resultat.doitReessayer)

        val ligne = dao.recupererParId("local-1")
        assertFalse("le signalement ne doit pas être perdu (T09)", ligne!!.synchronise)
        assertNotNull("l'erreur doit être mémorisée pour l'utilisateur", ligne.derniereErreurSync)
    }

    // --- Synchronisation : refus du serveur ----------------------------------

    @Test
    fun `erreur 500 est temporaire donc on reessaie`() = runTest {
        dao.inserer(entiteLocale())
        api.reponseCreation = ReponseSimulee.Refus(500)

        val resultat = repository.synchroniserEnAttente()

        assertTrue(resultat.doitReessayer)
        assertEquals(1, resultat.nombreEchecs)
    }

    @Test
    fun `erreur 400 est definitive donc on ne reessaie pas`() = runTest {
        dao.inserer(entiteLocale())
        api.reponseCreation = ReponseSimulee.Refus(400)

        val resultat = repository.synchroniserEnAttente()

        assertEquals(1, resultat.nombreEchecs)
        assertFalse("un refus 400 ne se corrigera pas en réessayant", resultat.doitReessayer)
    }

    @Test
    fun `erreur 408 timeout est temporaire`() = runTest {
        dao.inserer(entiteLocale())
        api.reponseCreation = ReponseSimulee.Refus(408)

        assertTrue(repository.synchroniserEnAttente().doitReessayer)
    }

    @Test
    fun `plusieurs signalements en attente sont tous traites`() = runTest {
        dao.inserer(entiteLocale(idLocal = "local-1"))
        dao.inserer(entiteLocale(idLocal = "local-2"))
        dao.inserer(entiteLocale(idLocal = "local-3"))

        val resultat = repository.synchroniserEnAttente()

        assertEquals(3, resultat.nombreEnvoyes)
        assertEquals(3, api.creationsRecues.size)
    }

    // --- Photo ---------------------------------------------------------------

    @Test
    fun `une photo deja televersee n'est pas renvoyee une seconde fois`() = runTest {
        dao.inserer(
            entiteLocale(photoPath = "/chemin/photo.jpg", photoUrl = "https://exemple.test/deja.jpg")
        )

        repository.synchroniserEnAttente()

        assertEquals("aucun nouvel upload attendu", 0, api.nombreUploads)
        assertEquals(
            "https://exemple.test/deja.jpg",
            api.creationsRecues.single().photoUrl
        )
    }

    @Test
    fun `un fichier photo absent n'empeche pas l'envoi du signalement`() = runTest {
        dao.inserer(entiteLocale(photoPath = "/chemin/inexistant.jpg"))

        val resultat = repository.synchroniserEnAttente()

        assertEquals(1, resultat.nombreEnvoyes)
        assertNull(api.creationsRecues.single().photoUrl)
    }

    // --- Rafraîchissement des statuts ----------------------------------------

    @Test
    fun `rafraichirStatuts applique le statut du serveur a la ligne locale`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1", statut = "ENVOYE"))
        api.reponseListe = listOf(
            reponseServeur(
                id = "SIG-1",
                clientId = "local-1",
                statut = "PRIS_EN_CHARGE"
            )
        )

        assertTrue(repository.rafraichirStatuts())

        assertEquals(
            StatutSignalement.PRIS_EN_CHARGE.name,
            dao.recupererParId("local-1")!!.statut
        )
    }

    @Test
    fun `rafraichirStatuts remonte la proposition de resolution et son echeance`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1", statut = "PRIS_EN_CHARGE"))
        api.reponseListe = listOf(
            reponseServeur(
                id = "SIG-1",
                clientId = "local-1",
                statut = "RESOLUTION_A_CONFIRMER",
                resolutionProposeePar = "ADMIN",
                dateLimiteConfirmation = "2026-09-18T10:00:00.000Z"
            )
        )

        repository.rafraichirStatuts()

        val ligne = dao.recupererParId("local-1")!!
        assertEquals(StatutSignalement.RESOLUTION_A_CONFIRMER.name, ligne.statut)
        assertEquals("ADMIN", ligne.resolutionProposeePar)
        assertEquals("2026-09-18T10:00:00.000Z", ligne.dateLimiteConfirmation)
    }

    @Test
    fun `rafraichirStatuts sans reseau echoue sans planter ni modifier la base`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1", statut = "ENVOYE"))
        api.listeEnPanne = true

        assertFalse(repository.rafraichirStatuts())
        assertEquals(StatutSignalement.ENVOYE.name, dao.recupererParId("local-1")!!.statut)
    }

    @Test
    fun `rafraichirStatuts ignore un signalement inconnu en local`() = runTest {
        api.reponseListe = listOf(reponseServeur(id = "SIG-9", clientId = "jamais-vu"))

        assertTrue(repository.rafraichirStatuts())
        assertTrue(dao.contenu().isEmpty())
    }

    // --- Actions de résolution (J5) ------------------------------------------

    @Test
    fun `proposer une resolution avant synchronisation est refuse avec un message clair`() = runTest {
        dao.inserer(entiteLocale(serverId = null))

        val resultat = repository.proposerResolution("local-1")

        assertTrue(resultat is ResultatActionResolution.Echec)
        assertTrue(
            (resultat as ResultatActionResolution.Echec).message.contains("Synchronisez")
        )
    }

    @Test
    fun `proposer une resolution envoie le role CITOYEN et applique la reponse`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1", statut = "PRIS_EN_CHARGE"))
        api.reponseResolution = ReponseSimulee.Ok(
            reponseServeur(
                id = "SIG-1",
                statut = "RESOLUTION_A_CONFIRMER",
                resolutionProposeePar = "CITOYEN"
            )
        )

        val resultat = repository.proposerResolution("local-1")

        assertTrue(resultat is ResultatActionResolution.Succes)
        assertEquals("CITOYEN", api.rolesRecus.single())
        val ligne = dao.recupererParId("local-1")!!
        assertEquals(StatutSignalement.RESOLUTION_A_CONFIRMER.name, ligne.statut)
        assertEquals("CITOYEN", ligne.resolutionProposeePar)
    }

    @Test
    fun `un refus 409 du serveur remonte son message a l'utilisateur`() = runTest {
        dao.inserer(entiteLocale(synchronise = true, serverId = "SIG-1"))
        api.reponseResolution = ReponseSimulee.Refus(409, """{"error":"Proposition impossible"}""")

        val resultat = repository.confirmerResolution("local-1")

        assertTrue(resultat is ResultatActionResolution.Echec)
        assertTrue(
            (resultat as ResultatActionResolution.Echec).message.contains("Proposition impossible")
        )
    }

    @Test
    fun `toujours endommage repasse la ligne locale en rouvert`() = runTest {
        dao.inserer(
            entiteLocale(synchronise = true, serverId = "SIG-1", statut = "RESOLUTION_A_CONFIRMER")
        )
        api.reponseResolution = ReponseSimulee.Ok(
            reponseServeur(
                id = "SIG-1",
                statut = "REOUVERT_NON_RESOLU",
                motifReouverture = "Toujours endommagé"
            )
        )

        val resultat = repository.declarerToujoursEndommage("local-1")

        assertTrue(resultat is ResultatActionResolution.Succes)
        assertEquals("le backend exige le rôle sur la réouverture", "CITOYEN", api.rolesRecus.single())
        val ligne = dao.recupererParId("local-1")!!
        assertEquals(StatutSignalement.REOUVERT_NON_RESOLU.name, ligne.statut)
        assertEquals("Toujours endommagé", ligne.motifReouverture)
    }

    @Test
    fun `une action sur un signalement inconnu echoue proprement`() = runTest {
        val resultat = repository.confirmerResolution("inexistant")

        assertTrue(resultat is ResultatActionResolution.Echec)
    }
}
