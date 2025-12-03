package com.example.appmovil

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.data.Departamento
import com.example.appmovil.data.Foto
import com.example.appmovil.data.MapaLike
import com.example.appmovil.data.Notificacion
import com.example.appmovil.data.Publicacion
import com.example.appmovil.data.UsuarioFollow
import com.example.appmovil.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.coroutines.launch
import java.util.Date
import android.widget.Button

// Helper para almacenar la configuración de cada departamento
data class DeptConfig(val idView: Int, val idMask: Int, val nombre: String)

class MapaActivity : AppCompatActivity() {

    private var imagenDestino: ImageView? = null
    private var mascaraActual: Int = 0
    private var currentDeptName: String = ""
    
    // Visiting Logic
    private var targetUserId: Int = -1
    private var isMyMap: Boolean = true
    private var currentUserId: Int = -1

    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && imagenDestino != null) {
            showUploadDialog(uri)
        }
    }

    // Lista de departamentos
    private val departamentosList = listOf(
        DeptConfig(R.id.imgLima, R.drawable.mask_lima, "Lima"),
        DeptConfig(R.id.imgAncash, R.drawable.mask_ancash, "Áncash"),
        DeptConfig(R.id.imgLa_libertad, R.drawable.mask_la_libertad, "La Libertad"),
        DeptConfig(R.id.imgCajamarca, R.drawable.mask_cajamarca, "Cajamarca"),
        DeptConfig(R.id.imgPiura, R.drawable.mask_piura, "Piura"),
        DeptConfig(R.id.imgLambayeque, R.drawable.mask_lambayeque, "Lambayeque"),
        DeptConfig(R.id.imgTumbes, R.drawable.mask_tumbes, "Tumbes"),
        DeptConfig(R.id.imgIca, R.drawable.mask_ica, "Ica"),
        DeptConfig(R.id.imgArequipa, R.drawable.mask_arequipa, "Arequipa"),
        DeptConfig(R.id.imgMoquegua, R.drawable.mask_moquegua, "Moquegua"),
        DeptConfig(R.id.imgTacna, R.drawable.mask_tacna, "Tacna"),
        DeptConfig(R.id.imgAmazonas, R.drawable.mask_amazonas, "Amazonas"),
        DeptConfig(R.id.imgSanMartin, R.drawable.mask_san_martin, "San Martín"),
        DeptConfig(R.id.imgHuanuco, R.drawable.mask_huanuco, "Huánuco"),
        DeptConfig(R.id.imgPasco, R.drawable.mask_pasco, "Pasco"),
        DeptConfig(R.id.imgJunin, R.drawable.mask_junin, "Junín"),
        DeptConfig(R.id.imgHuancavelica, R.drawable.mask_huancavelica, "Huancavelica"),
        DeptConfig(R.id.imgAyacucho, R.drawable.mask_ayacucho, "Ayacucho"),
        DeptConfig(R.id.imgApurimac, R.drawable.mask_apurimac, "Apurímac"),
        DeptConfig(R.id.imgLoreto, R.drawable.mask_loreto, "Loreto"),
        DeptConfig(R.id.imgUcayali, R.drawable.mask_ucayali, "Ucayali"),
        DeptConfig(R.id.imgMadre_de_Dios, R.drawable.mask_madre_de_dios, "Madre de Dios"),
        DeptConfig(R.id.imgPuno, R.drawable.mask_puno, "Puno"),
        DeptConfig(R.id.imgCuzco, R.drawable.mask_cuzco, "Cusco")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        currentUserId = SessionManager.getUserId(this)
        
        // Check if we are visiting another user
        val intentTarget = intent.getIntExtra("TARGET_USER_ID", -1)
        if (intentTarget != -1 && intentTarget != currentUserId) {
            targetUserId = intentTarget
            isMyMap = false
        } else {
            targetUserId = currentUserId
            isMyMap = true
        }

        // Inicializar Listeners usando la lista
        departamentosList.forEach { dept ->
            configurarDepto(dept)
        }

        setupUI()
    }
    
    private fun setupUI() {
        val btnPerfil = findViewById<LinearLayout>(R.id.btnPerfil)
        val btnNotificaciones = findViewById<LinearLayout>(R.id.btnNotificaciones)
        val btnLike = findViewById<FloatingActionButton>(R.id.fab_like)
        val txtLikeCount = findViewById<TextView>(R.id.txtLikeCount)
        val btnFollow = findViewById<Button>(R.id.btnFollow)
        val headerLayout = findViewById<LinearLayout>(R.id.headerLayout)

        if (isMyMap) {
            // Normal Navigation
            btnPerfil.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            btnNotificaciones.setOnClickListener {
                startActivity(Intent(this, NotificationsActivity::class.java))
            }
            
            // SHOW Like Count/Button even for MY map (read-only or self-like depending on preference, 
            // usually read-only count is good, but let's show it always as requested)
             if (btnLike != null) {
                 btnLike.visibility = View.VISIBLE
                 // Disable click for self-like? Or allow it? Let's allow viewing count primarily.
                 // For now, we setup it normally, but maybe disable click if isMyMap if desired.
                 setupLikeButton(btnLike, txtLikeCount) 
             }
             if (txtLikeCount != null) txtLikeCount.visibility = View.VISIBLE

             if (btnFollow != null) btnFollow.visibility = View.GONE
        } else {
            // Visitor Mode
            btnPerfil.setOnClickListener { finish() } 
            btnNotificaciones.setOnClickListener { finish() }

             if (btnLike != null) {
                 btnLike.visibility = View.VISIBLE
                 if (txtLikeCount != null) setupLikeButton(btnLike, txtLikeCount)
             }
             if (txtLikeCount != null) txtLikeCount.visibility = View.VISIBLE
             
             if (btnFollow != null) {
                 btnFollow.visibility = View.VISIBLE
                 setupFollowButton(btnFollow)
             }
             
             headerLayout.setOnClickListener { finish() }
        }
    }
    
    private fun setupFollowButton(btn: Button) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val count = db.appDao().isFollowing(currentUserId, targetUserId)
            var isFollowing = count > 0
            updateFollowButton(btn, isFollowing)
            
            btn.setOnClickListener {
                lifecycleScope.launch {
                    if (isFollowing) {
                        db.appDao().deleteFollow(currentUserId, targetUserId)
                        isFollowing = false
                        Toast.makeText(this@MapaActivity, "Dejaste de seguir", Toast.LENGTH_SHORT).show()
                    } else {
                        db.appDao().insertFollow(UsuarioFollow(currentUserId, targetUserId))
                        db.appDao().insertNotificacion(
                            Notificacion(
                                fk_usuario_destino = targetUserId,
                                fk_usuario_origen = currentUserId, // NEW
                                tipo = "FOLLOW",
                                mensaje = "¡Te ha empezado a seguir!",
                                leido = false
                            )
                        )
                        isFollowing = true
                        Toast.makeText(this@MapaActivity, "Siguiendo usuario", Toast.LENGTH_SHORT).show()
                    }
                    updateFollowButton(btn, isFollowing)
                }
            }
        }
    }
    
    private fun updateFollowButton(btn: Button, isFollowing: Boolean) {
        if (isFollowing) {
            btn.text = "Siguiendo"
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
        } else {
            btn.text = "Seguir"
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2196F3")) // Blue
        }
    }
    
    private fun setupLikeButton(fab: FloatingActionButton, txtCount: TextView) {
        val db = AppDatabase.getDatabase(this)
        
        lifecycleScope.launch {
            // Initial State
            updateLikeState(db, fab, txtCount)
            
            fab.setOnClickListener {
                if (isMyMap) {
                    Toast.makeText(this@MapaActivity, "Estos son tus likes totales", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val count = db.appDao().hasUserLiked(currentUserId, targetUserId)
                    val isLiked = count > 0

                    if (isLiked) {
                        db.appDao().deleteLike(currentUserId, targetUserId)
                        Toast.makeText(this@MapaActivity, "Like eliminado", Toast.LENGTH_SHORT).show()
                    } else {
                        db.appDao().insertLike(MapaLike(currentUserId, targetUserId))
                        db.appDao().insertNotificacion(
                            Notificacion(
                                fk_usuario_destino = targetUserId,
                                fk_usuario_origen = currentUserId,
                                tipo = "LIKE",
                                mensaje = "A alguien le gustó tu mapa",
                                leido = false
                            )
                        )
                        Toast.makeText(this@MapaActivity, "¡Le diste Like al mapa!", Toast.LENGTH_SHORT).show()
                    }
                    updateLikeState(db, fab, txtCount)
                }
            }
        }
    }
    
    private suspend fun updateLikeState(db: AppDatabase, fab: FloatingActionButton, txtCount: TextView) {
        val likes = db.appDao().countLikesForUser(targetUserId)
        txtCount.text = likes.toString()
        
        if (!isMyMap) {
            val count = db.appDao().hasUserLiked(currentUserId, targetUserId)
            val isLiked = count > 0
            updateLikeIcon(fab, isLiked)
        } else {
            // If my map, show filled heart purely decorative or border?
            // Usually filled represents "Love received".
            fab.setImageResource(R.drawable.ic_favorite_filled)
        }
    }
    
    private fun updateLikeIcon(fab: FloatingActionButton, isLiked: Boolean) {
        if (isLiked) {
            fab.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            fab.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadMapImages()
        bindBottomNavigation()
    }
    
    private fun bindBottomNavigation() {
        val btnInicio = findViewById<LinearLayout>(R.id.btnInicio)
        val btnExplorar = findViewById<LinearLayout>(R.id.btnExplorar)
        val btnNotificaciones = findViewById<LinearLayout>(R.id.btnNotificaciones)
        val btnPerfil = findViewById<LinearLayout>(R.id.btnPerfil)
        
        btnInicio.setOnClickListener {
            // ALWAYS go to MY map (clear extras)
            val intent = Intent(this, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        
        btnExplorar.setOnClickListener {
            startActivity(Intent(this, ExplorarActivity::class.java))
        }
        
        btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadUserData() {
        val txtHello = findViewById<TextView>(R.id.txtHello)
        val imgProfileHeader = findViewById<ImageView>(R.id.imgProfileHeader)
        val db = AppDatabase.getDatabase(this)
        
        if (targetUserId != -1) {
            lifecycleScope.launch {
                val user = db.appDao().getUsuarioById(targetUserId)
                if (user != null) {
                    val displayName = user.nombre ?: user.username
                    if (isMyMap) {
                        txtHello.text = "Hola, $displayName!"
                    } else {
                        txtHello.text = "Mapa de $displayName"
                    }

                    if (user.fotoPerfilUrl != null) {
                         imgProfileHeader.clearColorFilter()
                         Glide.with(this@MapaActivity)
                            .load(user.fotoPerfilUrl)
                            .circleCrop()
                            .into(imgProfileHeader)
                    } else {
                        imgProfileHeader.setImageResource(R.drawable.ic_person)
                        imgProfileHeader.setColorFilter(android.graphics.Color.parseColor("#757575"))
                    }
                }
                
                // Show Like Count for My Map?
                if (isMyMap) {
                    val btnFollow = findViewById<Button>(R.id.btnFollow)
                    btnFollow.visibility = View.VISIBLE // Reuse this button slot or create new TextView
                    btnFollow.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                    btnFollow.text = "" // Clear text
                    // We will use this button as a status text or hide it and use another view.
                    // Better: Hide Follow Button and show a Text with Heart Icon
                    btnFollow.visibility = View.GONE
                    
                    // We need to add a TextView for stats in XML to do this properly.
                    // For now, let's skip complex XML edits and focus on functionality.
                }
            }
        }
    }

    private fun loadMapImages() {
        val db = AppDatabase.getDatabase(this)
        
        if (targetUserId != -1) {
            lifecycleScope.launch {
                departamentosList.forEach { dept ->
                    val photoUrl = db.appDao().getLatestPhotoUrlForDept(targetUserId, dept.nombre)
                    val imageView = findViewById<ImageView>(dept.idView)
                    
                    if (photoUrl != null) {
                        val transformaciones = MultiTransformation(
                            CenterCrop(),
                            MaskTransformation(dept.idMask)
                        )
                        Glide.with(this@MapaActivity)
                            .load(photoUrl)
                            .apply(RequestOptions.bitmapTransform(transformaciones))
                            .into(imageView)
                    } else {
                         imageView.setImageDrawable(null)
                         imageView.setImageResource(dept.idMask)
                    }
                }
            }
        }
    }

    private fun configurarDepto(dept: DeptConfig) {
        val imageView = findViewById<ImageView>(dept.idView)
        imageView.setOnClickListener {
            imagenDestino = imageView
            mascaraActual = dept.idMask
            currentDeptName = dept.nombre
            
            if (isMyMap) {
                showOptionsDialog()
            } else {
                openGalleryForVisitor()
            }
        }
    }
    
    private fun openGalleryForVisitor() {
        val intent = Intent(this, DepartmentPostsActivity::class.java)
        intent.putExtra("DEPT_NAME", currentDeptName)
        intent.putExtra("TARGET_USER_ID", targetUserId)
        startActivity(intent)
    }

    private fun showOptionsDialog() {
        val options = arrayOf("Subir Foto", "Ver Galería")
        AlertDialog.Builder(this)
            .setTitle(currentDeptName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> abrirGaleria.launch("image/*")
                    1 -> {
                        val intent = Intent(this, DepartmentPostsActivity::class.java)
                        intent.putExtra("DEPT_NAME", currentDeptName)
                        intent.putExtra("TARGET_USER_ID", currentUserId)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    private fun showUploadDialog(uri: Uri) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_upload_post, null)
        
        val imgPreview = dialogView.findViewById<ImageView>(R.id.preview_image)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_title)
        val etDesc = dialogView.findViewById<EditText>(R.id.et_description)

        imgPreview.setImageURI(uri)

        builder.setView(dialogView)
            .setTitle("Nueva Publicación en $currentDeptName")
            .setPositiveButton("Guardar") { _, _ ->
                val title = etTitle.text.toString()
                val desc = etDesc.text.toString()
                savePostToDb(uri, title, desc)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun savePostToDb(uri: Uri, title: String, desc: String) {
        val db = AppDatabase.getDatabase(this)
        val userId = SessionManager.getUserId(this)

        if (userId == -1) return

        lifecycleScope.launch {
            try {
                var dept = db.appDao().getDepartamentoByName(currentDeptName)
                if (dept == null) {
                    db.appDao().insertDepartamento(Departamento(nombre = currentDeptName, svgData = null))
                    dept = db.appDao().getDepartamentoByName(currentDeptName)
                }

                if (dept != null) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) { }

                    val pubId = db.appDao().insertPublicacion(
                        Publicacion(
                            fk_usuario = userId,
                            fk_departamento = dept.id_departamento,
                            titulo = if (title.isBlank()) "Viaje a $currentDeptName" else title,
                            reseña = desc,
                            fechaViaje = Date(),
                            publicado = true
                        )
                    )

                    db.appDao().insertFoto(
                        Foto(
                            fk_publicacion = pubId.toInt(),
                            fotoUrl = uri.toString(),
                            orden = 0,
                            principalMapa = true
                        )
                    )

                    updateMapVisual(uri)
                    Toast.makeText(this@MapaActivity, "Guardado!", Toast.LENGTH_SHORT).show()
                    loadMapImages()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MapaActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateMapVisual(uri: Uri) {
        if (imagenDestino != null) {
            val transformaciones = MultiTransformation(
                CenterCrop(),
                MaskTransformation(mascaraActual)
            )
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.bitmapTransform(transformaciones))
                .into(imagenDestino!!)
        }
    }
}