package mg.itu.tatitra_app.data.repository

import kotlinx.coroutines.flow.Flow
import mg.itu.tatitra_app.data.local.PreferencesDataStore

class PreferencesRepository(
    private val preferences: PreferencesDataStore
) {
    fun observerDerniereSyncMs(): Flow<Long?> = preferences.derniereSyncMs

    suspend fun enregistrerDerniereSync(horodatageMs: Long = System.currentTimeMillis()) {
        preferences.enregistrerDerniereSync(horodatageMs)
    }
}
