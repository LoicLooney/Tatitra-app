package mg.itu.tatitra_app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.tatitra_app.TatitraApplication
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement

data class MesSignalementsUiState(
    val signalements: List<Signalement> = emptyList(),
    val signalementsFiltres: List<Signalement> = emptyList(),
    val filtreStatut: StatutSignalement? = null,
    val filtreCategorie: Categorie? = null
)

class MesSignalementsViewModel(
    private val repository: SignalementRepository
) : ViewModel() {

    private val filtres = MutableStateFlow(Filtres())

    val uiState: StateFlow<MesSignalementsUiState> =
        combine(
            repository.observerSignalements(),
            filtres
        ) { signalements, f ->
            val filtresListe = signalements.filter { s ->
                (f.statut == null || s.statut == f.statut) &&
                    (f.categorie == null || s.categorie == f.categorie)
            }
            MesSignalementsUiState(
                signalements = signalements,
                signalementsFiltres = filtresListe,
                filtreStatut = f.statut,
                filtreCategorie = f.categorie
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DUREE_ABONNEMENT_MS),
            initialValue = MesSignalementsUiState()
        )

    init {
        viewModelScope.launch {
            repository.rafraichirStatuts()
        }
    }

    fun filtrerStatut(statut: StatutSignalement?) {
        filtres.update { it.copy(statut = statut) }
    }

    fun filtrerCategorie(categorie: Categorie?) {
        filtres.update { it.copy(categorie = categorie) }
    }

    private data class Filtres(
        val statut: StatutSignalement? = null,
        val categorie: Categorie? = null
    )

    companion object {
        private const val DUREE_ABONNEMENT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                MesSignalementsViewModel(application.container.signalementRepository)
            }
        }
    }
}
