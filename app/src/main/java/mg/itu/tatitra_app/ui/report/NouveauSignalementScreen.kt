package mg.itu.tatitra_app.ui.report

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.ReglesSignalement
import mg.itu.tatitra_app.ui.theme.TatitraappTheme
import mg.itu.tatitra_app.util.formaterCoordonnees
import java.io.File

/**
 * Point d'entrée navigable : détient les lanceurs de permissions et d'activités
 * (appareil photo, galerie), et délègue tout le reste au ViewModel.
 */
@Composable
fun NouveauSignalementRoute(
    onRetour: () -> Unit,
    onSignalementEnregistre: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NouveauSignalementViewModel = viewModel(factory = NouveauSignalementViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lanceurAppareilPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { succes -> viewModel.photoPrise(succes) }

    val lanceurPermissionCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee ->
        if (accordee) lanceurAppareilPhoto.launch(viewModel.preparerPriseDePhoto())
    }

    // Photo Picker : pas de permission de stockage à demander (§13.1 du cahier des charges).
    val lanceurGalerie = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { source -> viewModel.photoChoisieDansGalerie(source) }

    val lanceurPermissionPosition = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultats ->
        val accordee = resultats.values.any { it }
        if (accordee) viewModel.recupererPosition() else viewModel.permissionPositionRefusee()
    }

    LaunchedEffect(uiState.idLocalEnregistre) {
        val idLocal = uiState.idLocalEnregistre ?: return@LaunchedEffect
        viewModel.enregistrementTraite()
        onSignalementEnregistre(idLocal)
    }

    NouveauSignalementScreen(
        uiState = uiState,
        onRetour = onRetour,
        onCategorieChangee = viewModel::changerCategorie,
        onDescriptionChangee = viewModel::changerDescription,
        onDonneeDemonstrationChangee = viewModel::changerDonneeDemonstration,
        onPrendrePhoto = { lanceurPermissionCamera.launch(Manifest.permission.CAMERA) },
        onChoisirPhoto = {
            lanceurGalerie.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRetirerPhoto = viewModel::retirerPhoto,
        onDemanderPosition = {
            lanceurPermissionPosition.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onEnregistrer = viewModel::enregistrer,
        onMessageAffiche = viewModel::messageAffiche,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NouveauSignalementScreen(
    uiState: NouveauSignalementUiState,
    onRetour: () -> Unit,
    onCategorieChangee: (Categorie) -> Unit,
    onDescriptionChangee: (String) -> Unit,
    onDonneeDemonstrationChangee: (Boolean) -> Unit,
    onPrendrePhoto: () -> Unit,
    onChoisirPhoto: () -> Unit,
    onRetirerPhoto: () -> Unit,
    onDemanderPosition: () -> Unit,
    onEnregistrer: () -> Unit,
    onMessageAffiche: () -> Unit,
    modifier: Modifier = Modifier
) {
    val etatSnackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        etatSnackbar.showSnackbar(message)
        onMessageAffiche()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nouveau signalement") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(etatSnackbar) }
    ) { paddingInterne ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ChampCategorie(
                categorie = uiState.categorie,
                onCategorieChangee = onCategorieChangee
            )

            SectionPhoto(
                photoPath = uiState.photoPath,
                onPrendrePhoto = onPrendrePhoto,
                onChoisirPhoto = onChoisirPhoto,
                onRetirerPhoto = onRetirerPhoto
            )

            SectionPosition(
                uiState = uiState,
                onDemanderPosition = onDemanderPosition
            )

            ChampDescription(
                description = uiState.description,
                estValide = uiState.descriptionEstValide,
                onDescriptionChangee = onDescriptionChangee
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isDemo,
                    onCheckedChange = onDonneeDemonstrationChangee
                )
                Column {
                    Text("Donnée de démonstration", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "À cocher pour un incident simulé présenté en soutenance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onEnregistrer,
                enabled = uiState.peutEnregistrer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.enregistrementEnCours) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Enregistrer", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = "Le signalement est d'abord enregistré sur le téléphone, puis envoyé " +
                    "automatiquement dès qu'une connexion est disponible.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChampCategorie(
    categorie: Categorie,
    onCategorieChangee: (Categorie) -> Unit,
    modifier: Modifier = Modifier
) {
    // État purement visuel : il peut rester dans le Composable (remember).
    var menuOuvert by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Catégorie", style = MaterialTheme.typography.titleSmall)
        ExposedDropdownMenuBox(
            expanded = menuOuvert,
            onExpandedChange = { menuOuvert = it }
        ) {
            TextField(
                value = categorie.libelle,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOuvert) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = menuOuvert,
                onDismissRequest = { menuOuvert = false }
            ) {
                Categorie.entries.forEach { proposition ->
                    DropdownMenuItem(
                        text = { Text(proposition.libelle) },
                        onClick = {
                            onCategorieChangee(proposition)
                            menuOuvert = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionPhoto(
    photoPath: String?,
    onPrendrePhoto: () -> Unit,
    onChoisirPhoto: () -> Unit,
    onRetirerPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Photo", style = MaterialTheme.typography.titleSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onPrendrePhoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Prendre")
            }
            OutlinedButton(onClick = onChoisirPhoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Galerie")
            }
        }

        if (photoPath == null) {
            Text(
                text = "Facultative : un signalement reste valide sans photo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(shape = RoundedCornerShape(12.dp)) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Photo du signalement",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            IconButton(
                onClick = onRetirerPhoto,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Retirer la photo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SectionPosition(
    uiState: NouveauSignalementUiState,
    onDemanderPosition: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Localisation", style = MaterialTheme.typography.titleSmall)

        OutlinedButton(
            onClick = onDemanderPosition,
            enabled = !uiState.rechercheGpsEnCours,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.rechercheGpsEnCours) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text("Recherche de la position…")
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (uiState.aUnePosition) "Actualiser ma position" else "Obtenir ma position")
            }
        }

        Text(
            text = "Latitude / longitude : ${formaterCoordonnees(uiState.latitude, uiState.longitude)}",
            style = MaterialTheme.typography.bodyMedium
        )

        val messageGps = uiState.messageGps
        if (messageGps != null) {
            Text(
                text = messageGps,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ChampDescription(
    description: String,
    estValide: Boolean,
    onDescriptionChangee: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Description", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChangee,
            placeholder = { Text("Ex. : chaussée dégradée devant l'arrêt de bus") },
            minLines = 3,
            isError = description.isNotEmpty() && !estValide,
            supportingText = {
                Text(
                    "${description.length} / ${ReglesSignalement.LONGUEUR_DESCRIPTION_MAX} caractères " +
                        "(minimum ${ReglesSignalement.LONGUEUR_DESCRIPTION_MIN})"
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApercuNouveauSignalement() {
    TatitraappTheme {
        NouveauSignalementScreen(
            uiState = NouveauSignalementUiState(
                description = "Chaussée dégradée - donnée de démonstration",
                latitude = -18.8792,
                longitude = 47.5079
            ),
            onRetour = {},
            onCategorieChangee = {},
            onDescriptionChangee = {},
            onDonneeDemonstrationChangee = {},
            onPrendrePhoto = {},
            onChoisirPhoto = {},
            onRetirerPhoto = {},
            onDemanderPosition = {},
            onEnregistrer = {},
            onMessageAffiche = {}
        )
    }
}
