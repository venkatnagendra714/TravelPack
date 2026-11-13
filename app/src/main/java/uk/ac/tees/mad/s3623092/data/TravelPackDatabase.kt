package uk.ac.tees.mad.s3623092.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TripEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TravelPackDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE trips ADD COLUMN photoUri TEXT"
                )
            }
        }

        @Volatile
        private var INSTANCE: TravelPackDatabase? = null

        fun getDatabase(context: Context): TravelPackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TravelPackDatabase::class.java,
                    "travelpack_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}