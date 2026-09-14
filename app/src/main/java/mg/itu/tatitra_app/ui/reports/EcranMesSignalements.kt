package mg.itu.tatitra_app.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.ui.components.StatutBadge
import mg.itu.tatitra_app.util.formaterDateHeure

@Composable
fun EcranMesSignalementsRoute(
    onRetour: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MesSignalementsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EcranMesSignalements(
        uiState = uiState,
        onRetour = onRetour,
        onOuvrirSignalement = onOuvrirSignalement,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranMesSignalements(
    uiState: MesSignalementsUiState,
    onRetour: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mes signalements") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.estFictif) {
                item {
                    Text(
                        text = "Données de démonstration (fictives) — J2",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(
                items = uiState.signalements,
                key = { it.idLocal }
            ) { signalement ->
                CarteSignalement(
                    signalement = signalement,
                    onClick = { onOuvrirSignalement(signalement.idLocal) }
                )
            }
        }
    }
}

@Composable
private fun CarteSignalement(
    signalement: Signalement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Même structure que CarteSignalementResume (Accueil) — cours / projet
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = signalement.categorie.libelle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = signalement.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Text(
                text = formaterDateHeure(signalement.dateCreation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatutBadge(statut = signalement.statut)
        }
    }
}
