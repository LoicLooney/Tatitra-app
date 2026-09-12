package mg.itu.tatitra_app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mg.itu.tatitra_app.TatitraApplication

/**
 * Envoie au serveur les signalements créés hors ligne, dès qu'une connexion est disponible.
 * Survit à la fermeture de l'application : c'est WorkManager qui le relance (F-CIT-08).
 */
class SyncSignalementsWorker(
    context: Context,
    parametres: WorkerParameters
) : CoroutineWorker(context, parametres) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as TatitraApplication).container.signalementRepository

        val resultat = repository.synchroniserEnAttente()
        // Une fois les envois faits, on relit les statuts côté serveur (prise en charge, résolution).
        repository.rafraichirStatuts()

        Log.i(
            TAG,
            "Synchronisation terminée : ${resultat.nombreEnvoyes} envoyé(s), " +
                "${resultat.nombreEchecs} échec(s)"
        )

        return when {
            // Panne temporaire : WorkManager réessaiera avec son délai exponentiel.
            resultat.doitReessayer && runAttemptCount < NOMBRE_TENTATIVES_MAX -> Result.retry()
            resultat.doitReessayer -> Result.failure()
            else -> Result.success()
        }
    }

    private companion object {
        const val TAG = "SyncSignalementsWorker"

        // Au-delà, on arrête d'insister : l'utilisateur garde le bouton « Réessayer ».
        const val NOMBRE_TENTATIVES_MAX = 5
    }
}
