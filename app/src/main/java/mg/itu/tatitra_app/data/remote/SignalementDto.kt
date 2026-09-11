package mg.itu.tatitra_app.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Corps envoyé à POST /api/signalements (§11.2 du cahier des charges).
 * `clientId` = identifiant généré sur le téléphone, clé d'idempotence côté serveur.
 */
data class SignalementRequest(
    @SerializedName("clientId") val clientId: String,
    @SerializedName("categorie") val categorie: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("photoUrl") val photoUrl: String?,
    @SerializedName("isDemo") val isDemo: Boolean
)

/** Réponse du serveur pour un signalement (création, détail ou liste). */
data class SignalementResponse(
    @SerializedName("id") val id: String,
    @SerializedName("clientId") val clientId: String? = null,
    @SerializedName("categorie") val categorie: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("photoUrl") val photoUrl: String? = null,
    @SerializedName("statut") val statut: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

/** Réponse de POST /api/uploads : l'URL publique du fichier stocké. */
data class UploadResponse(
    @SerializedName("url") val url: String
)
