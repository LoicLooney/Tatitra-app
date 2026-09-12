package mg.itu.tatitra_app.ui.navigation

/**
 * Routes de navigation (S5). Centralisées ici pour qu'aucune chaîne de route
 * ne soit écrite en dur dans un écran.
 */
object DestinationsTatitra {
    const val ACCUEIL = "accueil"
    const val NOUVEAU_SIGNALEMENT = "nouveau-signalement"
    const val SIGNALEMENTS = "signalements"

    const val ARGUMENT_ID_LOCAL = "idLocal"
    const val DETAIL_SIGNALEMENT = "signalement/{$ARGUMENT_ID_LOCAL}"

    /** Construit la route de détail pour un signalement donné. */
    fun detailSignalement(idLocal: String): String = "signalement/$idLocal"
}
