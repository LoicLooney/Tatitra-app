package mg.itu.tatitra_app.data.local

import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement

/**
 * Conversions entre la ligne Room (chaînes) et le modèle métier (enums).
 * Une catégorie inconnue en base est repliée sur ROUTE plutôt que de faire planter la liste.
 */
fun SignalementEntity.versDomaine(): Signalement = Signalement(
    idLocal = idLocal,
    categorie = Categorie.depuisCode(categorie) ?: Categorie.ROUTE,
    description = description,
    latitude = latitude,
    longitude = longitude,
    photoPath = photoPath,
    photoUrl = photoUrl,
    statut = StatutSignalement.depuisCode(statut),
    synchronise = synchronise,
    serverId = serverId,
    dateCreation = dateCreation,
    isDemo = isDemo,
    derniereErreurSync = derniereErreurSync
)

fun Signalement.versEntity(): SignalementEntity = SignalementEntity(
    idLocal = idLocal,
    categorie = categorie.name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    photoPath = photoPath,
    photoUrl = photoUrl,
    statut = statut.name,
    synchronise = synchronise,
    serverId = serverId,
    dateCreation = dateCreation,
    isDemo = isDemo,
    derniereErreurSync = derniereErreurSync
)
