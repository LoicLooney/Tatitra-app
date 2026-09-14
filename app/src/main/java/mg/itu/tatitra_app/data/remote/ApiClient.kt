package mg.itu.tatitra_app.data.remote

import mg.itu.tatitra_app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construit l'unique client Retrofit de l'application.
 * L'URL de base vient de BuildConfig (voir app/build.gradle.kts) : aucune adresse en dur dans le code.
 */
object ApiClient {

    private const val TIMEOUT_SECONDES = 20L

    val api: TatitraApi by lazy { construireApi(BuildConfig.API_BASE_URL) }

    private fun construireApi(baseUrl: String): TatitraApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(construireClientHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TatitraApi::class.java)

    private fun construireClientHttp(): OkHttpClient {
        val journalisation = HttpLoggingInterceptor().apply {
            // Les corps de requête ne sont journalisés qu'en debug (NF-07 : pas de donnée exposée en release).
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(journalisation)
            // Un backend de démo injoignable doit échouer vite : le worker réessaiera.
            .connectTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDES, TimeUnit.SECONDS)
            .build()
    }
}
