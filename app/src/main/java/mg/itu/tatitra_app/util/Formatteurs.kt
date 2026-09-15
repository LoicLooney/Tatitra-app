package mg.itu.tatitra_app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private val FORMAT_DATE_HEURE = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

fun formaterDateHeure(horodatage: Long): String = FORMAT_DATE_HEURE.format(Date(horodatage))

fun formaterDateIso(valeur: String?): String {
    val millis = parserDateIsoEnMillis(valeur) ?: return "—"
    return formaterDateHeure(millis)
}

fun formaterCoordonnees(latitude: Double?, longitude: Double?): String =
    if (latitude == null || longitude == null) {
        "Non renseignée"
    } else {
        String.format(Locale.FRANCE, "%.6f, %.6f", latitude, longitude)
    }

// minSdk 24 : pas de java.time obligatoire.
fun parserDateIsoEnMillis(valeur: String?): Long? {
    if (valeur.isNullOrBlank()) return null
    val textes = listOf(valeur.trim(), valeur.trim().replace(" ", "T"))
    val motifs = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (texte in textes) {
        for (motif in motifs) {
            try {
                val format = SimpleDateFormat(motif, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                format.parse(texte)?.time?.let { return it }
            } catch (_: Exception) {
            }
        }
    }
    return null
}

data class CompteARebours(
    val jours: Long,
    val heures: Long,
    val minutes: Long,
    val secondes: Long,
    val expire: Boolean
) {
    val libelleCourt: String
        get() = if (expire) {
            "Délai dépassé"
        } else {
            String.format(
                Locale.FRANCE,
                "%d j %02d h %02d min %02d s",
                jours,
                heures,
                minutes,
                secondes
            )
        }
}

fun calculerCompteARebours(
    echeanceIso: String?,
    maintenantMs: Long = System.currentTimeMillis()
): CompteARebours? {
    val echeanceMs = parserDateIsoEnMillis(echeanceIso) ?: return null
    val reste = echeanceMs - maintenantMs
    if (reste <= 0L) {
        return CompteARebours(0, 0, 0, 0, expire = true)
    }
    val jours = TimeUnit.MILLISECONDS.toDays(reste)
    val apresJours = reste - TimeUnit.DAYS.toMillis(jours)
    val heures = TimeUnit.MILLISECONDS.toHours(apresJours)
    val apresHeures = apresJours - TimeUnit.HOURS.toMillis(heures)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(apresHeures)
    val apresMinutes = apresHeures - TimeUnit.MINUTES.toMillis(minutes)
    val secondes = TimeUnit.MILLISECONDS.toSeconds(apresMinutes)
    return CompteARebours(jours, heures, minutes, secondes, expire = false)
}
