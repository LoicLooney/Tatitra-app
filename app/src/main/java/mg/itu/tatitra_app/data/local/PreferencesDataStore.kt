package mg.itu.tatitra_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tatitraPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tatitra_preferences"
)

class PreferencesDataStore(context: Context) {

    private val dataStore = context.applicationContext.tatitraPreferencesDataStore

    val derniereSyncMs: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[CLE_DERNIERE_SYNC]
    }

    suspend fun enregistrerDerniereSync(horodatageMs: Long = System.currentTimeMillis()) {
        dataStore.edit { prefs ->
            prefs[CLE_DERNIERE_SYNC] = horodatageMs
        }
    }

    companion object {
        private val CLE_DERNIERE_SYNC = longPreferencesKey("derniere_sync_ms")
    }
}
