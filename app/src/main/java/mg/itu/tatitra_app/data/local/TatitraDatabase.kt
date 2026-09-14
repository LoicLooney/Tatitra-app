package mg.itu.tatitra_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Base locale de l'application. Une seule instance pour tout le processus. */
@Database(
    entities = [SignalementEntity::class],
    version = 2,
    exportSchema = true
)
abstract class TatitraDatabase : RoomDatabase() {

    abstract fun signalementDao(): SignalementDao

    companion object {
        private const val NOM_BASE = "tatitra.db"

        /** Ajoute les colonnes de résolution (J5) sans perdre les signalements locaux. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE signalements ADD COLUMN resolutionProposeePar TEXT DEFAULT NULL"
                )
                db.execSQL(
                    "ALTER TABLE signalements ADD COLUMN dateLimiteConfirmation TEXT DEFAULT NULL"
                )
                db.execSQL(
                    "ALTER TABLE signalements ADD COLUMN motifReouverture TEXT DEFAULT NULL"
                )
            }
        }

        @Volatile
        private var instance: TatitraDatabase? = null

        fun obtenirInstance(context: Context): TatitraDatabase =
            instance ?: synchronized(this) {
                instance ?: construire(context).also { instance = it }
            }

        private fun construire(context: Context): TatitraDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TatitraDatabase::class.java,
                NOM_BASE
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
