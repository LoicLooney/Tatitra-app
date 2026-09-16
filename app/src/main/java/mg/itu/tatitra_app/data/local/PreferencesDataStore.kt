package mg.itu.tatitra_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.itu.tatitra_app.domain.SessionUtilisateur

private val Context.tatitraPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tatitra_preferences"
)

class PreferencesDataStore(context: Context) : StockageSession {

    private val dataStore = context.applicationContext.tatitraPreferencesDataStore

    val derniereSyncMs: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[CLE_DERNIERE_SYNC]
    }

    suspend fun enregistrerDerniereSync(horodatageMs: Long = System.currentTimeMillis()) {
        dataStore.edit { prefs ->
            prefs[CLE_DERNIERE_SYNC] = horodatageMs
        }
    }

    // --- Session utilisateur -------------------------------------------------
    //
    // Les jetons sont stockés en clair dans le DataStore de l'application, isolé des
    // autres applications par le bac à sable Android. Cela convient à un projet de
    // démonstration ; une mise en production justifierait EncryptedSharedPreferences
    // ou le Keystore, pour résister à un appareil rooté ou à une sauvegarde extraite.

    override val session: Flow<SessionUtilisateur?> = dataStore.data.map { prefs ->
        val id = prefs[CLE_SESSION_ID]
        val email = prefs[CLE_SESSION_EMAIL]
        val jeton = prefs[CLE_SESSION_JETON]
        if (id == null || email == null || jeton == null) {
            null
        } else {
            SessionUtilisateur(
                idUtilisateur = id,
                email = email,
                jetonAcces = jeton,
                jetonRafraichissement = prefs[CLE_SESSION_RAFRAICHISSEMENT].orEmpty(),
                expireLeMs = prefs[CLE_SESSION_EXPIRE_LE] ?: 0L
            )
        }
    }

    /** Vrai quand l'utilisateur a choisi « Continuer en mode démonstration ». */
    override val modeDemonstration: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CLE_MODE_DEMONSTRATION] ?: false
    }

    override suspend fun enregistrerSession(session: SessionUtilisateur) {
        dataStore.edit { prefs ->
            prefs[CLE_SESSION_ID] = session.idUtilisateur
            prefs[CLE_SESSION_EMAIL] = session.email
            prefs[CLE_SESSION_JETON] = session.jetonAcces
            prefs[CLE_SESSION_RAFRAICHISSEMENT] = session.jetonRafraichissement
            prefs[CLE_SESSION_EXPIRE_LE] = session.expireLeMs
            // Se connecter met fin au mode démonstration.
            prefs[CLE_MODE_DEMONSTRATION] = false
        }
    }

    override suspend fun effacerSession() {
        dataStore.edit { prefs ->
            prefs.remove(CLE_SESSION_ID)
            prefs.remove(CLE_SESSION_EMAIL)
            prefs.remove(CLE_SESSION_JETON)
            prefs.remove(CLE_SESSION_RAFRAICHISSEMENT)
            prefs.remove(CLE_SESSION_EXPIRE_LE)
            prefs[CLE_MODE_DEMONSTRATION] = false
        }
    }

    override suspend fun activerModeDemonstration() {
        dataStore.edit { prefs ->
            prefs[CLE_MODE_DEMONSTRATION] = true
        }
    }

    companion object {
        private val CLE_DERNIERE_SYNC = longPreferencesKey("derniere_sync_ms")

        private val CLE_SESSION_ID = stringPreferencesKey("session_id_utilisateur")
        private val CLE_SESSION_EMAIL = stringPreferencesKey("session_email")
        private val CLE_SESSION_JETON = stringPreferencesKey("session_jeton_acces")
        private val CLE_SESSION_RAFRAICHISSEMENT =
            stringPreferencesKey("session_jeton_rafraichissement")
        private val CLE_SESSION_EXPIRE_LE = longPreferencesKey("session_expire_le_ms")
        private val CLE_MODE_DEMONSTRATION = booleanPreferencesKey("mode_demonstration")
    }
}
