package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy Data
        val notifications = listOf(
            Notification(1, "¡Nuevo comentario!", "Juan comentó en tu mapa de Cuzco: '¡Qué hermoso lugar!'", NotificationType.COMMENT),
            Notification(2, "¡Te dieron un corazón!", "A Maria le encantó tu racha de 5 días.", NotificationType.HEART),
            Notification(3, "¡Nuevo comentario!", "Pedro comentó en tu mapa de Lima: '¿Dónde es eso?'", NotificationType.COMMENT),
            Notification(4, "¡Te dieron un corazón!", "A Luis le gustó tu progreso.", NotificationType.HEART),
            Notification(5, "¡Nuevo comentario!", "Ana comentó: 'Sigue así, vas genial.'", NotificationType.COMMENT)
        )

        val adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter

        // Navigation
        val btnInicio = findViewById<LinearLayout>(R.id.btnInicio)
        btnInicio.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        val btnPerfil = findViewById<LinearLayout>(R.id.btnPerfil)
        btnPerfil.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}