package mg.itu.tatitra_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import mg.itu.tatitra_app.domain.StatutSignalement
import mg.itu.tatitra_app.ui.theme.BleuTatitra
import mg.itu.tatitra_app.ui.theme.GrisTexte
import mg.itu.tatitra_app.ui.theme.OrangeAttente
import mg.itu.tatitra_app.ui.theme.RougeRejet
import mg.itu.tatitra_app.ui.theme.VertResolu

/**
 * Pastille de statut : icône + libellé.
 * La couleur n'est jamais le seul porteur de sens (NF-08, §6.6 du cahier des charges).
 */
@Composable
fun StatutBadge(
    statut: StatutSignalement,
    modifier: Modifier = Modifier
) {
    val couleur = couleurStatut(statut)
    Row(
        modifier = modifier
            .background(couleur.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = iconeStatut(statut),
            contentDescription = null, // Le libellé qui suit porte déjà l'information.
            tint = couleur,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = statut.libelle,
            style = MaterialTheme.typography.labelMedium,
            color = couleur
        )
    }
}

private fun iconeStatut(statut: StatutSignalement): ImageVector = when (statut) {
    StatutSignalement.EN_ATTENTE_SYNC -> Icons.Filled.CloudOff
    StatutSignalement.ENVOYE -> Icons.Filled.CloudUpload
    StatutSignalement.A_VERIFIER -> Icons.AutoMirrored.Filled.HelpOutline
    StatutSignalement.PRIS_EN_CHARGE -> Icons.Filled.Build
    StatutSignalement.REJETE -> Icons.Filled.Cancel
    StatutSignalement.RESOLUTION_A_CONFIRMER -> Icons.Filled.ReportProblem
    StatutSignalement.RESOLU_CONFIRME -> Icons.Filled.CheckCircle
    StatutSignalement.REOUVERT_NON_RESOLU -> Icons.Filled.Refresh
}

private fun couleurStatut(statut: StatutSignalement): Color = when (statut) {
    StatutSignalement.EN_ATTENTE_SYNC -> OrangeAttente
    StatutSignalement.ENVOYE -> BleuTatitra
    StatutSignalement.A_VERIFIER -> GrisTexte
    StatutSignalement.PRIS_EN_CHARGE -> BleuTatitra
    StatutSignalement.REJETE -> RougeRejet
    StatutSignalement.RESOLUTION_A_CONFIRMER -> OrangeAttente
    StatutSignalement.RESOLU_CONFIRME -> VertResolu
    StatutSignalement.REOUVERT_NON_RESOLU -> RougeRejet
}
