package mg.itu.tatitra_app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FORMAT_DATE_HEURE = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

/** Date de création affichée à l'utilisateur, en français. */
fun formaterDateHeure(horodatage: Long): String = FORMAT_DATE_HEURE.format(Date(horodatage))

/** Coordonnées lisibles ; « Non renseignée » si le GPS n'a rien fourni. */
fun formaterCoordonnees(latitude: Double?, longitude: Double?): String =
    if (latitude == null || longitude == null) {
        "Non renseignée"
    } else {
        String.format(Locale.FRANCE, "%.6f, %.6f", latitude, longitude)
    }
