package com.example.appmovil


import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.MaskTransformation

class MapaActivity : AppCompatActivity() {

    private var imagenDestino: ImageView? = null
    private var mascaraActual: Int = 0

    // Tu lanzador de galería (NO CAMBIA, sigue igual con el MultiTransformation)
    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && imagenDestino != null) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // --- ZONA DE CONEXIÓN ---
        // Solo agrega una línea por cada departamento aquí:

        configurarDepto(R.id.imgLima, R.drawable.mask_lima)
        configurarDepto(R.id.imgAncash, R.drawable.mask_ancash)
        configurarDepto(R.id.imgLa_libertad, R.drawable.mask_la_libertad)
        configurarDepto(R.id.imgCajamarca, R.drawable.mask_cajamarca)
        configurarDepto(R.id.imgPiura, R.drawable.mask_piura)
        configurarDepto(R.id.imgLambayeque, R.drawable.mask_lambayeque)
        configurarDepto(R.id.imgTumbes, R.drawable.mask_tumbes)
        configurarDepto(R.id.imgIca, R.drawable.mask_ica)
        configurarDepto(R.id.imgArequipa, R.drawable.mask_arequipa)
        configurarDepto(R.id.imgMoquegua, R.drawable.mask_moquegua)
        configurarDepto(R.id.imgTacna, R.drawable.mask_tacna)
        configurarDepto(R.id.imgAmazonas, R.drawable.mask_amazonas)
        configurarDepto(R.id.imgSanMartin, R.drawable.mask_san_martin)
        configurarDepto(R.id.imgHuanuco, R.drawable.mask_huanuco)
        configurarDepto(R.id.imgPasco, R.drawable.mask_pasco)
        configurarDepto(R.id.imgJunin, R.drawable.mask_junin)
        configurarDepto(R.id.imgHuancavelica, R.drawable.mask_huancavelica)
        configurarDepto(R.id.imgAyacucho, R.drawable.mask_ayacucho)
        configurarDepto(R.id.imgApurimac, R.drawable.mask_apurimac)
        configurarDepto(R.id.imgLoreto, R.drawable.mask_loreto)
        configurarDepto(R.id.imgUcayali, R.drawable.mask_ucayali)
        configurarDepto(R.id.imgMadre_de_Dios, R.drawable.mask_madre_de_dios)
        configurarDepto(R.id.imgPuno, R.drawable.mask_puno)
        configurarDepto(R.id.imgCuzco, R.drawable.mask_cuzco)


    }

    // --- FUNCIÓN MÁGICA ---
    // Esta función hace todo el trabajo sucio por ti
    private fun configurarDepto(idView: Int, idMask: Int) {
        val imageView = findViewById<ImageView>(idView)
        imageView.setOnClickListener {
            imagenDestino = imageView
            mascaraActual = idMask
            abrirGaleria.launch("image/*")
        }
    }
}