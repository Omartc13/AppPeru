package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.data.Notificacion
import com.example.appmovil.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationsScreen()
        }
    }
}

@Composable
fun NotificationsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var notifications by remember { mutableStateOf<List<Notificacion>>(emptyList()) }

    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId(context)
        if (userId != -1) {
            notifications = db.appDao().getNotificacionesByUser(userId)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBarNotif(context) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Text(
                "Notificaciones",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {
                items(notifications) { notif ->
                    NotificationItem(notif)
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: Notificacion) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notif.leido) Color.White else Color(0xFFE3F2FD))
            .clickable {
                if (notif.fk_usuario_origen != null) {
                    val intent = Intent(context, MapaActivity::class.java)
                    intent.putExtra("TARGET_USER_ID", notif.fk_usuario_origen)
                    context.startActivity(intent)
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on Type
        val icon = when (notif.tipo) {
            "LIKE" -> Icons.Default.Favorite
            else -> Icons.Default.Notifications
        }
        val iconColor = when (notif.tipo) {
            "LIKE" -> Color(0xFFE91E63) // Pink
            else -> Color.Gray
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = notif.mensaje,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            Text(
                text = dateFormat.format(notif.fecha),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BottomNavigationBarNotif(context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D3E50))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Home
        Box(modifier = Modifier.clickable { 
            val intent = Intent(context, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }) {
             BottomNavItemNotif(icon = Icons.Default.Home, label = "Inicio", selected = false)
        }

        // Explore
        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, ExplorarActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItemNotif(icon = Icons.Default.Search, label = "Explorar", selected = false)
        }

        // Notif (Selected)
        BottomNavItemNotif(icon = Icons.Default.Notifications, label = "Notific.", selected = true)

        // Profile
        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, ProfileActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItemNotif(icon = Icons.Default.Person, label = "Perfil", selected = false)
        }
    }
}

@Composable
fun BottomNavItemNotif(icon: ImageVector, label: String, selected: Boolean) {
    val tintColor = if (selected) Color(0xFFFFC107) else Color.White 
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = tintColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = tintColor)
    }
}
