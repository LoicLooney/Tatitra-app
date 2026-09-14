package mg.itu.tatitra_app.ui.reports

import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement

/**
 * Jeu de données fictives pour J2 (Membre B).
 * Permet d'afficher et de naviguer la liste sans dépendre encore de Room / de l'API.
 */
object SignalementsFictifs {

    private val maintenant = System.currentTimeMillis()

    val liste: List<Signalement> = listOf(
        Signalement(
            idLocal = "demo-001",
            categorie = Categorie.ROUTE,
            description = "Nid-de-poule profond devant le marché — donnée simulée",
            latitude = -18.879200,
            longitude = 47.507900,
            photoPath = null,
            photoUrl = null,
            statut = StatutSignalement.EN_ATTENTE_SYNC,
            synchronise = false,
            serverId = null,
            dateCreation = maintenant - 3_600_000L,
            isDemo = true,
            derniereErreurSync = null
        ),
        Signalement(
            idLocal = "demo-002",
            categorie = Categorie.DECHETS,
            description = "Accumulation de déchets près du canal — donnée simulée",
            latitude = -18.880000,
            longitude = 47.510000,
            photoPath = null,
            photoUrl = null,
            statut = StatutSignalement.ENVOYE,
            synchronise = true,
            serverId = "11111111-1111-1111-1111-111111111111",
            dateCreation = maintenant - 86_400_000L,
            isDemo = true,
            derniereErreurSync = null
        ),
        Signalement(
            idLocal = "demo-003",
            categorie = Categorie.ECLAIRAGE,
            description = "Lampadaire éteint depuis plusieurs soirs — donnée simulée",
            latitude = -18.910000,
            longitude = 47.520000,
            photoPath = null,
            photoUrl = null,
            statut = StatutSignalement.PRIS_EN_CHARGE,
            synchronise = true,
            serverId = "22222222-2222-2222-2222-222222222222",
            dateCreation = maintenant - 172_800_000L,
            isDemo = true,
            derniereErreurSync = null
        ),
        Signalement(
            idLocal = "demo-004",
            categorie = Categorie.DRAINAGE,
            description = "Regard bouché après la pluie — donnée simulée",
            latitude = null,
            longitude = null,
            photoPath = null,
            photoUrl = null,
            statut = StatutSignalement.RESOLUTION_A_CONFIRMER,
            synchronise = true,
            serverId = "33333333-3333-3333-3333-333333333333",
            dateCreation = maintenant - 259_200_000L,
            isDemo = true,
            derniereErreurSync = null
        ),
        Signalement(
            idLocal = "demo-005",
            categorie = Categorie.PONT,
            description = "Garde-corps endommagé sur la passerelle — donnée simulée",
            latitude = -18.870000,
            longitude = 47.490000,
            photoPath = null,
            photoUrl = null,
            statut = StatutSignalement.RESOLU_CONFIRME,
            synchronise = true,
            serverId = "44444444-4444-4444-4444-444444444444",
            dateCreation = maintenant - 604_800_000L,
            isDemo = true,
            derniereErreurSync = null
        )
    )

    fun parIdLocal(idLocal: String): Signalement? = liste.firstOrNull { it.idLocal == idLocal }
}
