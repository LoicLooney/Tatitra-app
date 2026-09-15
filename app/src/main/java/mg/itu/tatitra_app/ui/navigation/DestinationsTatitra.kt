package mg.itu.tatitra_app.ui.navigation

object DestinationsTatitra {
    const val ACCUEIL = "accueil"
    const val NOUVEAU_SIGNALEMENT = "nouveau-signalement"
    const val SIGNALEMENTS = "signalements"

    const val ARGUMENT_ID_LOCAL = "idLocal"
    const val DETAIL_SIGNALEMENT = "signalement/{$ARGUMENT_ID_LOCAL}"
    const val CONFIRMATION_RESOLUTION =
        "signalement/{$ARGUMENT_ID_LOCAL}/confirmation"

    fun detailSignalement(idLocal: String): String = "signalement/$idLocal"

    fun confirmationResolution(idLocal: String): String =
        "signalement/$idLocal/confirmation"
}
