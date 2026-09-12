package mg.itu.tatitra_app.domain

/**
 * Règles de saisie partagées avec la validation du backend
 * (backend/src/constants.js) : les deux côtés doivent refuser la même chose.
 */
object ReglesSignalement {
    const val LONGUEUR_DESCRIPTION_MIN = 10
    const val LONGUEUR_DESCRIPTION_MAX = 500

    const val LATITUDE_MIN = -90.0
    const val LATITUDE_MAX = 90.0
    const val LONGITUDE_MIN = -180.0
    const val LONGITUDE_MAX = 180.0

    fun descriptionEstValide(description: String): Boolean =
        description.trim().length in LONGUEUR_DESCRIPTION_MIN..LONGUEUR_DESCRIPTION_MAX

    fun positionEstValide(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return latitude in LATITUDE_MIN..LATITUDE_MAX && longitude in LONGITUDE_MIN..LONGITUDE_MAX
    }
}
