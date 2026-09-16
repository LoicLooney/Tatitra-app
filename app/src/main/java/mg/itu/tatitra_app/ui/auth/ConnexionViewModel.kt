package mg.itu.tatitra_app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.tatitra_app.TatitraApplication
import mg.itu.tatitra_app.data.repository.AuthRepository
import mg.itu.tatitra_app.data.repository.ResultatAuth
import mg.itu.tatitra_app.domain.ReglesConnexion

data class ConnexionUiState(
    val identifiant: String = "",
    val motDePasse: String = "",
    val motDePasseVisible: Boolean = false,
    val enCours: Boolean = false,
    /** Message d'erreur global (refus du serveur, réseau). */
    val erreur: String? = null,
    /** Message neutre : lien de confirmation ou de réinitialisation envoyé. */
    val information: String? = null,
    /** Passe à true quand la session est ouverte : l'écran demande alors la navigation. */
    val connecte: Boolean = false,
    val authentificationDisponible: Boolean = true
) {
    val erreurIdentifiant: String? get() = ReglesConnexion.erreurIdentifiant(identifiant)
    val erreurMotDePasse: String? get() = ReglesConnexion.erreurMotDePasse(motDePasse)

    val peutEnvoyer: Boolean
        get() = !enCours && ReglesConnexion.formulaireEstComplet(identifiant, motDePasse)
}

class ConnexionViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConnexionUiState(authentificationDisponible = repository.authentificationDisponible)
    )
    val uiState: StateFlow<ConnexionUiState> = _uiState.asStateFlow()

    fun saisirIdentifiant(valeur: String) {
        _uiState.update { it.copy(identifiant = valeur, erreur = null, information = null) }
    }

    fun saisirMotDePasse(valeur: String) {
        _uiState.update { it.copy(motDePasse = valeur, erreur = null, information = null) }
    }

    fun basculerVisibiliteMotDePasse() {
        _uiState.update { it.copy(motDePasseVisible = !it.motDePasseVisible) }
    }

    fun seConnecter() = lancer { etat ->
        repository.connecter(etat.identifiant, etat.motDePasse)
    }

    fun creerCompte() = lancer { etat ->
        repository.creerCompte(etat.identifiant, etat.motDePasse)
    }

    /**
     * Le lien de réinitialisation n'exige pas de mot de passe : on ne vérifie donc que
     * l'adresse, sans quoi le lien serait inaccessible à qui a justement tout oublié.
     */
    fun motDePasseOublie() {
        val etat = _uiState.value
        if (etat.enCours) return
        if (!ReglesConnexion.emailEstValide(etat.identifiant)) {
            _uiState.update {
                it.copy(erreur = "Saisissez d'abord votre adresse e-mail.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(enCours = true, erreur = null, information = null) }
            appliquer(repository.demanderReinitialisation(etat.identifiant))
        }
    }

    fun continuerEnDemonstration(surTermine: () -> Unit) {
        viewModelScope.launch {
            repository.continuerEnDemonstration()
            surTermine()
        }
    }

    private fun lancer(action: suspend (ConnexionUiState) -> ResultatAuth) {
        val etat = _uiState.value
        if (!etat.peutEnvoyer) return
        viewModelScope.launch {
            _uiState.update { it.copy(enCours = true, erreur = null, information = null) }
            appliquer(action(etat))
        }
    }

    private fun appliquer(resultat: ResultatAuth) {
        _uiState.update { etat ->
            when (resultat) {
                is ResultatAuth.Succes ->
                    etat.copy(enCours = false, connecte = true)

                is ResultatAuth.ConfirmationRequise ->
                    etat.copy(
                        enCours = false,
                        information = "Un e-mail vient d'être envoyé à ${resultat.email}. " +
                            "Ouvrez le lien qu'il contient, puis revenez vous connecter."
                    )

                is ResultatAuth.Echec ->
                    etat.copy(enCours = false, erreur = resultat.message)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                ConnexionViewModel(application.container.authRepository)
            }
        }
    }
}
