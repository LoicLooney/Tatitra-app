package mg.itu.tatitra_app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import mg.itu.tatitra_app.data.local.StockageSession
import mg.itu.tatitra_app.data.remote.IdentifiantsRequest
import mg.itu.tatitra_app.data.remote.RecuperationRequest
import mg.itu.tatitra_app.data.remote.SessionResponse
import mg.itu.tatitra_app.data.remote.SupabaseAuthApi
import mg.itu.tatitra_app.data.remote.UtilisateurResponse
import mg.itu.tatitra_app.domain.SessionUtilisateur
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Stockage de session en mémoire, au comportement du DataStore. */
class FauxStockageSession : StockageSession {

    private val _session = MutableStateFlow<SessionUtilisateur?>(null)
    private val _demonstration = MutableStateFlow(false)

    override val session: Flow<SessionUtilisateur?> = _session
    override val modeDemonstration: Flow<Boolean> = _demonstration

    var nombreEffacements = 0
        private set

    override suspend fun enregistrerSession(session: SessionUtilisateur) {
        _session.value = session
        _demonstration.value = false
    }

    override suspend fun effacerSession() {
        nombreEffacements += 1
        _session.value = null
        _demonstration.value = false
    }

    override suspend fun activerModeDemonstration() {
        _demonstration.value = true
    }

    fun sessionCourante(): SessionUtilisateur? = _session.value
    fun enDemonstration(): Boolean = _demonstration.value
}

/** Comportement simulé d'un appel à Supabase Auth. */
sealed interface ReponseAuthSimulee {
    data class Ok(val reponse: SessionResponse) : ReponseAuthSimulee
    data class Panne(val message: String = "réseau coupé") : ReponseAuthSimulee
    data class Refus(val code: Int, val corps: String = "") : ReponseAuthSimulee
}

class FausseSupabaseAuthApi(
    var reponseConnexion: ReponseAuthSimulee = ReponseAuthSimulee.Ok(sessionServeur()),
    var reponseInscription: ReponseAuthSimulee = ReponseAuthSimulee.Ok(sessionServeur()),
    var reinitialisationEnPanne: Boolean = false
) : SupabaseAuthApi {

    val connexionsRecues = mutableListOf<IdentifiantsRequest>()
    val inscriptionsRecues = mutableListOf<IdentifiantsRequest>()
    val reinitialisationsRecues = mutableListOf<String>()
    val deconnexionsRecues = mutableListOf<String>()

    override suspend fun connecter(
        typeAutorisation: String,
        identifiants: IdentifiantsRequest
    ): SessionResponse {
        connexionsRecues += identifiants
        return resoudre(reponseConnexion)
    }

    override suspend fun creerCompte(identifiants: IdentifiantsRequest): SessionResponse {
        inscriptionsRecues += identifiants
        return resoudre(reponseInscription)
    }

    override suspend fun envoyerLienReinitialisation(requete: RecuperationRequest) {
        if (reinitialisationEnPanne) throw IOException("réseau coupé")
        reinitialisationsRecues += requete.email
    }

    override suspend fun deconnecter(autorisation: String) {
        deconnexionsRecues += autorisation
    }

    private fun resoudre(comportement: ReponseAuthSimulee): SessionResponse =
        when (comportement) {
            is ReponseAuthSimulee.Ok -> comportement.reponse
            is ReponseAuthSimulee.Panne -> throw IOException(comportement.message)
            is ReponseAuthSimulee.Refus -> throw HttpException(
                Response.error<SessionResponse>(
                    comportement.code,
                    comportement.corps.toResponseBody("application/json".toMediaType())
                )
            )
        }
}

fun sessionServeur(
    accessToken: String? = "jeton-acces",
    refreshToken: String? = "jeton-rafraichissement",
    expiresIn: Long? = 3600,
    idUtilisateur: String? = "u-1",
    email: String? = "rakoto@exemple.mg"
) = SessionResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    user = UtilisateurResponse(id = idUtilisateur, email = email)
)
