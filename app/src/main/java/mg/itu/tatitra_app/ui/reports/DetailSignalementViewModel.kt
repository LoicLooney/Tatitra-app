package mg.itu.tatitra_app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.tatitra_app.TatitraApplication
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.domain.Signalement

data class DetailSignalementUiState(
    val signalement: Signalement? = null,
    val charge: Boolean = true
)

/**
 * Détail d'un signalement (J3) : photo, statut, GPS depuis Room (données réelles).
 */
class DetailSignalementViewModel(
    idLocal: String,
    private val repository: SignalementRepository
) : ViewModel() {

    val uiState: StateFlow<DetailSignalementUiState> =
        repository.observerSignalement(idLocal)
            .map { DetailSignalementUiState(signalement = it, charge = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(DUREE_ABONNEMENT_MS),
                initialValue = DetailSignalementUiState()
            )

    init {
        viewModelScope.launch {
            repository.rafraichirStatuts()
        }
    }

    companion object {
        private const val DUREE_ABONNEMENT_MS = 5_000L

        fun factory(idLocal: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                DetailSignalementViewModel(
                    idLocal = idLocal,
                    repository = application.container.signalementRepository
                )
            }
        }
    }
}
