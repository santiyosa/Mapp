package com.maintenance.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.maintenance.app.data.database.dao.AppSettingsDAO
import com.maintenance.app.data.database.dao.MaintenanceDAO
import com.maintenance.app.data.database.dao.RecordDAO
import com.maintenance.app.data.database.dao.UISettingsDAO
import com.maintenance.app.data.database.entities.AppSettingsEntity
import com.maintenance.app.data.database.entities.MaintenanceEntity
import com.maintenance.app.data.database.entities.RecordEntity
import com.maintenance.app.data.database.entities.UISettingsEntity

/**
 * Room Database class for the Maintenance App.
 * This class defines the database configuration and serves as the main access point
 * for the underlying connection to your app's persisted, relational data.
 */
@Database(
    entities = [
        RecordEntity::class,
        MaintenanceEntity::class,
        UISettingsEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MaintenanceDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDAO
    abstract fun maintenanceDao(): MaintenanceDAO
    abstract fun uiSettingsDao(): UISettingsDAO
    abstract fun appSettingsDao(): AppSettingsDAO

    companion object {
        const val DATABASE_NAME = "maintenance_database"

        @Volatile
        private var INSTANCE: MaintenanceDatabase? = null

        fun getDatabase(context: Context): MaintenanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaintenanceDatabase::class.java,
                    DATABASE_NAME
                )
                    .addTypeConverter(Converters())
                    .addCallback(DatabaseCallback())
                    // Add migrations as needed
                    // .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Database callback for initialization tasks.
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate database with initial data if needed
                // This runs on a background thread
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Database is ready to use
            }
        }

        /**
         * Example migration from version 1 to 2.
         * Uncomment and modify as needed for future schema changes.
         */
        /*
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here
                // Example: database.execSQL("ALTER TABLE records ADD COLUMN new_column TEXT")
            }
        }
        */
    }
}