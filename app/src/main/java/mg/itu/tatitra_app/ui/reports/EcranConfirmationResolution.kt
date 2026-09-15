package mg.itu.tatitra_app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.ui.components.CompteAReboursResolution
import mg.itu.tatitra_app.ui.components.StatutBadge

@Composable
fun EcranConfirmationResolutionRoute(
    idLocal: String,
    onRetour: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailSignalementViewModel = viewModel(
        factory = DetailSignalementViewModel.factory(idLocal)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EcranConfirmationResolution(
        uiState = uiState,
        onRetour = onRetour,
        onConfirmer = viewModel::confirmerResolution,
        onToujoursEndommage = viewModel::declarerToujoursEndommage,
        onRechargerMeta = viewModel::rechargerDetailServeur,
        onMessageAffiche = viewModel::messageAffiche,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranConfirmationResolution(
    uiState: DetailSignalementUiState,
    onRetour: () -> Unit,
    onConfirmer: () -> Unit,
    onToujoursEndommage: () -> Unit,
    onRechargerMeta: () -> Unit,
    onMessageAffiche: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        onMessageAffiche()
        if (message == "Action enregistrée.") {
            onRetour()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Confirmation de résolution") },
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
        when {
            uiState.charge -> {
                Text(
                    text = "Chargement…",
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp)
                )
            }
            uiState.signalement == null -> {
                Text(
                    text = "Signalement introuvable.",
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp)
                )
            }
            else -> {
                ContenuConfirmation(
                    uiState = uiState,
                    signalement = uiState.signalement,
                    onConfirmer = onConfirmer,
                    onToujoursEndommage = onToujoursEndommage,
                    onRechargerMeta = onRechargerMeta,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ContenuConfirmation(
    uiState: DetailSignalementUiState,
    signalement: Signalement,
    onConfirmer: () -> Unit,
    onToujoursEndommage: () -> Unit,
    onRechargerMeta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disabled = uiState.actionEnCours
    val peutAgir = uiState.peutConfirmerOuRouvrir()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = signalement.categorie.libelle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        StatutBadge(statut = signalement.statut)

        Text(
            text = signalement.description,
            style = MaterialTheme.typography.bodyLarge
        )

        CompteAReboursResolution(echeanceIso = signalement.dateLimiteConfirmation)

        Text(
            text = "Sans réponse avant l'échéance, le signalement sera rouvert automatiquement " +
                "(règle J+7).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when {
            uiState.metaResolutionIncomplete -> {
                Text(
                    text = "Données de proposition incomplètes.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = onRechargerMeta,
                    enabled = !disabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Réessayer le chargement")
                }
            }

            peutAgir -> {
                Text(
                    text = "L'administration a proposé une résolution. Confirmez si le problème " +
                        "est corrigé, ou signalez qu'il est toujours présent.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onConfirmer,
                    enabled = !disabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (disabled) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.size(8.dp))
                    }
                    Text("Confirmer la résolution")
                }
                OutlinedButton(
                    onClick = onToujoursEndommage,
                    enabled = !disabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Toujours endommagé")
                }
            }

            uiState.attenteConfirmationAdmin() -> {
                Text(
                    text = "Votre proposition est en attente de confirmation par l'administration. " +
                        "Le compte à rebours ci-dessus indique le délai restant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                Text(
                    text = "Aucune action de confirmation n'est requise pour ce signalement.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
