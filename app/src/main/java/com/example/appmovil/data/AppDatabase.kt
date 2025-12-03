package com.example.appmovil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Usuario::class, Departamento::class, Publicacion::class, Foto::class, MapaLike::class, Notificacion::class, UsuarioFollow::class, PublicacionLike::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_peru_database"
                )
                .fallbackToDestructiveMigration() // Borra la BD si cambias la estructura (solo en desarrollo)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}