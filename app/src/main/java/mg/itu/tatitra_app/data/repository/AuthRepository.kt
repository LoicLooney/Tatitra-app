package mg.itu.tatitra_app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import mg.itu.tatitra_app.data.local.StockageSession
import mg.itu.tatitra_app.data.remote.ErreurAuthResponse
import mg.itu.tatitra_app.data.remote.IdentifiantsRequest
import mg.itu.tatitra_app.data.remote.RecuperationRequest
import mg.itu.tatitra_app.data.remote.SessionResponse
import mg.itu.tatitra_app.data.remote.SupabaseAuthApi
import mg.itu.tatitra_app.domain.SessionUtilisateur
import retrofit2.HttpException
import java.io.IOException

/**
 * Point d'accès unique à l'authentification : l'UI ne parle jamais directement à
 * Supabase ni au DataStore.
 *
 * L'API peut être nulle : c'est le cas tant que l'URL et la clé publiable Supabase
 * ne sont pas renseignées au build. Le mode démonstration reste alors le seul chemin,
 * et les messages le disent clairement au lieu de laisser croire à une panne.
 */
class AuthRepository(
    private val api: SupabaseAuthApi?,
    private val preferences: StockageSession
) {

    fun observerSession(): Flow<SessionUtilisateur?> = preferences.session

    fun observerModeDemonstration(): Flow<Boolean> = preferences.modeDemonstration

    val authentificationDisponible: Boolean get() = api != null

    suspend fun connecter(email: String, motDePasse: String): ResultatAuth =
        executer("connexion") { service ->
            val reponse = service.connecter(identifiants = identifiants(email, motDePasse))
            conclure(reponse, email)
        }

    suspend fun creerCompte(email: String, motDePasse: String): ResultatAuth =
        executer("inscription") { service ->
            val reponse = service.creerCompte(identifiants(email, motDePasse))
            conclure(reponse, email)
        }

    /**
     * Demande l'envoi du lien de réinitialisation.
     *
     * Supabase répond 200 même pour une adresse inconnue — c'est volontaire : révéler
     * qu'une adresse n'est pas inscrite renseignerait un attaquant. Le message affiché
     * reste donc neutre.
     */
    suspend fun demanderReinitialisation(email: String): ResultatAuth =
        executer("réinitialisation") { service ->
            service.envoyerLienReinitialisation(RecuperationRequest(email.trim()))
            ResultatAuth.ConfirmationRequise(email.trim())
        }

    /**
     * Ferme la session. L'effacement local a lieu quoi qu'il arrive : si le serveur est
     * injoignable, l'utilisateur doit tout de même pouvoir se déconnecter de l'appareil.
     */
    suspend fun deconnecter() {
        val jeton = preferences.session.first()?.jetonAcces
        preferences.effacerSession()
        if (jeton.isNullOrBlank()) return
        try {
            api?.deconnecter("Bearer $jeton")
        } catch (erreur: IOException) {
            Log.w(TAG, "Déconnexion serveur impossible (réseau) : ${erreur.message}")
        } catch (erreur: HttpException) {
            Log.w(TAG, "Déconnexion serveur refusée : ${erreur.code()}")
        }
    }

    suspend fun continuerEnDemonstration() {
        preferences.activerModeDemonstration()
    }

    // --- Interne -------------------------------------------------------------

    private fun identifiants(email: String, motDePasse: String) =
        IdentifiantsRequest(email = email.trim(), password = motDePasse)

    private suspend fun executer(
        operation: String,
        appel: suspend (SupabaseAuthApi) -> ResultatAuth
    ): ResultatAuth {
        val service = api ?: return ResultatAuth.Echec(
            "L'authentification n'est pas configurée sur cette version de l'application. " +
                "Utilisez le mode démonstration."
        )
        return try {
            appel(service)
        } catch (erreur: IOException) {
            Log.w(TAG, "$operation impossible (réseau) : ${erreur.message}")
            ResultatAuth.Echec("Pas de connexion. Vérifiez votre réseau et réessayez.")
        } catch (erreur: HttpException) {
            Log.w(TAG, "$operation refusée : ${erreur.code()}")
            ResultatAuth.Echec(messageLisible(erreur))
        }
    }

    /**
     * Transforme la réponse de Supabase en session, ou en demande de confirmation
     * lorsque le projet exige de valider l'adresse avant d'ouvrir une session.
     */
    private suspend fun conclure(reponse: SessionResponse, emailSaisi: String): ResultatAuth {
        val jeton = reponse.accessToken
        val email = reponse.user?.email ?: reponse.email ?: emailSaisi.trim()

        if (jeton.isNullOrBlank()) {
            return ResultatAuth.ConfirmationRequise(email)
        }

        val session = SessionUtilisateur(
            idUtilisateur = reponse.user?.id ?: reponse.id.orEmpty(),
            email = email,
            jetonAcces = jeton,
            jetonRafraichissement = reponse.refreshToken.orEmpty(),
            expireLeMs = System.currentTimeMillis() +
                (reponse.expiresIn ?: DUREE_JETON_PAR_DEFAUT_SECONDES) * 1_000L
        )
        preferences.enregistrerSession(session)
        return ResultatAuth.Succes(session)
    }

    /**
     * Traduit le refus de Supabase, dont les messages sont en anglais et parfois
     * techniques, en une phrase que l'utilisateur peut suivre.
     */
    private fun messageLisible(erreur: HttpException): String {
        val corps = try {
            erreur.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        val message = corps?.let { lireErreur(it)?.premierMessage() }.orEmpty()

        return when {
            message.contains("Invalid login", ignoreCase = true) ||
                message.contains("invalid_grant", ignoreCase = true) ->
                "E-mail ou mot de passe incorrect."

            message.contains("Email not confirmed", ignoreCase = true) ->
                "Adresse non confirmée. Ouvrez le lien reçu par e-mail avant de vous connecter."

            message.contains("already registered", ignoreCase = true) ||
                message.contains("already been registered", ignoreCase = true) ->
                "Un compte existe déjà avec cette adresse. Connectez-vous."

            message.contains("Password should be", ignoreCase = true) ->
                "Mot de passe trop court ou trop simple."

            // Supabase refuse les domaines sans enregistrement MX : une adresse en
            // « @exemple.mg » passe la validation locale mais pas la sienne.
            message.contains("is invalid", ignoreCase = true) ||
                erreur.code() == 422 && message.contains("email", ignoreCase = true) ->
                "Adresse e-mail refusée : vérifiez le domaine. Une adresse fictive " +
                    "(exemple.mg, test.com…) n'est pas acceptée."

            erreur.code() == 429 ->
                "Trop de tentatives. Patientez quelques minutes avant de réessayer."

            message.isNotBlank() -> message

            else -> "Connexion refusée (HTTP ${erreur.code()})."
        }
    }

    private fun lireErreur(corps: String): ErreurAuthResponse? = try {
        Gson().fromJson(corps, ErreurAuthResponse::class.java)
    } catch (_: JsonSyntaxException) {
        null
    }

    private companion object {
        const val TAG = "AuthRepository"
        const val DUREE_JETON_PAR_DEFAUT_SECONDES = 3_600L
    }
}
