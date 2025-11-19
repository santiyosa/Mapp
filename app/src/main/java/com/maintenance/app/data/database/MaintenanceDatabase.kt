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
import com.maintenance.app.data.database.dao.SearchDAO
import com.maintenance.app.data.database.dao.SearchHistoryDAO
import com.maintenance.app.data.database.dao.UISettingsDAO
import com.maintenance.app.data.database.daos.MaintenanceDraftDao
import com.maintenance.app.data.database.entities.AppSettingsEntity
import com.maintenance.app.data.database.entities.MaintenanceEntity
import com.maintenance.app.data.database.entities.RecordEntity
import com.maintenance.app.data.database.entities.SearchHistoryEntity
import com.maintenance.app.data.database.entities.UISettingsEntity
import com.maintenance.app.data.database.entities.MaintenanceDraftEntity
import com.maintenance.app.data.database.entities.SettingsEntity
import com.maintenance.app.data.database.dao.SettingsDao

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
        AppSettingsEntity::class,
        MaintenanceDraftEntity::class,
        SearchHistoryEntity::class,
        SettingsEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MaintenanceDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDAO
    abstract fun maintenanceDao(): MaintenanceDAO
    abstract fun uiSettingsDao(): UISettingsDAO
    abstract fun appSettingsDao(): AppSettingsDAO
    abstract fun maintenanceDraftDao(): MaintenanceDraftDao
    abstract fun searchDao(): SearchDAO
    abstract fun searchHistoryDao(): SearchHistoryDAO
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "maintenance_database"

        @Volatile
        private var INSTANCE: MaintenanceDatabase? = null

        fun getDatabase(context: Context, converters: Converters? = null): MaintenanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    MaintenanceDatabase::class.java,
                    DATABASE_NAME
                )
                
                // Add type converter if provided (from Hilt)
                if (converters != null) {
                    builder.addTypeConverter(converters)
                }
                
                val instance = builder
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
         * Migration from version 1 to 2 - adds maintenance drafts table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS maintenance_drafts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        record_id INTEGER NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        type TEXT NOT NULL DEFAULT '',
                        cost TEXT NOT NULL DEFAULT '',
                        currency TEXT NOT NULL DEFAULT 'COP',
                        performed_by TEXT NOT NULL DEFAULT '',
                        location TEXT NOT NULL DEFAULT '',
                        duration_minutes TEXT NOT NULL DEFAULT '',
                        parts_replaced TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        priority TEXT NOT NULL DEFAULT 'MEDIUM',
                        is_recurring INTEGER NOT NULL DEFAULT 0,
                        recurrence_interval_days TEXT NOT NULL DEFAULT '',
                        selected_images TEXT NOT NULL DEFAULT '',
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                
                // Create index for faster lookups
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_maintenance_drafts_record_id ON maintenance_drafts(record_id)"
                )
            }
        }

        /**
         * Migration from version 2 to 3 - adds search history table.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS search_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        query TEXT NOT NULL,
                        search_criteria TEXT NOT NULL,
                        result_count INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                
                // Create index for faster timestamp-based queries
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_search_history_timestamp ON search_history(timestamp)"
                )
            }
        }

        /**
         * Migration from version 3 to 4 - adds app settings table.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_app_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
                        language TEXT NOT NULL DEFAULT 'SPANISH',
                        enable_notifications INTEGER NOT NULL DEFAULT 1,
                        enable_biometric INTEGER NOT NULL DEFAULT 0,
                        enable_auto_backup INTEGER NOT NULL DEFAULT 1,
                        backup_frequency_days INTEGER NOT NULL DEFAULT 7,
                        enable_data_collection INTEGER NOT NULL DEFAULT 0,
                        last_updated INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}