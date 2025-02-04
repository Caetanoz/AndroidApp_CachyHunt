package pt.ipp.estg.cachyhunt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.ipp.estg.cachyhunt.data.local.dao.GeocacheCapturedDao
import pt.ipp.estg.cachyhunt.data.local.dao.GeocacheDao
import pt.ipp.estg.cachyhunt.data.local.dao.QuestionDao
import pt.ipp.estg.cachyhunt.data.local.dao.UserDao
import pt.ipp.estg.cachyhunt.data.models.Converters
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.models.User

@Database(entities = [User::class, Geocache::class, GeocacheCaptured::class, Question::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun geocacheDao(): GeocacheDao
    abstract fun geocacheCapturedDao(): GeocacheCapturedDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}