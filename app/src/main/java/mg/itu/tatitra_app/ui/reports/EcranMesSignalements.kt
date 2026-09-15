package mg.itu.tatitra_app.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.RoleResolution
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement
import mg.itu.tatitra_app.ui.components.StatutBadge
import mg.itu.tatitra_app.util.formaterDateHeure

@Composable
fun EcranMesSignalementsRoute(
    onRetour: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    onOuvrirConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MesSignalementsViewModel = viewModel(factory = MesSignalementsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EcranMesSignalements(
        uiState = uiState,
        onRetour = onRetour,
        onOuvrirSignalement = onOuvrirSignalement,
        onOuvrirConfirmation = onOuvrirConfirmation,
        onFiltrerStatut = viewModel::filtrerStatut,
        onFiltrerCategorie = viewModel::filtrerCategorie,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranMesSignalements(
    uiState: MesSignalementsUiState,
    onRetour: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    onOuvrirConfirmation: (String) -> Unit,
    onFiltrerStatut: (StatutSignalement?) -> Unit,
    onFiltrerCategorie: (Categorie?) -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FiltresMesSignalements(
                statut = uiState.filtreStatut,
                categorie = uiState.filtreCategorie,
                onFiltrerStatut = onFiltrerStatut,
                onFiltrerCategorie = onFiltrerCategorie,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.signalementsFiltres.isEmpty()) {
                Text(
                    text = if (uiState.signalements.isEmpty()) {
                        "Aucun signalement pour le moment. Créez-en un depuis l'accueil."
                    } else {
                        "Aucun signalement pour ces filtres."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.signalementsFiltres,
                    key = { it.idLocal }
                ) { signalement ->
                    CarteSignalement(
                        signalement = signalement,
                        onClick = { onOuvrirSignalement(signalement.idLocal) },
                        onConfirmer = {
                            onOuvrirConfirmation(signalement.idLocal)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltresMesSignalements(
    statut: StatutSignalement?,
    categorie: Categorie?,
    onFiltrerStatut: (StatutSignalement?) -> Unit,
    onFiltrerCategorie: (Categorie?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Statut", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = statut == null,
                onClick = { onFiltrerStatut(null) },
                label = { Text("Tous") }
            )
            StatutSignalement.entries.forEach { valeur ->
                FilterChip(
                    selected = statut == valeur,
                    onClick = { onFiltrerStatut(valeur) },
                    label = { Text(valeur.libelle) }
                )
            }
        }
        Text("Catégorie", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = categorie == null,
                onClick = { onFiltrerCategorie(null) },
                label = { Text("Toutes") }
            )
            Categorie.entries.forEach { valeur ->
                FilterChip(
                    selected = categorie == valeur,
                    onClick = { onFiltrerCategorie(valeur) },
                    label = { Text(valeur.libelle) }
                )
            }
        }
    }
}

@Composable
private fun CarteSignalement(
    signalement: Signalement,
    onClick: () -> Unit,
    onConfirmer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rouvert = signalement.statut == StatutSignalement.REOUVERT_NON_RESOLU
    val aConfirmer = signalement.statut == StatutSignalement.RESOLUTION_A_CONFIRMER &&
        signalement.resolutionProposeePar == RoleResolution.ADMIN

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                rouvert -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                aConfirmer -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            rouvert -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f))
            aConfirmer -> BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f))
            else -> null
        }
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
            if (rouvert && !signalement.motifReouverture.isNullOrBlank()) {
                Text(
                    text = "Motif : ${signalement.motifReouverture}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (aConfirmer) {
                TextButton(
                    onClick = {
                        onConfirmer()
                    }
                ) {
                    Text("Confirmer la résolution")
                }
            }
        }
    }
}
