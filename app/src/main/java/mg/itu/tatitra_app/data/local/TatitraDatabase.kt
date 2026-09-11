package mg.itu.tatitra_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base locale de l'application. Une seule instance pour tout le processus. */
@Database(
    entities = [SignalementEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TatitraDatabase : RoomDatabase() {

    abstract fun signalementDao(): SignalementDao

    companion object {
        private const val NOM_BASE = "tatitra.db"

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
            ).build()
    }
}
