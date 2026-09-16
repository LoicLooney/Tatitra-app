package mg.itu.tatitra_app.data.repository

import mg.itu.tatitra_app.domain.SessionUtilisateur

/**
 * Issue d'une opération d'authentification.
 *
 * [ConfirmationRequise] n'est ni un succès ni un échec : le compte est bien créé, mais
 * le projet Supabase exige la confirmation de l'adresse avant d'ouvrir une session.
 * Le confondre avec une erreur laisserait l'utilisateur croire que l'inscription a raté.
 */
sealed interface ResultatAuth {

    data class Succes(val session: SessionUtilisateur) : ResultatAuth

    data class ConfirmationRequise(val email: String) : ResultatAuth

    data class Echec(val message: String) : ResultatAuth
}
