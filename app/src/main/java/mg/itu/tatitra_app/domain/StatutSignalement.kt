package mg.itu.tatitra_app.domain

/**
 * États d'un signalement (§15.2 du cahier des charges, §2.6 des règles de code).
 *
 * EN_ATTENTE_SYNC est le seul état purement local : il décrit un signalement créé
 * hors ligne et pas encore accepté par le serveur.
 */
enum class StatutSignalement(val libelle: String) {
    EN_ATTENTE_SYNC("En attente de synchronisation"),
    ENVOYE("Envoyé"),
    A_VERIFIER("À vérifier"),
    PRIS_EN_CHARGE("Pris en charge"),
    REJETE("Rejeté"),
    RESOLUTION_A_CONFIRMER("Résolution à confirmer"),
    RESOLU_CONFIRME("Résolu"),
    REOUVERT_NON_RESOLU("Rouvert - non résolu");

    companion object {
        /** Statut de repli si le serveur renvoie une valeur inconnue : on n'invente pas d'état métier. */
        fun depuisCode(code: String?): StatutSignalement =
            entries.firstOrNull { it.name == code } ?: ENVOYE
    }
}
