package mg.itu.tatitra_app.data.repository

import kotlinx.coroutines.flow.Flow
import mg.itu.tatitra_app.data.local.PreferencesDataStore

/**
 * Préférences persistantes (J4) : l'UI ne parle pas directement au DataStore.
 */
class PreferencesRepository(
    private val preferences: PreferencesDataStore
) {
    fun observerLangue(): Flow<String> = preferences.langue

    fun observerDerniereSyncMs(): Flow<Long?> = preferences.derniereSyncMs

    suspend fun definirLangue(code: String) {
        preferences.enregistrerLangue(code)
    }

    suspend fun enregistrerDerniereSync(horodatageMs: Long = System.currentTimeMillis()) {
        preferences.enregistrerDerniereSync(horodatageMs)
    }
}
