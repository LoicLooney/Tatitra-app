package mg.itu.tatitra_app.data.local

import kotlinx.coroutines.flow.Flow
import mg.itu.tatitra_app.domain.SessionUtilisateur

/**
 * Ce que le Repository d'authentification attend du stockage local.
 *
 * L'interface existe pour la même raison que SignalementDao : elle permet de tester le
 * Repository sur la JVM avec un double en mémoire, sans Context Android ni DataStore.
 */
interface StockageSession {
    val session: Flow<SessionUtilisateur?>
    val modeDemonstration: Flow<Boolean>

    suspend fun enregistrerSession(session: SessionUtilisateur)
    suspend fun effacerSession()
    suspend fun activerModeDemonstration()
}
