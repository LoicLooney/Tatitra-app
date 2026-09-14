package mg.itu.tatitra_app.ui.home

import android.app.Application
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
import mg.itu.tatitra_app.data.local.PreferencesDataStore
import mg.itu.tatitra_app.data.repository.PreferencesRepository
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement
import mg.itu.tatitra_app.worker.SyncScheduler

/** État affiché par l'écran Accueil. */
data class AccueilUiState(
    val signalementsRecents: List<Signalement> = emptyList(),
    val nombreEnAttente: Int = 0,
    val nombreEnvoyes: Int = 0,
    val nombreAConfirmer: Int = 0,
    val nombreResolus: Int = 0,
    val synchronisationEnCours: Boolean = false,
    val messageSynchronisation: String? = null,
    /** Préférences DataStore (J4), relues au démarrage. */
    val langue: String = PreferencesDataStore.LANGUE_PAR_DEFAUT,
    val derniereSyncMs: Long? = null
) {
    val aDesSignalements: Boolean get() = signalementsRecents.isNotEmpty()
}

/** Détenteur de l'état de l'écran Accueil : l'UI observe, les événements remontent ici (S6). */
class AccueilViewModel(
    private val application: Application,
    private val repository: SignalementRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val etatSynchronisation = MutableStateFlow(EtatSynchronisation())

    val uiState: StateFlow<AccueilUiState> =
        combine(
            repository.observerSignalements(),
            etatSynchronisation,
            preferencesRepository.observerLangue(),
            preferencesRepository.observerDerniereSyncMs()
        ) { signalements, sync, langue, derniereSyncMs ->
            construireEtat(signalements, sync, langue, derniereSyncMs)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DUREE_ABONNEMENT_MS),
            initialValue = AccueilUiState()
        )

    /**
     * Synchronisation demandée manuellement (bouton « Réessayer »).
     * La synchronisation automatique reste assurée par WorkManager.
     */
    fun synchroniserMaintenant() {
        if (etatSynchronisation.value.enCours) return

        viewModelScope.launch {
            etatSynchronisation.update { it.copy(enCours = true, message = null) }

            val resultat = repository.synchroniserEnAttente()
            val rafraichi = repository.rafraichirStatuts()
            // Pourquoi : ne pas afficher une « dernière sync » si rien n'a réellement abouti.
            if (resultat.nombreEnvoyes > 0 || rafraichi) {
                preferencesRepository.enregistrerDerniereSync()
            }

            val message = when {
                resultat.nombreEchecs > 0 -> resultat.message ?: "Synchronisation incomplète."
                resultat.nombreEnvoyes > 0 -> "${resultat.nombreEnvoyes} signalement(s) envoyé(s)."
                else -> "Aucun signalement en attente."
            }
            etatSynchronisation.update { EtatSynchronisation(enCours = false, message = message) }

            if (resultat.doitReessayer) {
                SyncScheduler.demanderSynchronisation(application)
            }
        }
    }

    /** Change la langue et la persiste dans DataStore (relue au prochain démarrage). */
    fun changerLangue(code: String) {
        viewModelScope.launch {
            preferencesRepository.definirLangue(code)
        }
    }

    fun messageAffiche() {
        etatSynchronisation.update { it.copy(message = null) }
    }

    private fun construireEtat(
        signalements: List<Signalement>,
        sync: EtatSynchronisation,
        langue: String,
        derniereSyncMs: Long?
    ): AccueilUiState = AccueilUiState(
        signalementsRecents = signalements.take(NOMBRE_SIGNALEMENTS_RECENTS),
        nombreEnAttente = signalements.count { !it.synchronise },
        nombreEnvoyes = signalements.count {
            it.synchronise && it.statut in STATUTS_EN_TRAITEMENT
        },
        nombreAConfirmer = signalements.count {
            it.statut == StatutSignalement.RESOLUTION_A_CONFIRMER
        },
        nombreResolus = signalements.count { it.statut == StatutSignalement.RESOLU_CONFIRME },
        synchronisationEnCours = sync.enCours,
        messageSynchronisation = sync.message,
        langue = langue,
        derniereSyncMs = derniereSyncMs
    )

    private data class EtatSynchronisation(
        val enCours: Boolean = false,
        val message: String? = null
    )

    companion object {
        private const val NOMBRE_SIGNALEMENTS_RECENTS = 3
        private const val DUREE_ABONNEMENT_MS = 5_000L

        private val STATUTS_EN_TRAITEMENT = setOf(
            StatutSignalement.ENVOYE,
            StatutSignalement.A_VERIFIER,
            StatutSignalement.PRIS_EN_CHARGE,
            StatutSignalement.REOUVERT_NON_RESOLU
        )

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                AccueilViewModel(
                    application = application,
                    repository = application.container.signalementRepository,
                    preferencesRepository = application.container.preferencesRepository
                )
            }
        }
    }
}
