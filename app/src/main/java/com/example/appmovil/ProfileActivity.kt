package com.example.appmovil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
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

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                onBackClick = { finish() },
                onLogout = {
                    SessionManager.clearSession(this)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(onBackClick: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    
    // Estado del usuario
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId(context)
        if (userId != -1) {
            usuario = db.appDao().getUsuarioById(userId)
            usuario?.fotoPerfilUrl?.let { 
                photoUri = Uri.parse(it)
            }
        }
    }

    // Selector de imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            // Guardar en Base de Datos
            val userId = SessionManager.getUserId(context)
            if (userId != -1) {
                scope.launch {
                    // Persistir permiso de lectura para el URI (importante para reinicios)
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        // En algunos dispositivos o fuentes esto puede fallar, pero intentamos
                    }
                    
                    db.appDao().updateFotoPerfil(userId, it.toString())
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Top Bar con Menu Hamborguesa funcional
            TopBar(onBackClick, onLogout)

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Info (Con nombre real y foto real)
            ProfileInfo(
                usuario = usuario, 
                currentPhoto = photoUri,
                onEditPhoto = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats (Todos en 0 como pedido)
            StatsRow()

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            SectionTabs()

            Spacer(modifier = Modifier.height(16.dp))

            // Grid
            PhotoGrid()
        }
    }
}

@Composable
fun TopBar(onBackClick: () -> Unit, onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        Text(
            text = "Mi Perfil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        // Menu Hamburguesa
        Box {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar Sesión") },
                    onClick = {
                        showMenu = false
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileInfo(usuario: Usuario?, currentPhoto: Uri?, onEditPhoto: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { onEditPhoto() }
        ) {
            if (currentPhoto != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentPhoto)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Profile Picture Placeholder",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .padding(20.dp) // Padding si es el icono default
                )
            }
            
            // Botón de editar (cámara)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722))
                    .padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore),
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre del Usuario (desde BD)
        Text(
            text = usuario?.nombre ?: usuario?.username ?: "Cargando...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Email del usuario
        if (usuario != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = usuario.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Location",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Ubicación hardcoded por ahora (o podrías pedirla al registrar)
            Text(
                text = "Perú",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(count = "0", label = "Seguidores")
        StatItem(count = "0", label = "Siguiendo")
        StatItem(count = "0", label = "Viajes")
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SectionTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 24.dp)
        ) {
            Text(
                text = "Fotos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 3.dp)
                    .background(Color(0xFFFF5722), RoundedCornerShape(2.dp))
            )
        }
        // Puedes agregar más tabs aquí si quieres
    }
}

@Composable
fun PhotoGrid() {
    // Aquí se deberían mostrar las fotos del usuario desde la BD
    // Por ahora dejamos el placeholder o vacío si prefieres
    
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Aún no has subido fotos.", color = Color.Gray)
    }
}

@Composable
fun BottomNavigationBar() {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D3E50)) // Dark Navy
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Inicio -> MapaActivity
        Box(modifier = Modifier.clickable { 
            val intent = Intent(context, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }) {
            BottomNavItem(icon = Icons.Default.Home, label = "Inicio", selected = false)
        }

        // Explorar -> Placeholder
        Box(modifier = Modifier.clickable { 
            Toast.makeText(context, "Función Explorar próximamente", Toast.LENGTH_SHORT).show()
        }) {
             BottomNavItem(icon = Icons.Default.Search, label = "Explorar", selected = false)
        }

        // Notific. -> NotificationsActivity
        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, NotificationsActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItem(icon = Icons.Default.Notifications, label = "Notific.", selected = false)
        }

        // Perfil -> Stay here (Selected)
        BottomNavItem(icon = Icons.Default.Person, label = "Perfil", selected = true)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean) {
    val tintColor = if (selected) Color(0xFFFFC107) else Color.White // Yellow/Orange for selected

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = tintColor
        )
    }
}