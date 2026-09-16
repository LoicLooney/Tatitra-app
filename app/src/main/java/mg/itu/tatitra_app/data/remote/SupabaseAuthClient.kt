package mg.itu.tatitra_app.data.remote

import mg.itu.tatitra_app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client Retrofit dédié à Supabase Auth.
 *
 * Séparé de [ApiClient] parce qu'il vise un autre hôte et exige un en-tête « apikey »
 * qui n'a rien à faire dans les requêtes vers le backend TATITRA.
 *
 * [estConfigure] est faux tant que l'URL ou la clé publiable manquent : l'application
 * reste alors parfaitement utilisable en mode démonstration, et l'écran de connexion
 * le dit explicitement plutôt que d'échouer au premier appel réseau.
 */
object SupabaseAuthClient {

    private const val TIMEOUT_SECONDES = 20L

    val estConfigure: Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val api: SupabaseAuthApi? by lazy {
        if (estConfigure) construireApi() else null
    }

    private fun construireApi(): SupabaseAuthApi =
        Retrofit.Builder()
            .baseUrl(normaliserUrl(BuildConfig.SUPABASE_URL))
            .client(construireClientHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseAuthApi::class.java)

    /** Retrofit exige une URL de base terminée par « / ». */
    private fun normaliserUrl(url: String): String =
        if (url.endsWith('/')) url else "$url/"

    private fun construireClientHttp(): OkHttpClient {
        val journalisation = HttpLoggingInterceptor().apply {
            // Jamais BODY ici, même en debug : le corps contient le mot de passe en clair.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor { chaine ->
                val requete = chaine.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chaine.proceed(requete)
            }
            .addInterceptor(journalisation)
            .connectTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .build()
    }
}
