package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.data.Usuario
import com.example.appmovil.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File

class ExplorarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExplorarScreen()
        }
    }
}

@Composable
fun ExplorarScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var users by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    LaunchedEffect(Unit) {
        val myId = SessionManager.getUserId(context)
        if (myId != -1) {
            users = db.appDao().getAllUsersExcept(myId)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBarExplorar(context) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Explorar Mapas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users) { user ->
                    UserMapItem(user) {
                        // Click on User Map -> Open MapActivity with TARGET_USER_ID
                        val intent = Intent(context, MapaActivity::class.java)
                        intent.putExtra("TARGET_USER_ID", user.id_usuario)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun UserMapItem(user: Usuario, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            // Map Preview
            if (user.fotoMapaCaptura != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(user.fotoMapaCaptura))
                        .crossfade(true)
                        .build(),
                    contentDescription = "User Map",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.mapa_guia), // Reuse the map image
                    contentDescription = "User Map",
                    contentScale = ContentScale.Fit, // Fit entire map
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // User Photo
                if (user.fotoPerfilUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.fotoPerfilUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = user.nombre ?: user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBarExplorar(context: android.content.Context) {
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
             BottomNavItemEx(icon = Icons.Default.Home, label = "Inicio", selected = false)
        }

        // Explore (Selected)
        BottomNavItemEx(icon = Icons.Default.Search, label = "Explorar", selected = true)

        // Notif
        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, NotificationsActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItemEx(icon = Icons.Default.Notifications, label = "Notific.", selected = false)
        }

        // Profile
        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, ProfileActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItemEx(icon = Icons.Default.Person, label = "Perfil", selected = false)
        }
    }
}

@Composable
fun BottomNavItemEx(icon: ImageVector, label: String, selected: Boolean) {
    val tintColor = if (selected) Color(0xFFFFC107) else Color.White 
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = tintColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = tintColor)
    }
}
