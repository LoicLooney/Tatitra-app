package mg.itu.tatitra_app.data.repository

/**
 * Bilan d'une passe de synchronisation.
 *
 * `doitReessayer` distingue une panne temporaire (réseau coupé, serveur injoignable,
 * erreur 500) d'un refus définitif du serveur (400 : données invalides). Seule la
 * première mérite une nouvelle tentative du WorkManager.
 */
data class ResultatSync(
    val nombreEnvoyes: Int = 0,
    val nombreEchecs: Int = 0,
    val doitReessayer: Boolean = false,
    val message: String? = null
) {
    val aTravaille: Boolean get() = nombreEnvoyes > 0 || nombreEchecs > 0
}
