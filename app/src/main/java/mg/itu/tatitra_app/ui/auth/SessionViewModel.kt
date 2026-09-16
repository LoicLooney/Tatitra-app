package mg.itu.tatitra_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.tatitra_app.TatitraApplication
import mg.itu.tatitra_app.data.repository.AuthRepository
import mg.itu.tatitra_app.domain.SessionUtilisateur

data class SessionUiState(
    /** Vrai tant que le DataStore n'a pas répondu : on n'affiche rien avant de savoir. */
    val chargement: Boolean = true,
    val session: SessionUtilisateur? = null,
    val modeDemonstration: Boolean = false
) {
    /** L'application est accessible : soit connecté, soit en mode démonstration assumé. */
    val accesOuvert: Boolean get() = session != null || modeDemonstration
}

/**
 * Décide, au lancement, si l'on entre dans l'application ou si l'on affiche la connexion.
 *
 * Porté par le DataStore : une connexion réussie ou le choix du mode démonstration font
 * basculer l'écran sans que personne n'ait à déclencher de navigation.
 */
class SessionViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<SessionUiState> =
        combine(
            repository.observerSession(),
            repository.observerModeDemonstration()
        ) { session, demonstration ->
            SessionUiState(
                chargement = false,
                session = session,
                modeDemonstration = demonstration
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DUREE_ABONNEMENT_MS),
            initialValue = SessionUiState()
        )

    fun seDeconnecter() {
        viewModelScope.launch {
            repository.deconnecter()
        }
    }

    companion object {
        private const val DUREE_ABONNEMENT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                SessionViewModel(application.container.authRepository)
            }
        }
    }
}
