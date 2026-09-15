package mg.itu.tatitra_app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ligne de la base locale Room : source de vérité du téléphone (§7.2 du cahier des charges).
 *
 * `idLocal` est généré sur le téléphone et sert aussi de clé d'idempotence côté serveur
 * (`clientId`) : rejouer une synchronisation ne crée pas de doublon.
 */
@Entity(tableName = "signalements")
data class SignalementEntity(
    @PrimaryKey val idLocal: String,
    val categorie: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    @ColumnInfo(defaultValue = "NULL") val photoUrl: String?,
    val statut: String,
    val synchronise: Boolean = false,
    val serverId: String? = null,
    val dateCreation: Long,
    @ColumnInfo(defaultValue = "1") val isDemo: Boolean = true,
    @ColumnInfo(defaultValue = "NULL") val derniereErreurSync: String? = null,
    /** ADMIN ou CITOYEN — nécessaire pour afficher confirmer / attendre hors ligne. */
    @ColumnInfo(defaultValue = "NULL") val resolutionProposeePar: String? = null,
    @ColumnInfo(defaultValue = "NULL") val dateLimiteConfirmation: String? = null,
    @ColumnInfo(defaultValue = "NULL") val motifReouverture: String? = null
)
