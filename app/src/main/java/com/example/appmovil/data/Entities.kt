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
    ],
    indices = [
        Index(value = ["fk_usuario"]),
        Index(value = ["fk_departamento"])
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
    ],
    indices = [Index(value = ["fk_publicacion"])]
)
data class Foto(
    @PrimaryKey(autoGenerate = true) val id_foto: Int = 0,
    val fk_publicacion: Int,
    @ColumnInfo(name = "foto_url") val fotoUrl: String,
    val orden: Int?,
    @ColumnInfo(name = "principal_mapa") val principalMapa: Boolean = false
)

@Entity(
    tableName = "mapa_like",
    primaryKeys = ["fk_user_from", "fk_user_to"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_user_from"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_user_to"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["fk_user_from"]),
        Index(value = ["fk_user_to"])
    ]
)
data class MapaLike(
    val fk_user_from: Int,
    val fk_user_to: Int,
    val fecha: Date = Date()
)

@Entity(
    tableName = "notificacion",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_usuario_destino"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_usuario_origen"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["fk_usuario_destino"]), Index(value = ["fk_usuario_origen"])]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true) val id_notificacion: Int = 0,
    val fk_usuario_destino: Int,
    val fk_usuario_origen: Int? = null, // Quién hizo la acción (para ir a su perfil)
    val tipo: String, // "LIKE", "COMENTARIO", "FOLLOW", etc.
    val mensaje: String,
    val fecha: Date = Date(),
    val leido: Boolean = false
)

@Entity(
    tableName = "publicacion_like",
    primaryKeys = ["fk_usuario", "fk_publicacion"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_usuario"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Publicacion::class, parentColumns = ["id_publicacion"], childColumns = ["fk_publicacion"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["fk_usuario"]),
        Index(value = ["fk_publicacion"])
    ]
)
data class PublicacionLike(
    val fk_usuario: Int,
    val fk_publicacion: Int,
    val fecha: Date = Date()
)

@Entity(
    tableName = "usuario_follow",
    primaryKeys = ["fk_follower", "fk_followed"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_follower"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["fk_followed"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["fk_follower"]),
        Index(value = ["fk_followed"])
    ]
)
data class UsuarioFollow(
    val fk_follower: Int,
    val fk_followed: Int,
    val fecha: Date = Date()
)