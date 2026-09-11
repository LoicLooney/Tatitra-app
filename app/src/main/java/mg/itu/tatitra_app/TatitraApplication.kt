package mg.itu.tatitra_app

import android.app.Application
import mg.itu.tatitra_app.data.local.TatitraDatabase
import mg.itu.tatitra_app.data.remote.ApiClient
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.worker.SyncScheduler

/**
 * Conteneur de dépendances de l'application : une seule base Room, un seul client
 * Retrofit, un seul Repository partagé par les ViewModels et le worker de synchronisation.
 */
class ConteneurApplication(application: Application) {

    val signalementRepository: SignalementRepository by lazy {
        SignalementRepository(
            dao = TatitraDatabase.obtenirInstance(application).signalementDao(),
            api = ApiClient.api
        )
    }
}

class TatitraApplication : Application() {

    lateinit var container: ConteneurApplication
        private set

    override fun onCreate() {
        super.onCreate()
        container = ConteneurApplication(this)

        // Rattrape les signalements restés en attente si l'application n'est pas rouverte.
        SyncScheduler.planifierSynchronisationPeriodique(this)
    }
}
