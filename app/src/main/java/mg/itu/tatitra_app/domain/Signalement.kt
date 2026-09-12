package mg.itu.tatitra_app.domain

/**
 * Signalement tel que manipulé par l'interface et les ViewModels.
 *
 * Les champs optionnels (position, photo, identifiant serveur) sont nullables et
 * traités explicitement : un signalement reste valide sans photo ni GPS.
 */
data class Signalement(
    val idLocal: String,
    val categorie: Categorie,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val photoUrl: String?,
    val statut: StatutSignalement,
    val synchronise: Boolean,
    val serverId: String?,
    val dateCreation: Long,
    val isDemo: Boolean,
    val derniereErreurSync: String?
) {
    val aUnePosition: Boolean get() = latitude != null && longitude != null

    /** Photo à afficher : l'URL serveur une fois synchronisée, sinon le fichier local. */
    val sourcePhoto: String? get() = photoUrl ?: photoPath
}
