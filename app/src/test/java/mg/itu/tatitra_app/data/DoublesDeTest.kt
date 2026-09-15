package mg.itu.tatitra_app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import mg.itu.tatitra_app.data.local.SignalementDao
import mg.itu.tatitra_app.data.local.SignalementEntity
import mg.itu.tatitra_app.data.remote.ResolutionReopenRequest
import mg.itu.tatitra_app.data.remote.ResolutionRoleRequest
import mg.itu.tatitra_app.data.remote.SignalementRequest
import mg.itu.tatitra_app.data.remote.SignalementResponse
import mg.itu.tatitra_app.data.remote.TatitraApi
import mg.itu.tatitra_app.data.remote.UploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Doubles de test : une base et une API en mémoire.
 *
 * Ils permettent de tester le Repository — le cœur de l'offline-first — sans téléphone,
 * sans base SQLite et sans serveur, donc en quelques millisecondes sur la JVM.
 */

/** Base locale simulée : une simple map, avec le même comportement que le DAO Room. */
class FauxSignalementDao : SignalementDao {

    private val lignes = MutableStateFlow<Map<String, SignalementEntity>>(emptyMap())

    /** Nombre d'écritures : sert à vérifier qu'on ne réécrit pas inutilement. */
    var nombreMisesAJour = 0
        private set

    override suspend fun inserer(item: SignalementEntity) {
        lignes.value = lignes.value + (item.idLocal to item)
    }

    override suspend fun mettreAJour(item: SignalementEntity) {
        nombreMisesAJour += 1
        lignes.value = lignes.value + (item.idLocal to item)
    }

    override fun observerTout(): Flow<List<SignalementEntity>> =
        lignes.map { table -> table.values.sortedByDescending { it.dateCreation } }

    override fun observerParId(idLocal: String): Flow<SignalementEntity?> =
        lignes.map { it[idLocal] }

    override suspend fun recupererParId(idLocal: String): SignalementEntity? = lignes.value[idLocal]

    override suspend fun recupererEnAttenteDeSync(): List<SignalementEntity> =
        lignes.value.values.filter { !it.synchronise }.sortedBy { it.dateCreation }

    override fun observerNombreEnAttente(): Flow<Int> =
        lignes.map { table -> table.values.count { !it.synchronise } }

    override suspend fun supprimer(idLocal: String) {
        lignes.value = lignes.value - idLocal
    }

    fun contenu(): List<SignalementEntity> = lignes.value.values.toList()
}

/** Comportement que l'API simulée doit adopter pour un appel donné. */
sealed interface ReponseSimulee {
    data class Ok(val reponse: SignalementResponse) : ReponseSimulee
    data class Panne(val message: String = "réseau coupé") : ReponseSimulee
    data class Refus(val code: Int, val corps: String = "") : ReponseSimulee
}

/** API simulée : renvoie ce qu'on lui demande et enregistre les appels reçus. */
class FausseTatitraApi(
    var reponseCreation: ReponseSimulee = ReponseSimulee.Ok(reponseServeur()),
    var reponseListe: List<SignalementResponse> = emptyList(),
    var reponseResolution: ReponseSimulee = ReponseSimulee.Ok(reponseServeur()),
    var urlPhoto: String? = "https://exemple.test/photo.jpg",
    /** Simule une coupure réseau sur GET /api/signalements. */
    var listeEnPanne: Boolean = false
) : TatitraApi {

    val creationsRecues = mutableListOf<SignalementRequest>()
    val rolesRecus = mutableListOf<String>()
    var nombreUploads = 0
        private set

    override suspend fun creerSignalement(request: SignalementRequest): SignalementResponse {
        creationsRecues += request
        return resoudre(reponseCreation)
    }

    override suspend fun listerSignalements(): List<SignalementResponse> {
        if (listeEnPanne) throw IOException("réseau coupé")
        return reponseListe
    }

    override suspend fun recupererSignalement(id: String): SignalementResponse =
        resoudre(reponseResolution)

    override suspend fun envoyerPhoto(photo: MultipartBody.Part): UploadResponse {
        nombreUploads += 1
        return UploadResponse(urlPhoto ?: throw IOException("upload impossible"))
    }

    override suspend fun proposerResolution(
        id: String,
        request: ResolutionRoleRequest,
        role: String
    ): SignalementResponse {
        rolesRecus += request.role
        return resoudre(reponseResolution)
    }

    override suspend fun confirmerResolution(
        id: String,
        request: ResolutionRoleRequest,
        role: String
    ): SignalementResponse {
        rolesRecus += request.role
        return resoudre(reponseResolution)
    }

    override suspend fun rouvrirResolution(
        id: String,
        request: ResolutionReopenRequest,
        role: String
    ): SignalementResponse {
        rolesRecus += request.role
        return resoudre(reponseResolution)
    }

    private fun resoudre(comportement: ReponseSimulee): SignalementResponse = when (comportement) {
        is ReponseSimulee.Ok -> comportement.reponse
        is ReponseSimulee.Panne -> throw IOException(comportement.message)
        is ReponseSimulee.Refus -> throw HttpException(
            Response.error<SignalementResponse>(
                comportement.code,
                comportement.corps.toResponseBody("application/json".toMediaType())
            )
        )
    }
}

fun reponseServeur(
    id: String = "SIG-000001",
    statut: String = "ENVOYE",
    clientId: String? = null,
    photoUrl: String? = null,
    resolutionProposeePar: String? = null,
    dateLimiteConfirmation: String? = null,
    motifReouverture: String? = null
) = SignalementResponse(
    id = id,
    clientId = clientId,
    statut = statut,
    photoUrl = photoUrl,
    resolutionProposeePar = resolutionProposeePar,
    dateLimiteConfirmation = dateLimiteConfirmation,
    motifReouverture = motifReouverture
)

fun entiteLocale(
    idLocal: String = "local-1",
    statut: String = "EN_ATTENTE_SYNC",
    synchronise: Boolean = false,
    serverId: String? = null,
    photoPath: String? = null,
    photoUrl: String? = null
) = SignalementEntity(
    idLocal = idLocal,
    categorie = "ROUTE",
    description = "Chaussée dégradée devant l'arrêt",
    latitude = -18.8792,
    longitude = 47.5079,
    photoPath = photoPath,
    photoUrl = photoUrl,
    statut = statut,
    synchronise = synchronise,
    serverId = serverId,
    dateCreation = 1_700_000_000_000L,
    isDemo = true,
    derniereErreurSync = null
)
