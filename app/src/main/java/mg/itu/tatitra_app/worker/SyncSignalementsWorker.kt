package mg.itu.tatitra_app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mg.itu.tatitra_app.TatitraApplication

class SyncSignalementsWorker(
    context: Context,
    parametres: WorkerParameters
) : CoroutineWorker(context, parametres) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as TatitraApplication).container
        val repository = container.signalementRepository
        val preferences = container.preferencesRepository

        val resultat = repository.synchroniserEnAttente()
        val rafraichi = repository.rafraichirStatuts()
        // Ne pas marquer « dernière sync » si rien n'a réellement abouti.
        if (resultat.nombreEnvoyes > 0 || rafraichi) {
            preferences.enregistrerDerniereSync()
        }

        Log.i(
            TAG,
            "Synchronisation terminée : ${resultat.nombreEnvoyes} envoyé(s), " +
                "${resultat.nombreEchecs} échec(s), rafraîchi=$rafraichi"
        )

        return when {
            resultat.doitReessayer && runAttemptCount < NOMBRE_TENTATIVES_MAX -> Result.retry()
            resultat.doitReessayer -> Result.failure()
            else -> Result.success()
        }
    }

    private companion object {
        const val TAG = "SyncSignalementsWorker"
        const val NOMBRE_TENTATIVES_MAX = 5
    }
}
