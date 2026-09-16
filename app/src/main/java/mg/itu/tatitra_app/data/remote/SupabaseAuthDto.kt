package mg.itu.tatitra_app.data.remote

import com.google.gson.annotations.SerializedName

/** Corps commun à la connexion et à l'inscription. */
data class IdentifiantsRequest(
    val email: String,
    val password: String
)

data class RecuperationRequest(
    val email: String
)

/**
 * Réponse de Supabase Auth.
 *
 * Les jetons sont absents lorsque la confirmation par e-mail est activée dans le projet :
 * l'inscription réussit alors sans ouvrir de session, et l'utilisateur doit d'abord
 * cliquer le lien reçu. Le Repository distingue les deux cas pour l'expliquer.
 */
data class SessionResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: UtilisateurResponse? = null,
    /** Renseigné quand l'inscription renvoie directement l'utilisateur, sans enveloppe. */
    val id: String? = null,
    val email: String? = null,
    @SerializedName("confirmation_sent_at") val confirmationEnvoyeeLe: String? = null
)

data class UtilisateurResponse(
    val id: String? = null,
    val email: String? = null,
    @SerializedName("email_confirmed_at") val emailConfirmeLe: String? = null
)

/**
 * Corps d'erreur de Supabase Auth. Le champ porteur du message change selon
 * l'endpoint et la version de GoTrue : on les accepte tous plutôt que d'afficher
 * un « Erreur 400 » nu à l'utilisateur.
 */
data class ErreurAuthResponse(
    val code: String? = null,
    val error: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null,
    val msg: String? = null,
    val message: String? = null,
    @SerializedName("error_code") val errorCode: String? = null
) {
    fun premierMessage(): String? =
        listOfNotNull(errorDescription, msg, message, error)
            .firstOrNull { it.isNotBlank() }
}
