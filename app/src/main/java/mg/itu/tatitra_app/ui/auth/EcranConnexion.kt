package mg.itu.tatitra_app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.itu.tatitra_app.R

@Composable
fun EcranConnexionRoute(
    onConnecte: () -> Unit,
    onModeDemonstration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConnexionViewModel = viewModel(factory = ConnexionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.connecte) {
        if (uiState.connecte) onConnecte()
    }

    EcranConnexion(
        uiState = uiState,
        onIdentifiantChange = viewModel::saisirIdentifiant,
        onMotDePasseChange = viewModel::saisirMotDePasse,
        onBasculerVisibilite = viewModel::basculerVisibiliteMotDePasse,
        onSeConnecter = viewModel::seConnecter,
        onCreerCompte = viewModel::creerCompte,
        onMotDePasseOublie = viewModel::motDePasseOublie,
        onModeDemonstration = { viewModel.continuerEnDemonstration(onModeDemonstration) },
        modifier = modifier
    )
}

/**
 * Écran de connexion, repris du prototype (docs/prototypes) : en-tête logo, deux champs,
 * lien de récupération, action principale, création de compte, puis l'échappatoire
 * « mode démonstration » qui garde l'application utilisable sans compte ni réseau.
 */
@Composable
fun EcranConnexion(
    uiState: ConnexionUiState,
    onIdentifiantChange: (String) -> Unit,
    onMotDePasseChange: (String) -> Unit,
    onBasculerVisibilite: () -> Unit,
    onSeConnecter: () -> Unit,
    onCreerCompte: () -> Unit,
    onMotDePasseOublie: () -> Unit,
    onModeDemonstration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.erreur, uiState.information) {
        val message = uiState.erreur ?: uiState.information ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { paddingInterne ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterne)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            EnteteConnexion()

            Spacer(Modifier.height(30.dp))

            Text(
                text = "Connexion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Accédez à vos signalements et suivez leur traitement.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(22.dp))

            OutlinedTextField(
                value = uiState.identifiant,
                onValueChange = onIdentifiantChange,
                label = { Text("TÉLÉPHONE OU EMAIL") },
                placeholder = { Text("vous@exemple.mg") },
                singleLine = true,
                isError = uiState.erreurIdentifiant != null,
                supportingText = {
                    uiState.erreurIdentifiant?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                enabled = !uiState.enCours,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.motDePasse,
                onValueChange = onMotDePasseChange,
                label = { Text("MOT DE PASSE") },
                singleLine = true,
                isError = uiState.erreurMotDePasse != null,
                supportingText = {
                    uiState.erreurMotDePasse?.let { Text(it) }
                },
                visualTransformation = if (uiState.motDePasseVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = onBasculerVisibilite) {
                        Icon(
                            imageVector = if (uiState.motDePasseVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (uiState.motDePasseVisible) {
                                "Masquer le mot de passe"
                            } else {
                                "Afficher le mot de passe"
                            }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                enabled = !uiState.enCours,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onMotDePasseOublie,
                enabled = !uiState.enCours
            ) {
                Text("Mot de passe oublié ?")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSeConnecter,
                enabled = uiState.peutEnvoyer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.enCours) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("SE CONNECTER", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCreerCompte,
                enabled = uiState.peutEnvoyer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("CRÉER UN COMPTE", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

            SeparateurOu()

            Spacer(Modifier.height(20.dp))

            TextButton(
                onClick = onModeDemonstration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuer en mode démonstration")
            }

            if (!uiState.authentificationDisponible) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "L'authentification Supabase n'est pas configurée sur cette " +
                        "version. Le mode démonstration reste disponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EnteteConnexion(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_symbole),
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )
        Column {
            Text(
                text = "TATITRA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Signaler. Suivre. Améliorer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeparateurOu(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "OU",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
