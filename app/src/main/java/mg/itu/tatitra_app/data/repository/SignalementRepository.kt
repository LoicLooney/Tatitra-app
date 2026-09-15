package mg.itu.tatitra_app.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.tatitra_app.data.local.SignalementDao
import mg.itu.tatitra_app.data.local.SignalementEntity
import mg.itu.tatitra_app.data.local.versDomaine
import mg.itu.tatitra_app.data.remote.ResolutionReopenRequest
import mg.itu.tatitra_app.data.remote.ResolutionRoleRequest
import mg.itu.tatitra_app.data.remote.SignalementRequest
import mg.itu.tatitra_app.data.remote.SignalementResponse
import mg.itu.tatitra_app.data.remote.TatitraApi
import mg.itu.tatitra_app.domain.Categorie
import mg.itu.tatitra_app.domain.Signalement
import mg.itu.tatitra_app.domain.StatutSignalement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Point d'accès unique aux signalements : l'UI ne parle jamais directement à Room ni à Retrofit.
 * Room est la source de vérité ; le réseau ne fait qu'alimenter et confirmer cette base.
 */
class SignalementRepository(
    private val dao: SignalementDao,
    private val api: TatitraApi
) {

    fun observerSignalements(): Flow<List<Signalement>> =
        dao.observerTout().map { lignes -> lignes.map { it.versDomaine() } }

    fun observerSignalement(idLocal: String): Flow<Signalement?> =
        dao.observerParId(idLocal).map { it?.versDomaine() }

    fun observerNombreEnAttente(): Flow<Int> = dao.observerNombreEnAttente()

    /**
     * Enregistre immédiatement le signalement en local (F-CIT-06) et renvoie son identifiant.
     * Aucun accès réseau ici : la création ne dépend jamais de la connexion.
     */
    suspend fun enregistrerLocal(
        categorie: Categorie,
        description: String,
        latitude: Double?,
        longitude: Double?,
        photoPath: String?,
        isDemo: Boolean
    ): String {
        val idLocal = UUID.randomUUID().toString()
        dao.inserer(
            SignalementEntity(
                idLocal = idLocal,
                categorie = categorie.name,
                description = description.trim(),
                latitude = latitude,
                longitude = longitude,
                photoPath = photoPath,
                photoUrl = null,
                statut = StatutSignalement.EN_ATTENTE_SYNC.name,
                synchronise = false,
                serverId = null,
                dateCreation = System.currentTimeMillis(),
                isDemo = isDemo,
                derniereErreurSync = null
            )
        )
        return idLocal
    }

    /**
     * Envoie au serveur tous les signalements encore marqués non synchronisés.
     * Appelée par le WorkManager et par le bouton « Réessayer » de l'écran Accueil.
     */
    suspend fun synchroniserEnAttente(): ResultatSync {
        val enAttente = dao.recupererEnAttenteDeSync()
        if (enAttente.isEmpty()) return ResultatSync()

        var envoyes = 0
        var echecs = 0
        var doitReessayer = false
        var dernierMessage: String? = null

        for (local in enAttente) {
            when (val resultat = envoyerUnSignalement(local)) {
                is EnvoiSignalement.Succes -> envoyes++
                is EnvoiSignalement.EchecTemporaire -> {
                    echecs++
                    doitReessayer = true
                    dernierMessage = resultat.message
                    marquerErreur(local, resultat.message)
                }
                is EnvoiSignalement.EchecDefinitif -> {
                    echecs++
                    dernierMessage = resultat.message
                    marquerErreur(local, resultat.message)
                }
            }
        }

        return ResultatSync(
            nombreEnvoyes = envoyes,
            nombreEchecs = echecs,
            doitReessayer = doitReessayer,
            message = dernierMessage
        )
    }

    /**
     * Relit le statut serveur des signalements déjà synchronisés (prise en charge,
     * proposition de résolution…). Silencieuse en cas de coupure réseau.
     */
    suspend fun rafraichirStatuts(): Boolean = try {
        val distants = api.listerSignalements()
        for (distant in distants) {
            val idLocal = distant.clientId ?: continue
            val local = dao.recupererParId(idLocal) ?: continue
            val statutDistant = StatutSignalement.depuisCode(distant.statut).name
            if (local.statut != statutDistant ||
                local.serverId != distant.id ||
                local.resolutionProposeePar != distant.resolutionProposeePar ||
                local.dateLimiteConfirmation != distant.dateLimiteConfirmation ||
                local.motifReouverture != distant.motifReouverture
            ) {
                dao.mettreAJour(
                    local.copy(
                        statut = statutDistant,
                        serverId = distant.id,
                        photoUrl = distant.photoUrl ?: local.photoUrl,
                        resolutionProposeePar = distant.resolutionProposeePar,
                        dateLimiteConfirmation = distant.dateLimiteConfirmation,
                        motifReouverture = distant.motifReouverture
                    )
                )
            }
        }
        true
    } catch (erreur: IOException) {
        Log.w(TAG, "Rafraîchissement des statuts impossible (réseau) : ${erreur.message}")
        false
    } catch (erreur: HttpException) {
        Log.w(TAG, "Rafraîchissement des statuts refusé par le serveur : ${erreur.code()}")
        false
    }

    /**
     * Relit le détail serveur d'un signalement synchronisé (champs résolution J5).
     */
    suspend fun recupererDetailServeur(idLocal: String): SignalementResponse? {
        val local = dao.recupererParId(idLocal) ?: return null
        val serverId = local.serverId ?: return null
        return try {
            val distant = api.recupererSignalement(serverId)
            appliquerDistant(local, distant)
            distant
        } catch (erreur: IOException) {
            Log.w(TAG, "Détail serveur indisponible : ${erreur.message}")
            null
        } catch (erreur: HttpException) {
            Log.w(TAG, "Détail serveur refusé : ${erreur.code()}")
            null
        }
    }

    /** Citoyen propose une résolution (J5). */
    suspend fun proposerResolution(idLocal: String): ResultatActionResolution =
        executerResolution(idLocal) { serverId ->
            api.proposerResolution(serverId, ResolutionRoleRequest(ROLE_CITOYEN), ROLE_CITOYEN)
        }

    /** Citoyen confirme une résolution proposée par l'admin. */
    suspend fun confirmerResolution(idLocal: String): ResultatActionResolution =
        executerResolution(idLocal) { serverId ->
            api.confirmerResolution(serverId, ResolutionRoleRequest(ROLE_CITOYEN), ROLE_CITOYEN)
        }

    /** Citoyen déclare « toujours endommagé ». */
    suspend fun declarerToujoursEndommage(idLocal: String): ResultatActionResolution =
        executerResolution(idLocal) { serverId ->
            api.rouvrirResolution(
                serverId,
                ResolutionReopenRequest(motif = "Toujours endommagé")
            )
        }

    private suspend fun executerResolution(
        idLocal: String,
        appel: suspend (serverId: String) -> SignalementResponse
    ): ResultatActionResolution {
        val local = dao.recupererParId(idLocal)
            ?: return ResultatActionResolution.Echec("Signalement introuvable.")
        val serverId = local.serverId
            ?: return ResultatActionResolution.Echec(
                "Synchronisez d'abord ce signalement avant une action de résolution."
            )
        return try {
            val distant = appel(serverId)
            appliquerDistant(local, distant)
            ResultatActionResolution.Succes(distant)
        } catch (erreur: IOException) {
            ResultatActionResolution.Echec("Serveur injoignable : ${erreur.message ?: "réseau"}")
        } catch (erreur: HttpException) {
            ResultatActionResolution.Echec(lireMessageHttp(erreur))
        }
    }

    private suspend fun appliquerDistant(local: SignalementEntity, distant: SignalementResponse) {
        val actuel = dao.recupererParId(local.idLocal) ?: local
        dao.mettreAJour(
            actuel.copy(
                synchronise = true,
                serverId = distant.id,
                photoUrl = distant.photoUrl ?: actuel.photoUrl,
                statut = StatutSignalement.depuisCode(distant.statut).name,
                derniereErreurSync = null,
                resolutionProposeePar = distant.resolutionProposeePar,
                dateLimiteConfirmation = distant.dateLimiteConfirmation,
                motifReouverture = distant.motifReouverture
            )
        )
    }

    private fun lireMessageHttp(erreur: HttpException): String {
        val corps = try {
            erreur.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        return if (!corps.isNullOrBlank()) {
            "Refus du serveur : $corps"
        } else {
            "Refus du serveur (HTTP ${erreur.code()})"
        }
    }

    /** Envoie la photo puis les métadonnées d'un signalement. */
    private suspend fun envoyerUnSignalement(local: SignalementEntity): EnvoiSignalement = try {
        val photoUrl = local.photoUrl ?: envoyerPhotoSiPresente(local.photoPath)?.also { url ->
            // Mémorisée tout de suite : si l'envoi des métadonnées échoue, la reprise
            // ne téléversera pas une seconde fois la même image.
            dao.mettreAJour(local.copy(photoUrl = url))
        }
        val distant = api.creerSignalement(
            SignalementRequest(
                clientId = local.idLocal,
                categorie = local.categorie,
                description = local.description,
                latitude = local.latitude,
                longitude = local.longitude,
                photoUrl = photoUrl,
                isDemo = local.isDemo
            )
        )
        // Relire la ligne : l'URL photo a pu être écrite juste avant.
        val actuel = dao.recupererParId(local.idLocal) ?: local
        dao.mettreAJour(
            actuel.copy(
                synchronise = true,
                serverId = distant.id,
                photoUrl = photoUrl ?: distant.photoUrl ?: actuel.photoUrl,
                statut = StatutSignalement.depuisCode(distant.statut).name,
                derniereErreurSync = null,
                resolutionProposeePar = distant.resolutionProposeePar,
                dateLimiteConfirmation = distant.dateLimiteConfirmation,
                motifReouverture = distant.motifReouverture
            )
        )
        EnvoiSignalement.Succes
    } catch (erreur: IOException) {
        // Réseau absent ou serveur injoignable : la donnée reste intacte en local (T09).
        EnvoiSignalement.EchecTemporaire("Serveur injoignable : ${erreur.message ?: "réseau indisponible"}")
    } catch (erreur: HttpException) {
        val message = "Refus du serveur (HTTP ${erreur.code()})"
        if (estErreurTemporaire(erreur.code())) {
            EnvoiSignalement.EchecTemporaire(message)
        } else {
            EnvoiSignalement.EchecDefinitif(message)
        }
    }

    /** Téléverse le fichier photo local et renvoie son URL, ou null si le signalement n'a pas de photo. */
    private suspend fun envoyerPhotoSiPresente(photoPath: String?): String? {
        if (photoPath == null) return null
        val fichier = File(photoPath)
        if (!fichier.exists()) {
            Log.w(TAG, "Photo introuvable, signalement envoyé sans image : $photoPath")
            return null
        }
        val corps = fichier.asRequestBody(TYPE_IMAGE.toMediaTypeOrNull())
        val partie = MultipartBody.Part.createFormData("photo", fichier.name, corps)
        return api.envoyerPhoto(partie).url
    }

    /** Relit la ligne avant de l'annoter : l'envoi a pu y écrire l'URL de la photo entre-temps. */
    private suspend fun marquerErreur(local: SignalementEntity, message: String) {
        val ligneActuelle = dao.recupererParId(local.idLocal) ?: local
        dao.mettreAJour(ligneActuelle.copy(derniereErreurSync = message))
    }

    /** 408 (timeout) et 429 (trop de requêtes) méritent une nouvelle tentative, pas un 400. */
    private fun estErreurTemporaire(code: Int): Boolean =
        code >= 500 || code == 408 || code == 429

    private sealed interface EnvoiSignalement {
        data object Succes : EnvoiSignalement
        data class EchecTemporaire(val message: String) : EnvoiSignalement
        data class EchecDefinitif(val message: String) : EnvoiSignalement
    }

    private companion object {
        const val TAG = "SignalementRepository"
        const val TYPE_IMAGE = "image/jpeg"
        const val ROLE_CITOYEN = "CITOYEN"
    }
}

/** Résultat d'une action de résolution (proposer / confirmer / rouvrir). */
sealed interface ResultatActionResolution {
    data class Succes(val distant: SignalementResponse) : ResultatActionResolution
    data class Echec(val message: String) : ResultatActionResolution
}
