package mg.itu.tatitra_app.domain

/**
 * Session ouverte auprès de Supabase Auth.
 *
 * Le jeton d'accès est conservé pour un usage futur (appels authentifiés) ; aujourd'hui
 * la connexion ne sert qu'à identifier l'utilisateur dans l'application : les signalements
 * restent anonymes côté serveur, comme convenu.
 */
data class SessionUtilisateur(
    val idUtilisateur: String,
    val email: String,
    val jetonAcces: String,
    val jetonRafraichissement: String,
    /** Horodatage d'expiration du jeton, en millisecondes. */
    val expireLeMs: Long
) {
    /**
     * Nom affiché à l'accueil. Faute de nom complet, on reprend la partie locale de
     * l'adresse : « rakoto.jean@exemple.mg » devient « Rakoto Jean ».
     */
    val nomAffiche: String
        get() = email
            .substringBefore('@')
            .split('.', '_', '-')
            .filter { it.isNotBlank() }
            .joinToString(" ") { mot ->
                mot.replaceFirstChar { it.uppercaseChar() }
            }
            .ifBlank { email }

    fun estExpiree(maintenantMs: Long = System.currentTimeMillis()): Boolean =
        maintenantMs >= expireLeMs
}
