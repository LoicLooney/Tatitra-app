package mg.itu.tatitra_app.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Contrat REST de Supabase Auth (GoTrue).
 *
 * On appelle l'API HTTP directement, avec le Retrofit déjà présent dans le projet,
 * plutôt que d'ajouter le SDK Supabase : quatre requêtes suffisent ici, et cela évite
 * une dépendance lourde pour un besoin que l'architecture existante couvre déjà.
 *
 * Toutes les requêtes portent l'en-tête « apikey » (clé publiable), ajouté par
 * l'intercepteur de SupabaseAuthClient.
 */
interface SupabaseAuthApi {

    /** POST /auth/v1/token?grant_type=password — connexion par e-mail et mot de passe. */
    @POST("auth/v1/token")
    suspend fun connecter(
        @Query("grant_type") typeAutorisation: String = "password",
        @Body identifiants: IdentifiantsRequest
    ): SessionResponse

    /** POST /auth/v1/signup — création de compte. */
    @POST("auth/v1/signup")
    suspend fun creerCompte(@Body identifiants: IdentifiantsRequest): SessionResponse

    /** POST /auth/v1/recover — envoi du lien de réinitialisation du mot de passe. */
    @POST("auth/v1/recover")
    suspend fun envoyerLienReinitialisation(@Body requete: RecuperationRequest)

    /** POST /auth/v1/logout — invalide le jeton côté serveur. */
    @POST("auth/v1/logout")
    suspend fun deconnecter(@Header("Authorization") autorisation: String)
}
