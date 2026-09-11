package mg.itu.tatitra_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Accès à la table locale des signalements.
 * Les lectures continues passent par Flow (l'UI se recompose seule, NF-13),
 * les écritures et lectures ponctuelles par des fonctions suspend (NF-12).
 */
@Dao
interface SignalementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserer(item: SignalementEntity)

    @Update
    suspend fun mettreAJour(item: SignalementEntity)

    @Query("SELECT * FROM signalements ORDER BY dateCreation DESC")
    fun observerTout(): Flow<List<SignalementEntity>>

    @Query("SELECT * FROM signalements WHERE idLocal = :idLocal")
    fun observerParId(idLocal: String): Flow<SignalementEntity?>

    @Query("SELECT * FROM signalements WHERE idLocal = :idLocal")
    suspend fun recupererParId(idLocal: String): SignalementEntity?

    /** Signalements créés hors ligne ou dont l'envoi a échoué : matière première du worker de sync. */
    @Query("SELECT * FROM signalements WHERE synchronise = 0 ORDER BY dateCreation ASC")
    suspend fun recupererEnAttenteDeSync(): List<SignalementEntity>

    @Query("SELECT COUNT(*) FROM signalements WHERE synchronise = 0")
    fun observerNombreEnAttente(): Flow<Int>

    @Query("DELETE FROM signalements WHERE idLocal = :idLocal")
    suspend fun supprimer(idLocal: String)
}
