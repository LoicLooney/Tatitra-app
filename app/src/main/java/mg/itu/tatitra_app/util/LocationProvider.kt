package mg.itu.tatitra_app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Coordonnées GPS d'un signalement. */
data class Position(val latitude: Double, val longitude: Double)

/**
 * Lecture de la position du téléphone via le Fused Location Provider.
 * Toujours suspend : l'attente d'un point GPS ne bloque pas l'interface (NF-12).
 */
object LocationProvider {

    private const val TAG = "LocationProvider"

    fun permissionAccordee(context: Context): Boolean =
        estAccordee(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            estAccordee(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    /**
     * Retourne la position courante, ou null si la permission manque, si le GPS est
     * coupé ou si aucun point n'a pu être obtenu. L'appelant affiche alors un message
     * clair et propose de réessayer (F-CIT-05, T07).
     */
    @SuppressLint("MissingPermission") // Vérifié juste au-dessus par permissionAccordee().
    suspend fun recupererPositionActuelle(context: Context): Position? {
        if (!permissionAccordee(context)) return null

        // Position approximative acceptée en secours si le GPS précis n'est pas autorisé.
        val precision = if (estAccordee(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val requete = CurrentLocationRequest.Builder()
            .setPriority(precision)
            .setDurationMillis(DUREE_MAX_RECHERCHE_MS)
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.getCurrentLocation(requete, null)
                .addOnSuccessListener { position ->
                    if (position == null) {
                        Log.w(TAG, "Aucun point GPS obtenu dans le délai imparti")
                        continuation.resume(null)
                    } else {
                        continuation.resume(Position(position.latitude, position.longitude))
                    }
                }
                .addOnFailureListener { erreur ->
                    Log.w(TAG, "Lecture GPS impossible : ${erreur.message}")
                    continuation.resume(null)
                }
        }
    }

    private fun estAccordee(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private const val DUREE_MAX_RECHERCHE_MS = 10_000L
}
