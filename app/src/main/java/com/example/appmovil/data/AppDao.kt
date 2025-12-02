package com.example.appmovil.data

import androidx.room.*

@Dao
interface AppDao {
    // --- USUARIO ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuario WHERE username = :username LIMIT 1")
    suspend fun getUsuarioByUsername(username: String): Usuario?

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    suspend fun getUsuarioById(id: Int): Usuario?

    @Query("UPDATE usuario SET foto_perfil_url = :url WHERE id_usuario = :id")
    suspend fun updateFotoPerfil(id: Int, url: String)

    // --- DEPARTAMENTO ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDepartamento(departamento: Departamento)

    @Query("SELECT * FROM departamento WHERE nombre = :nombre")
    suspend fun getDepartamentoByName(nombre: String): Departamento?

    @Query("SELECT * FROM departamento")
    suspend fun getAllDepartamentos(): List<Departamento>

    // --- PUBLICACION ---
    @Insert
    suspend fun insertPublicacion(publicacion: Publicacion): Long

    @Query("SELECT * FROM publicacion WHERE fk_usuario = :userId")
    suspend fun getPublicacionesByUser(userId: Int): List<Publicacion>

    // --- FOTO ---
    @Insert
    suspend fun insertFoto(foto: Foto)

    @Query("SELECT * FROM foto WHERE fk_publicacion = :publicacionId ORDER BY orden ASC")
    suspend fun getFotosByPublicacion(publicacionId: Int): List<Foto>
    
    // Consulta compleja: Obtener la foto principal del mapa para un usuario y departamento específico
    @Query("""
        SELECT f.* FROM foto f
        INNER JOIN publicacion p ON f.fk_publicacion = p.id_publicacion
        WHERE p.fk_usuario = :userId 
        AND p.fk_departamento = :deptoId 
        AND f.principal_mapa = 1 
        LIMIT 1
    """)
    suspend fun getFotoPrincipalMapa(userId: Int, deptoId: Int): Foto?
}