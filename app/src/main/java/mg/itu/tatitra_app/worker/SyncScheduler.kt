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

object SyncScheduler {

    private const val TRAVAIL_PONCTUEL = "tatitra_sync_ponctuelle"
    private const val TRAVAIL_PERIODIQUE = "tatitra_sync_periodique"
    private const val DELAI_REPRISE_SECONDES = 30L
    private const val INTERVALLE_PERIODIQUE_MINUTES = 15L

    fun demanderSynchronisation(context: Context) {
        val requete = OneTimeWorkRequestBuilder<SyncSignalementsWorker>()
            .setConstraints(contraintesReseau())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, DELAI_REPRISE_SECONDES, TimeUnit.SECONDS)
            .build()

        // REPLACE : reprendre si une passe précédente est coincée.
        WorkManager.getInstance(context)
            .enqueueUniqueWork(TRAVAIL_PONCTUEL, ExistingWorkPolicy.REPLACE, requete)
    }

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
