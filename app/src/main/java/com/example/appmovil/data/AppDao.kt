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

    @Query("SELECT * FROM publicacion WHERE fk_usuario = :userId AND fk_departamento = :deptId ORDER BY fecha_viaje DESC")
    suspend fun getPublicacionesByUserAndDept(userId: Int, deptId: Int): List<Publicacion>

    // --- FOTO ---
    @Insert
    suspend fun insertFoto(foto: Foto)

    @Query("SELECT * FROM foto WHERE fk_publicacion = :publicacionId ORDER BY orden ASC")
    suspend fun getFotosByPublicacion(publicacionId: Int): List<Foto>
    
    // Obtener la foto más reciente de un departamento (para pintar el mapa)
    @Query("""
        SELECT f.foto_url FROM foto f
        INNER JOIN publicacion p ON f.fk_publicacion = p.id_publicacion
        INNER JOIN departamento d ON p.fk_departamento = d.id_departamento
        WHERE p.fk_usuario = :userId AND d.nombre = :deptName
        ORDER BY p.fecha_viaje DESC, f.id_foto DESC
        LIMIT 1
    """)
    suspend fun getLatestPhotoUrlForDept(userId: Int, deptName: String): String?

    @Delete
    suspend fun deletePublicacion(publicacion: Publicacion)

    // 1. Obtener URL priorizando 'principal_mapa', luego la más reciente
    @Query("""
        SELECT f.foto_url FROM foto f
        INNER JOIN publicacion p ON f.fk_publicacion = p.id_publicacion
        INNER JOIN departamento d ON p.fk_departamento = d.id_departamento
        WHERE p.fk_usuario = :userId AND d.nombre = :deptName
        ORDER BY f.principal_mapa DESC, p.fecha_viaje DESC, f.id_foto DESC
        LIMIT 1
    """)
    suspend fun getLatestPhotoUrlForDept(userId: Int, deptName: String): String?

    // 2. Obtener el objeto Foto completo que es portada actualmente (para mostrarlo arriba en la galería)
    @Query("""
        SELECT f.* FROM foto f
        INNER JOIN publicacion p ON f.fk_publicacion = p.id_publicacion
        INNER JOIN departamento d ON p.fk_departamento = d.id_departamento
        WHERE p.fk_usuario = :userId AND d.nombre = :deptName
        ORDER BY f.principal_mapa DESC, p.fecha_viaje DESC, f.id_foto DESC
        LIMIT 1
    """)
    suspend fun getFotoPrincipalMapaObject(userId: Int, deptName: String): Foto?

    // 3. Quitar marca de principal a todas las fotos de ese usuario en ese departamento
    @Query("""
        UPDATE foto SET principal_mapa = 0 
        WHERE id_foto IN (
            SELECT f.id_foto FROM foto f
            INNER JOIN publicacion p ON f.fk_publicacion = p.id_publicacion
            INNER JOIN departamento d ON p.fk_departamento = d.id_departamento
            WHERE p.fk_usuario = :userId AND d.nombre = :deptName
        )
    """)
    suspend fun clearPrincipalMapaForDept(userId: Int, deptName: String)

    // 4. Marcar una foto específica como principal
    @Query("UPDATE foto SET principal_mapa = 1 WHERE id_foto = :fotoId")
    suspend fun setPrincipalMapa(fotoId: Int)
}
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