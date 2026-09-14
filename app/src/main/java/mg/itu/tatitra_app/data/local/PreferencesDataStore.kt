package mg.itu.tatitra_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tatitraPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tatitra_preferences"
)

/**
 * Accès DataStore aux préférences de l'application (J4).
 * Clés : langue (fr / mg), horodatage de la dernière synchronisation réussie.
 */
class PreferencesDataStore(context: Context) {

    private val dataStore = context.applicationContext.tatitraPreferencesDataStore

    val langue: Flow<String> = dataStore.data.map { prefs ->
        prefs[CLE_LANGUE] ?: LANGUE_PAR_DEFAUT
    }

    val derniereSyncMs: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[CLE_DERNIERE_SYNC]
    }

    suspend fun enregistrerLangue(code: String) {
        dataStore.edit { prefs ->
            prefs[CLE_LANGUE] = code
        }
    }

    suspend fun enregistrerDerniereSync(horodatageMs: Long = System.currentTimeMillis()) {
        dataStore.edit { prefs ->
            prefs[CLE_DERNIERE_SYNC] = horodatageMs
        }
    }

    companion object {
        const val LANGUE_PAR_DEFAUT = "fr"
        const val LANGUE_FR = "fr"
        const val LANGUE_MG = "mg"

        private val CLE_LANGUE = stringPreferencesKey("langue")
        private val CLE_DERNIERE_SYNC = longPreferencesKey("derniere_sync_ms")
    }
}
