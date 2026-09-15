package mg.itu.tatitra_app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.ui.components.CompteAReboursResolution
import mg.itu.tatitra_app.ui.components.StatutBadge
import mg.itu.tatitra_app.util.formaterCoordonnees
import mg.itu.tatitra_app.util.formaterDateHeure
import mg.itu.tatitra_app.util.formaterDateIso
import java.io.File

@Composable
fun EcranDetailSignalementRoute(
    idLocal: String,
    onRetour: () -> Unit,
    onOuvrirConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailSignalementViewModel = viewModel(
        factory = DetailSignalementViewModel.factory(idLocal)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EcranDetailSignalement(
        uiState = uiState,
        onRetour = onRetour,
        onProposer = viewModel::proposerResolution,
        onOuvrirConfirmation = onOuvrirConfirmation,
        onRechargerMeta = viewModel::rechargerDetailServeur,
        onMessageAffiche = viewModel::messageAffiche,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranDetailSignalement(
    uiState: DetailSignalementUiState,
    onRetour: () -> Unit,
    onProposer: () -> Unit,
    onOuvrirConfirmation: () -> Unit,
    onRechargerMeta: () -> Unit,
    onMessageAffiche: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        onMessageAffiche()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
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
        if (uiState.charge) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Chargement…")
            }
            return@Scaffold
        }

        val signalement = uiState.signalement
        if (signalement == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Signalement introuvable.")
            }
            return@Scaffold
        }

        ContenuDetail(
            uiState = uiState,
            signalement = signalement,
            onProposer = onProposer,
            onOuvrirConfirmation = onOuvrirConfirmation,
            onRechargerMeta = onRechargerMeta,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
private fun ContenuDetail(
    uiState: DetailSignalementUiState,
    signalement: Signalement,
    onProposer: () -> Unit,
    onOuvrirConfirmation: () -> Unit,
    onRechargerMeta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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

        PhotoSignalement(sourcePhoto = signalement.sourcePhoto)

        Text(
            text = signalement.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        LigneInfo("Créé le", formaterDateHeure(signalement.dateCreation))
        LigneInfo(
            "Localisation (GPS)",
            formaterCoordonnees(signalement.latitude, signalement.longitude)
        )
        LigneInfo("Synchronisé", if (signalement.synchronise) "Oui" else "Non")
        signalement.serverId?.let { LigneInfo("Id serveur", it) }
        signalement.resolutionProposeePar?.let {
            LigneInfo("Résolution proposée par", it)
        }
        signalement.dateLimiteConfirmation?.let {
            LigneInfo("Confirmer avant", formaterDateIso(it))
        }
        signalement.motifReouverture?.let {
            LigneInfo("Motif de réouverture", it)
        }

        SectionResolution(
            uiState = uiState,
            onProposer = onProposer,
            onOuvrirConfirmation = onOuvrirConfirmation,
            onRechargerMeta = onRechargerMeta
        )
    }
}

@Composable
private fun SectionResolution(
    uiState: DetailSignalementUiState,
    onProposer: () -> Unit,
    onOuvrirConfirmation: () -> Unit,
    onRechargerMeta: () -> Unit
) {
    val disabled = uiState.actionEnCours
    val signalement = uiState.signalement

    when {
        uiState.metaResolutionIncomplete -> {
            Text(
                text = "Proposition de résolution incomplète (données serveur manquantes).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onRechargerMeta,
                enabled = !disabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Réessayer le chargement")
            }
        }

        uiState.peutProposerResolution() -> {
            Text(
                text = "Résolution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Proposez une résolution si le problème paraît corrigé. " +
                    "L'administration devra confirmer sous 7 jours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onProposer,
                enabled = !disabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (disabled) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Proposer une résolution")
            }
        }

        uiState.peutConfirmerOuRouvrir() -> {
            Text(
                text = "Confirmation citoyenne",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            CompteAReboursResolution(
                echeanceIso = signalement?.dateLimiteConfirmation
            )
            Text(
                text = "Ouvrez l'écran de confirmation pour valider ou signaler " +
                    "que le problème est toujours présent.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onOuvrirConfirmation,
                enabled = !disabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmer ou refuser")
            }
        }

        uiState.attenteConfirmationAdmin() -> {
            CompteAReboursResolution(
                echeanceIso = signalement?.dateLimiteConfirmation,
                titre = "En attente de l'administration (J+7)"
            )
            Text(
                text = "Votre proposition est en attente de confirmation par l'administration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onOuvrirConfirmation,
                enabled = !disabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voir le compte à rebours")
            }
        }

        signalement?.synchronise == false -> {
            Text(
                text = "Synchronisez ce signalement pour accéder aux actions de résolution.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoSignalement(sourcePhoto: String?) {
    if (sourcePhoto.isNullOrBlank()) {
        Text(
            text = "Aucune photo jointe.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val modele = if (sourcePhoto.startsWith("http://") || sourcePhoto.startsWith("https://")) {
        sourcePhoto
    } else {
        File(sourcePhoto)
    }

    Card(shape = RoundedCornerShape(12.dp)) {
        AsyncImage(
            model = modele,
            contentDescription = "Photo du signalement",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
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
