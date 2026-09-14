package mg.itu.tatitra_app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.itu.tatitra_app.R
import mg.itu.tatitra_app.data.local.PreferencesDataStore
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.ui.components.StatutBadge
import mg.itu.tatitra_app.ui.theme.TatitraappTheme
import mg.itu.tatitra_app.util.formaterDateHeure

/**
 * Point d'entrée navigable : relie le ViewModel à l'écran.
 * L'écran lui-même reste sans logique métier (§2.2 des règles de code).
 */
@Composable
fun EcranAccueilRoute(
    onNouveauSignalement: () -> Unit,
    onVoirMesSignalements: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccueilViewModel = viewModel(factory = AccueilViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EcranAccueil(
        uiState = uiState,
        onNouveauSignalement = onNouveauSignalement,
        onVoirMesSignalements = onVoirMesSignalements,
        onOuvrirSignalement = onOuvrirSignalement,
        onSynchroniser = viewModel::synchroniserMaintenant,
        onChangerLangue = viewModel::changerLangue,
        onMessageAffiche = viewModel::messageAffiche,
        modifier = modifier
    )
}

@Composable
fun EcranAccueil(
    uiState: AccueilUiState,
    onNouveauSignalement: () -> Unit,
    onVoirMesSignalements: () -> Unit,
    onOuvrirSignalement: (String) -> Unit,
    onSynchroniser: () -> Unit,
    onChangerLangue: (String) -> Unit,
    onMessageAffiche: () -> Unit,
    modifier: Modifier = Modifier
) {
    val etatSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messageSynchronisation) {
        val message = uiState.messageSynchronisation ?: return@LaunchedEffect
        etatSnackbar.showSnackbar(message)
        onMessageAffiche()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(etatSnackbar) }
    ) { paddingInterne ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnteteTatitra()

            Button(
                onClick = onNouveauSignalement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Nouveau signalement", style = MaterialTheme.typography.titleMedium)
            }

            CarteSynthese(
                uiState = uiState,
                onSynchroniser = onSynchroniser
            )

            CartePreferences(
                langue = uiState.langue,
                derniereSyncMs = uiState.derniereSyncMs,
                onChangerLangue = onChangerLangue
            )

            SectionSignalementsRecents(
                uiState = uiState,
                onOuvrirSignalement = onOuvrirSignalement,
                onVoirMesSignalements = onVoirMesSignalements
            )
        }
    }
}

@Composable
private fun EnteteTatitra(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Image(
            painter = painterResource(R.drawable.logo_tatitra_horizontal),
            contentDescription = "TATITRA",
            modifier = Modifier.height(40.dp)
        )
        Text(
            text = "Signalez un problème d'infrastructure publique, même sans connexion.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Préférences DataStore (J4) : langue + dernière sync, relues au démarrage. */
@Composable
private fun CartePreferences(
    langue: String,
    derniereSyncMs: Long?,
    onChangerLangue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Préférences", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Dernière synchronisation : " +
                    if (derniereSyncMs == null) "jamais"
                    else formaterDateHeure(derniereSyncMs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Langue",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onChangerLangue(PreferencesDataStore.LANGUE_FR) },
                    enabled = langue != PreferencesDataStore.LANGUE_FR
                ) {
                    Text(if (langue == PreferencesDataStore.LANGUE_FR) "Français ✓" else "Français")
                }
                OutlinedButton(
                    onClick = { onChangerLangue(PreferencesDataStore.LANGUE_MG) },
                    enabled = langue != PreferencesDataStore.LANGUE_MG
                ) {
                    Text(if (langue == PreferencesDataStore.LANGUE_MG) "Malagasy ✓" else "Malagasy")
                }
            }
        }
    }
}

/** Compteurs « Mes signalements » et action de synchronisation manuelle. */
@Composable
private fun CarteSynthese(
    uiState: AccueilUiState,
    onSynchroniser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Mes signalements", style = MaterialTheme.typography.titleMedium)

            LigneCompteur(
                icone = Icons.Filled.CloudOff,
                libelle = "En attente de synchronisation",
                valeur = uiState.nombreEnAttente
            )
            LigneCompteur(
                icone = Icons.Filled.CloudUpload,
                libelle = "Envoyés",
                valeur = uiState.nombreEnvoyes
            )
            LigneCompteur(
                icone = Icons.Filled.ReportProblem,
                libelle = "Résolutions à confirmer",
                valeur = uiState.nombreAConfirmer
            )
            LigneCompteur(
                icone = Icons.Filled.CheckCircle,
                libelle = "Résolus",
                valeur = uiState.nombreResolus
            )

            if (uiState.nombreEnAttente > 0 || uiState.synchronisationEnCours) {
                HorizontalDivider()
                Text(
                    text = "Ces signalements partiront automatiquement au retour du réseau.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onSynchroniser,
                    enabled = !uiState.synchronisationEnCours,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.synchronisationEnCours) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                        Text("Synchronisation en cours…")
                    } else {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Synchroniser maintenant")
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneCompteur(
    icone: ImageVector,
    libelle: String,
    valeur: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = libelle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valeur.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionSignalementsRecents(
    uiState: AccueilUiState,
    onOuvrirSignalement: (String) -> Unit,
    onVoirMesSignalements: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Derniers signalements",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onVoirMesSignalements) { Text("Tout voir") }
        }

        if (!uiState.aDesSignalements) {
            Text(
                text = "Aucun signalement pour le moment. Utilisez le bouton ci-dessus pour en créer un.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        uiState.signalementsRecents.forEach { signalement ->
            CarteSignalementResume(
                signalement = signalement,
                onClick = { onOuvrirSignalement(signalement.idLocal) }
            )
        }
    }
}

@Composable
private fun CarteSignalementResume(
    signalement: Signalement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

@Preview(showBackground = true)
@Composable
private fun ApercuEcranAccueil() {
    TatitraappTheme {
        EcranAccueil(
            uiState = AccueilUiState(nombreEnAttente = 2, nombreEnvoyes = 5, nombreResolus = 3),
            onNouveauSignalement = {},
            onVoirMesSignalements = {},
            onOuvrirSignalement = {},
            onSynchroniser = {},
            onChangerLangue = {},
            onMessageAffiche = {}
        )
    }
}
