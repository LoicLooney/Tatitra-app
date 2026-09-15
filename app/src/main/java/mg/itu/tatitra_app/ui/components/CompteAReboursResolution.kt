package mg.itu.tatitra_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mg.itu.tatitra_app.util.CompteARebours
import mg.itu.tatitra_app.util.calculerCompteARebours
import mg.itu.tatitra_app.util.formaterDateIso

@Composable
fun CompteAReboursResolution(
    echeanceIso: String?,
    modifier: Modifier = Modifier,
    titre: String = "Délai de confirmation (J+7)"
) {
    var maintenant by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(echeanceIso) {
        while (true) {
            maintenant = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val compte = remember(echeanceIso, maintenant) {
        calculerCompteARebours(echeanceIso, maintenant)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Échéance : ${formaterDateIso(echeanceIso)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                compte == null -> {
                    Text(
                        text = "Échéance inconnue — synchronisez pour actualiser.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                compte.expire -> {
                    Text(
                        text = "Délai dépassé — le dossier sera rouvert automatiquement.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    AffichageUnites(compte)
                }
            }
        }
    }
}

@Composable
private fun AffichageUnites(compte: CompteARebours) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniteTemps(valeur = compte.jours, libelle = "jours")
        UniteTemps(valeur = compte.heures, libelle = "heures")
        UniteTemps(valeur = compte.minutes, libelle = "min")
        UniteTemps(valeur = compte.secondes, libelle = "sec")
    }
}

@Composable
private fun UniteTemps(valeur: Long, libelle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d", valeur),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = libelle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
