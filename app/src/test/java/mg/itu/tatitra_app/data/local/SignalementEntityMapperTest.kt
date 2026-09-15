package mg.itu.tatitra_app.data.local

import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.StatutSignalement
import org.junit.Assert.assertEquals
import org.junit.Test

/** Conversions entre la ligne Room (chaînes) et le modèle métier (enums). */
class SignalementEntityMapperTest {

    private fun entite(
        categorie: String = "ROUTE",
        statut: String = "ENVOYE"
    ) = SignalementEntity(
        idLocal = "local-1",
        categorie = categorie,
        description = "Chaussée dégradée",
        latitude = -18.8792,
        longitude = 47.5079,
        photoPath = "/photos/a.jpg",
        photoUrl = null,
        statut = statut,
        synchronise = false,
        serverId = null,
        dateCreation = 1_700_000_000_000L,
        isDemo = true,
        derniereErreurSync = null
    )

    @Test
    fun `aller-retour entite vers domaine puis retour conserve les donnees`() {
        val depart = entite().copy(
            photoUrl = "https://exemple.test/a.jpg",
            serverId = "SIG-1",
            synchronise = true,
            resolutionProposeePar = "ADMIN",
            dateLimiteConfirmation = "2026-09-18T10:00:00Z",
            motifReouverture = null
        )

        val arrivee = depart.versDomaine().versEntity()

        assertEquals(depart, arrivee)
    }

    @Test
    fun `les chaines deviennent des enums`() {
        val domaine = entite(categorie = "DECHETS", statut = "PRIS_EN_CHARGE").versDomaine()

        assertEquals(Categorie.DECHETS, domaine.categorie)
        assertEquals(StatutSignalement.PRIS_EN_CHARGE, domaine.statut)
    }

    @Test
    fun `une categorie inconnue en base ne fait pas planter la liste`() {
        val domaine = entite(categorie = "VALEUR_OBSOLETE").versDomaine()

        assertEquals(Categorie.ROUTE, domaine.categorie)
    }

    @Test
    fun `un statut inconnu retombe sur ENVOYE`() {
        val domaine = entite(statut = "STATUT_DU_FUTUR").versDomaine()

        assertEquals(StatutSignalement.ENVOYE, domaine.statut)
    }

    @Test
    fun `la photo affichee est l'URL serveur si elle existe, sinon le fichier local`() {
        val local = entite().versDomaine()
        assertEquals("/photos/a.jpg", local.sourcePhoto)

        val synchronise = entite().copy(photoUrl = "https://exemple.test/a.jpg").versDomaine()
        assertEquals("https://exemple.test/a.jpg", synchronise.sourcePhoto)
    }

    @Test
    fun `sans coordonnees, aUnePosition est faux`() {
        val sansGps = entite().copy(latitude = null, longitude = null).versDomaine()

        assertEquals(false, sansGps.aUnePosition)
        assertEquals(true, entite().versDomaine().aUnePosition)
    }
}
