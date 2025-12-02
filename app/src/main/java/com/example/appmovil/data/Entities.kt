package com.example.appmovil.data

import androidx.room.*
import java.util.Date

// --- CONVERTERS (Para manejar Fechas) ---
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

// --- ENTITIES (TABLAS) ---

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val username: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val nombre: String?,
    @ColumnInfo(name = "foto_perfil_url") val fotoPerfilUrl: String?,
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: Date?,
    val privado: Boolean = false
)

@Entity(
    tableName = "departamento",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Departamento(
    @PrimaryKey(autoGenerate = true) val id_departamento: Int = 0,
    val nombre: String,
    @ColumnInfo(name = "svg_data") val svgData: String?
)

@Entity(
    tableName = "publicacion",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["fk_usuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Departamento::class,
            parentColumns = ["id_departamento"],
            childColumns = ["fk_departamento"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Publicacion(
    @PrimaryKey(autoGenerate = true) val id_publicacion: Int = 0,
    val fk_usuario: Int,
    val fk_departamento: Int,
    val titulo: String,
    val reseña: String?,
    @ColumnInfo(name = "fecha_viaje") val fechaViaje: Date?,
    val publicado: Boolean = false
)

@Entity(
    tableName = "foto",
    foreignKeys = [
        ForeignKey(
            entity = Publicacion::class,
            parentColumns = ["id_publicacion"],
            childColumns = ["fk_publicacion"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Foto(
    @PrimaryKey(autoGenerate = true) val id_foto: Int = 0,
    val fk_publicacion: Int,
    @ColumnInfo(name = "foto_url") val fotoUrl: String,
    val orden: Int?,
    @ColumnInfo(name = "principal_mapa") val principalMapa: Boolean = false
)
