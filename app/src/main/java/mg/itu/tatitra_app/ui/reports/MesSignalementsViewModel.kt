package mg.itu.tatitra_app.ui.reports

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mg.itu.tatitra_app.domain.Signalement

data class MesSignalementsUiState(
    val signalements: List<Signalement> = emptyList(),
    val estFictif: Boolean = true
)

/**
 * ViewModel de « Mes signalements » (J2).
 * Pour l'instant : StateFlow alimenté par des données fictives.
 * Plus tard : Repository / Room / API.
 */
class MesSignalementsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        MesSignalementsUiState(signalements = SignalementsFictifs.liste)
    )
    val uiState: StateFlow<MesSignalementsUiState> = _uiState.asStateFlow()
}
