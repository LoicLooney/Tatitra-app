package mg.itu.tatitra_app.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Contrat REST partagé avec le backend (§11.1 du cahier des charges).
 * Toutes les fonctions sont suspend : aucun appel réseau sur le thread principal (NF-12).
 */
interface TatitraApi {

    /** POST /api/signalements — crée un signalement ; renvoie l'existant si le clientId est déjà connu. */
    @POST("api/signalements")
    suspend fun creerSignalement(@Body request: SignalementRequest): SignalementResponse

    /** GET /api/signalements — liste des signalements connus du serveur. */
    @GET("api/signalements")
    suspend fun listerSignalements(): List<SignalementResponse>

    /** GET /api/signalements/{id} — détail d'un signalement (statut à jour). */
    @GET("api/signalements/{id}")
    suspend fun recupererSignalement(@Path("id") id: String): SignalementResponse

    /** POST /api/uploads — envoie le fichier photo et récupère son URL de stockage. */
    @Multipart
    @POST("api/uploads")
    suspend fun envoyerPhoto(@Part photo: MultipartBody.Part): UploadResponse
}
