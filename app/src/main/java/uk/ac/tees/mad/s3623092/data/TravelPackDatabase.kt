package uk.ac.tees.mad.s3623092.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TripEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TravelPackDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TravelPackDatabase? = null

        fun getDatabase(context: Context): TravelPackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelPackDatabase::class.java,
                    "travelpack_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}