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
import mg.itu.tatitra_app.data.repository.ResultatActionResolution
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement

data class DetailSignalementUiState(
    val signalement: Signalement? = null,
    val charge: Boolean = true,
    val actionEnCours: Boolean = false,
    val message: String? = null,
    /** True si on attend encore le détail serveur pour savoir qui a proposé. */
    val metaResolutionIncomplete: Boolean = false
)

/**
 * Détail + actions de résolution (J5).
 * Les champs résolution viennent de Room (source de vérité) après sync / actions API.
 */
class DetailSignalementViewModel(
    private val idLocal: String,
    private val repository: SignalementRepository
) : ViewModel() {

    private val action = MutableStateFlow(EtatAction())

    val uiState: StateFlow<DetailSignalementUiState> =
        combine(
            repository.observerSignalement(idLocal),
            action
        ) { signalement, act ->
            val incomplete = signalement?.statut == StatutSignalement.RESOLUTION_A_CONFIRMER &&
                signalement.resolutionProposeePar.isNullOrBlank()
            DetailSignalementUiState(
                signalement = signalement,
                charge = false,
                actionEnCours = act.enCours,
                message = act.message,
                metaResolutionIncomplete = incomplete
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DUREE_ABONNEMENT_MS),
            initialValue = DetailSignalementUiState()
        )

    init {
        viewModelScope.launch {
            repository.rafraichirStatuts()
            repository.recupererDetailServeur(idLocal)
        }
    }

    fun rechargerDetailServeur() {
        viewModelScope.launch {
            action.update { it.copy(enCours = true, message = null) }
            val distant = repository.recupererDetailServeur(idLocal)
            action.update {
                it.copy(
                    enCours = false,
                    message = if (distant == null) {
                        "Impossible de charger la proposition de résolution."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun proposerResolution() = lancerAction {
        repository.proposerResolution(idLocal)
    }

    fun confirmerResolution() = lancerAction {
        repository.confirmerResolution(idLocal)
    }

    fun declarerToujoursEndommage() = lancerAction {
        repository.declarerToujoursEndommage(idLocal)
    }

    fun messageAffiche() {
        action.update { it.copy(message = null) }
    }

    private fun lancerAction(bloc: suspend () -> ResultatActionResolution) {
        if (action.value.enCours) return
        viewModelScope.launch {
            action.update { it.copy(enCours = true, message = null) }
            when (val resultat = bloc()) {
                is ResultatActionResolution.Succes -> {
                    action.update {
                        it.copy(enCours = false, message = "Action enregistrée.")
                    }
                }
                is ResultatActionResolution.Echec -> {
                    action.update { it.copy(enCours = false, message = resultat.message) }
                }
            }
        }
    }

    private data class EtatAction(
        val enCours: Boolean = false,
        val message: String? = null
    )

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

fun DetailSignalementUiState.peutProposerResolution(): Boolean {
    val s = signalement ?: return false
    if (!s.synchronise || s.serverId == null) return false
    return s.statut == StatutSignalement.PRIS_EN_CHARGE ||
        s.statut == StatutSignalement.REOUVERT_NON_RESOLU
}

fun DetailSignalementUiState.peutConfirmerOuRouvrir(): Boolean {
    val s = signalement ?: return false
    if (!s.synchronise || s.serverId == null) return false
    return s.statut == StatutSignalement.RESOLUTION_A_CONFIRMER &&
        s.resolutionProposeePar == "ADMIN"
}

fun DetailSignalementUiState.attenteConfirmationAdmin(): Boolean {
    val s = signalement ?: return false
    return s.statut == StatutSignalement.RESOLUTION_A_CONFIRMER &&
        s.resolutionProposeePar == "CITOYEN"
}
