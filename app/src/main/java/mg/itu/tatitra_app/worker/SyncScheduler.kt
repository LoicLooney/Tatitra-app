package mg.itu.tatitra_app.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Planification de la synchronisation.
 *
 * Deux déclencheurs complémentaires : une demande ponctuelle après chaque création
 * de signalement, et un filet de sécurité périodique si l'application n'est jamais rouverte.
 */
object SyncScheduler {

    private const val TRAVAIL_PONCTUEL = "tatitra_sync_ponctuelle"
    private const val TRAVAIL_PERIODIQUE = "tatitra_sync_periodique"
    private const val DELAI_REPRISE_SECONDES = 30L
    private const val INTERVALLE_PERIODIQUE_MINUTES = 15L

    /** Demande une synchronisation dès qu'un réseau est disponible. */
    fun demanderSynchronisation(context: Context) {
        val requete = OneTimeWorkRequestBuilder<SyncSignalementsWorker>()
            .setConstraints(contraintesReseau())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, DELAI_REPRISE_SECONDES, TimeUnit.SECONDS)
            .build()

        // KEEP : inutile d'empiler plusieurs passes, une seule traite toute la file d'attente.
        WorkManager.getInstance(context)
            .enqueueUniqueWork(TRAVAIL_PONCTUEL, ExistingWorkPolicy.KEEP, requete)
    }

    /** Filet de sécurité : relance régulière tant qu'il reste des signalements non envoyés. */
    fun planifierSynchronisationPeriodique(context: Context) {
        val requete = PeriodicWorkRequestBuilder<SyncSignalementsWorker>(
            INTERVALLE_PERIODIQUE_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(contraintesReseau())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRAVAIL_PERIODIQUE,
            ExistingPeriodicWorkPolicy.KEEP,
            requete
        )
    }

    private fun contraintesReseau(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
