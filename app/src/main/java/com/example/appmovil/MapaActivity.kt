package com.example.appmovil

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
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
import com.example.appmovil.data.Publicacion
import com.example.appmovil.utils.SessionManager
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.coroutines.launch
import java.util.Date

// Helper para almacenar la configuración de cada departamento
data class DeptConfig(val idView: Int, val idMask: Int, val nombre: String)

class MapaActivity : AppCompatActivity() {

    private var imagenDestino: ImageView? = null
    private var mascaraActual: Int = 0
    private var currentDeptName: String = ""

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

        // Inicializar Listeners usando la lista
        departamentosList.forEach { dept ->
            configurarDepto(dept)
        }

        val btnPerfil = findViewById<LinearLayout>(R.id.btnPerfil)
        btnPerfil.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val btnNotificaciones = findViewById<LinearLayout>(R.id.btnNotificaciones)
        btnNotificaciones.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadMapImages() // Recargar imágenes del mapa
    }

    private fun loadUserData() {
        val txtHello = findViewById<TextView>(R.id.txtHello)
        val imgProfileHeader = findViewById<ImageView>(R.id.imgProfileHeader)
        val db = AppDatabase.getDatabase(this)
        val userId = SessionManager.getUserId(this)

        if (userId != -1) {
            lifecycleScope.launch {
                val user = db.appDao().getUsuarioById(userId)
                if (user != null) {
                    val displayName = user.nombre ?: user.username
                    txtHello.text = "Hola, $displayName!"

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
            }
        }
    }

    private fun loadMapImages() {
        val db = AppDatabase.getDatabase(this)
        val userId = SessionManager.getUserId(this)

        if (userId != -1) {
            lifecycleScope.launch {
                departamentosList.forEach { dept ->
                    val photoUrl = db.appDao().getLatestPhotoUrlForDept(userId, dept.nombre)
                    if (photoUrl != null) {
                        val imageView = findViewById<ImageView>(dept.idView)
                        val transformaciones = MultiTransformation(
                            CenterCrop(),
                            MaskTransformation(dept.idMask)
                        )
                        Glide.with(this@MapaActivity)
                            .load(photoUrl)
                            .apply(RequestOptions.bitmapTransform(transformaciones))
                            .into(imageView)
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
            
            showOptionsDialog()
        }
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
                // 1. Ensure Department Exists
                var dept = db.appDao().getDepartamentoByName(currentDeptName)
                if (dept == null) {
                    db.appDao().insertDepartamento(Departamento(nombre = currentDeptName, svgData = null))
                    dept = db.appDao().getDepartamentoByName(currentDeptName)
                }

                if (dept != null) {
                    // 2. Save Persistable Permission
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        // Ignore
                    }

                    // 3. Insert Publicacion
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

                    // 4. Insert Foto
                    db.appDao().insertFoto(
                        Foto(
                            fk_publicacion = pubId.toInt(),
                            fotoUrl = uri.toString(),
                            orden = 0,
                            principalMapa = true
                        )
                    )

                    // 5. Visual Update on Map
                    updateMapVisual(uri)
                    
                    Toast.makeText(this@MapaActivity, "Guardado!", Toast.LENGTH_SHORT).show()
                    
                    // 6. Reload images to ensure correct order is respected if logic changed
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