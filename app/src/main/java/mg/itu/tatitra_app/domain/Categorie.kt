package mg.itu.tatitra_app.domain

/**
 * Catégories de problème d'infrastructure (§2.6 des règles de code).
 * Le nom de la constante EST la chaîne échangée avec le backend : aucune valeur en dur ailleurs.
 */
enum class Categorie(val libelle: String) {
    ROUTE("Route"),
    DECHETS("Déchets"),
    ECLAIRAGE("Éclairage public"),
    DRAINAGE("Drainage"),
    PONT("Pont / ouvrage");

    companion object {
        /** Retourne null si la valeur reçue (serveur, base locale) n'est pas une catégorie connue. */
        fun depuisCode(code: String?): Categorie? = entries.firstOrNull { it.name == code }
    }
}
