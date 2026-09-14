package mg.itu.tatitra_app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.ui.components.StatutBadge
import mg.itu.tatitra_app.util.formaterCoordonnees
import mg.itu.tatitra_app.util.formaterDateHeure

/**
 * Détail local pour un signalement fictif (J2).
 * Les données réelles serveur arriveront via GET /api/signalements/:id plus tard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranDetailSignalementFictif(
    idLocal: String,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signalement: Signalement? = SignalementsFictifs.parIdLocal(idLocal)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Détail") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (signalement == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Signalement introuvable : $idLocal")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = signalement.categorie.libelle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            StatutBadge(statut = signalement.statut)
            Text(
                text = signalement.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            LigneInfo("Créé le", formaterDateHeure(signalement.dateCreation))
            LigneInfo(
                "Localisation",
                formaterCoordonnees(signalement.latitude, signalement.longitude)
            )
            LigneInfo("Synchronisé", if (signalement.synchronise) "Oui" else "Non")
            LigneInfo("Id local", signalement.idLocal)
            signalement.serverId?.let { LigneInfo("Id serveur", it) }
            Text(
                text = "Aperçu fictif — les actions de résolution arriveront en J5.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LigneInfo(label: String, valeur: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valeur, style = MaterialTheme.typography.bodyLarge)
    }
}
