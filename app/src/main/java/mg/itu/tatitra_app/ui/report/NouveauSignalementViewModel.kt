package mg.itu.tatitra_app.ui.report

import android.app.Application
import android.net.Uri
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
import mg.itu.tatitra_app.data.repository.SignalementRepository
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.ReglesSignalement
import mg.itu.tatitra_app.util.LocationProvider
import mg.itu.tatitra_app.util.PhotoStorage
import mg.itu.tatitra_app.worker.SyncScheduler
import java.io.File

/** État du formulaire de création d'un signalement. */
data class NouveauSignalementUiState(
    val categorie: Categorie = Categorie.ROUTE,
    val description: String = "",
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDemo: Boolean = true,
    val rechercheGpsEnCours: Boolean = false,
    val messageGps: String? = null,
    val enregistrementEnCours: Boolean = false,
    val message: String? = null,
    val idLocalEnregistre: String? = null
) {
    val descriptionEstValide: Boolean get() = ReglesSignalement.descriptionEstValide(description)
    val aUnePosition: Boolean get() = latitude != null && longitude != null

    /** La photo et le GPS restent facultatifs : un signalement ne doit jamais être bloqué. */
    val peutEnregistrer: Boolean get() = descriptionEstValide && !enregistrementEnCours
}

/**
 * Logique de l'écran « Nouveau signalement » : saisie, photo, position, enregistrement local.
 * Aucun appel réseau ici — l'envoi est déclenché ensuite par WorkManager.
 */
class NouveauSignalementViewModel(
    private val application: Application,
    private val repository: SignalementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NouveauSignalementUiState())
    val uiState: StateFlow<NouveauSignalementUiState> = _uiState.asStateFlow()

    /** Fichier créé avant l'ouverture de l'appareil photo, confirmé seulement si la prise réussit. */
    private var fichierPhotoEnAttente: File? = null

    fun changerCategorie(categorie: Categorie) {
        _uiState.update { it.copy(categorie = categorie) }
    }

    fun changerDescription(description: String) {
        if (description.length > ReglesSignalement.LONGUEUR_DESCRIPTION_MAX) return
        _uiState.update { it.copy(description = description) }
    }

    fun changerDonneeDemonstration(isDemo: Boolean) {
        _uiState.update { it.copy(isDemo = isDemo) }
    }

    /** Prépare le fichier de destination et renvoie l'URI à passer à l'appareil photo. */
    fun preparerPriseDePhoto(): Uri {
        val fichier = PhotoStorage.creerFichierPhoto(application)
        fichierPhotoEnAttente = fichier
        return PhotoStorage.obtenirUriPartage(application, fichier)
    }

    fun photoPrise(succes: Boolean) {
        val fichier = fichierPhotoEnAttente
        fichierPhotoEnAttente = null
        if (!succes || fichier == null) {
            fichier?.delete()
            return
        }
        viewModelScope.launch {
            PhotoStorage.optimiser(fichier)
            remplacerPhoto(fichier.absolutePath)
        }
    }

    fun photoChoisieDansGalerie(source: Uri?) {
        if (source == null) return
        viewModelScope.launch {
            val chemin = PhotoStorage.importerDepuisGalerie(application, source)
            if (chemin == null) {
                _uiState.update { it.copy(message = "Image illisible, choisissez une autre photo.") }
            } else {
                remplacerPhoto(chemin)
            }
        }
    }

    fun retirerPhoto() {
        val ancienne = _uiState.value.photoPath
        PhotoStorage.supprimer(ancienne)
        _uiState.update { it.copy(photoPath = null) }
    }

    /** Lit la position du téléphone ; affiche un message clair si elle n'est pas disponible (T07). */
    fun recupererPosition() {
        if (_uiState.value.rechercheGpsEnCours) return

        viewModelScope.launch {
            _uiState.update { it.copy(rechercheGpsEnCours = true, messageGps = null) }

            if (!LocationProvider.permissionAccordee(application)) {
                _uiState.update {
                    it.copy(
                        rechercheGpsEnCours = false,
                        messageGps = "Position non autorisée. Le signalement peut être enregistré sans GPS."
                    )
                }
                return@launch
            }

            val position = LocationProvider.recupererPositionActuelle(application)
            _uiState.update {
                if (position == null) {
                    it.copy(
                        rechercheGpsEnCours = false,
                        messageGps = "Position introuvable. Vérifiez que la localisation est activée, puis réessayez."
                    )
                } else {
                    it.copy(
                        rechercheGpsEnCours = false,
                        latitude = position.latitude,
                        longitude = position.longitude,
                        messageGps = null
                    )
                }
            }
        }
    }

    fun permissionPositionRefusee() {
        _uiState.update {
            it.copy(
                rechercheGpsEnCours = false,
                messageGps = "Sans autorisation de localisation, le signalement partira sans coordonnées."
            )
        }
    }

    /**
     * Enregistre le signalement dans Room puis demande une synchronisation.
     * Le succès de l'enregistrement ne dépend pas du réseau (F-CIT-06).
     */
    fun enregistrer() {
        val etat = _uiState.value
        if (!etat.peutEnregistrer) {
            _uiState.update {
                it.copy(
                    message = "Décrivez le problème en au moins " +
                        "${ReglesSignalement.LONGUEUR_DESCRIPTION_MIN} caractères."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enregistrementEnCours = true, message = null) }

            val idLocal = repository.enregistrerLocal(
                categorie = etat.categorie,
                description = etat.description,
                latitude = etat.latitude,
                longitude = etat.longitude,
                photoPath = etat.photoPath,
                isDemo = etat.isDemo
            )
            SyncScheduler.demanderSynchronisation(application)

            _uiState.update {
                it.copy(
                    enregistrementEnCours = false,
                    idLocalEnregistre = idLocal,
                    message = "Signalement enregistré sur le téléphone."
                )
            }
        }
    }

    /** La navigation de retour a été effectuée : on évite de la rejouer après une rotation. */
    fun enregistrementTraite() {
        _uiState.update { it.copy(idLocalEnregistre = null) }
    }

    fun messageAffiche() {
        _uiState.update { it.copy(message = null) }
    }

    private fun remplacerPhoto(nouveauChemin: String) {
        val ancienne = _uiState.value.photoPath
        if (ancienne != null && ancienne != nouveauChemin) PhotoStorage.supprimer(ancienne)
        _uiState.update { it.copy(photoPath = nouveauChemin) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as TatitraApplication
                NouveauSignalementViewModel(application, application.container.signalementRepository)
            }
        }
    }
}
