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

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.lazy.items
import java.io.File
import java.io.FileInputStream
import com.example.appmovil.data.Publicacion
import com.example.appmovil.data.Foto

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

data class PostWithDetails(
    val publicacion: Publicacion,
    val fotoUrl: String?,
    val likeCount: Int,
    val deptName: String
)

private fun downloadMap(context: android.content.Context, userId: Int) {
    val filename = "map_snapshot_$userId.png"
    val file = File(context.filesDir, filename)
    if (!file.exists()) {
        Toast.makeText(context, "Primero visita tu mapa para generarlo", Toast.LENGTH_SHORT).show()
        return
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "MiMapaPeru_$userId.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AppPeru")
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
        try {
            resolver.openOutputStream(uri).use { out ->
                FileInputStream(file).use { inp ->
                    inp.copyTo(out!!)
                }
            }
            Toast.makeText(context, "Mapa guardado en Galería", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
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
    var userPosts by remember { mutableStateOf<List<PostWithDetails>>(emptyList()) }
    
    // Stats
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var likesCount by remember { mutableStateOf(0) }
    
    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId(context)
        if (userId != -1) {
            usuario = db.appDao().getUsuarioById(userId)
            usuario?.fotoPerfilUrl?.let { 
                photoUri = Uri.parse(it)
            }
            
            // Cargar estadísticas reales
            followersCount = db.appDao().countFollowers(userId)
            followingCount = db.appDao().countFollowing(userId)
            likesCount = db.appDao().countLikesForUser(userId)

            // Cargar Posts
            val posts = db.appDao().getPublicacionesByUser(userId)
            val detailedPosts = posts.map { post ->
                val photos = db.appDao().getFotosByPublicacion(post.id_publicacion)
                val firstPhoto = photos.firstOrNull()
                val pLikes = db.appDao().countLikesForPublicacion(post.id_publicacion)
                val dept = db.appDao().getDepartamentoById(post.fk_departamento)
                PostWithDetails(post, firstPhoto?.fotoUrl, pLikes, dept?.nombre ?: "Desconocido")
            }
            userPosts = detailedPosts
        }
    }

    // Selector de imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            val userId = SessionManager.getUserId(context)
            if (userId != -1) {
                scope.launch {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) { }
                    
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
            TopBar(onBackClick, onLogout)
            Spacer(modifier = Modifier.height(20.dp))

            ProfileInfo(
                usuario = usuario, 
                currentPhoto = photoUri,
                onEditPhoto = { imagePickerLauncher.launch("image/*") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { 
                    val userId = SessionManager.getUserId(context)
                    if (userId != -1) downloadMap(context, userId)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3E50))
            ) {
                Text("Descargar Mapa", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatsRow(followersCount, followingCount, likesCount)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTabs()
            Spacer(modifier = Modifier.height(16.dp))
            PhotoGrid(userPosts)
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
                        .padding(20.dp)
                )
            }
            
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

        Text(
            text = usuario?.nombre ?: usuario?.username ?: "Cargando...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

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
            Text(
                text = "Perú",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatsRow(followers: Int, following: Int, likes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(count = followers.toString(), label = "Seguidores")
        StatItem(count = following.toString(), label = "Siguiendo")
        // Reemplazamos "Viajes" con "Likes" o mostramos Viajes si prefieres, pero likes es social
        StatItem(count = likes.toString(), label = "Likes") 
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
    }
}

@Composable
fun PhotoGrid(posts: List<PostWithDetails>) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No tienes fotos aún.", color = Color.Gray)
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts) { item ->
                PostItemCard(item)
            }
        }
    }
}

@Composable
fun PostItemCard(item: PostWithDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (item.fotoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(item.fotoUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.publicacion.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.deptName,
                    fontSize = 12.sp,
                    color = Color(0xFFFF5722),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val comentario = item.publicacion.reseña
                if (!comentario.isNullOrBlank()) {
                    Text(
                        text = comentario,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite_filled),
                        contentDescription = "Likes",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.likeCount} Likes",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D3E50))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(modifier = Modifier.clickable { 
            val intent = Intent(context, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)
        }) {
            BottomNavItem(icon = Icons.Default.Home, label = "Inicio", selected = false)
        }

        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, ExplorarActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItem(icon = Icons.Default.Search, label = "Explorar", selected = false)
        }

        Box(modifier = Modifier.clickable { 
             val intent = Intent(context, NotificationsActivity::class.java)
             context.startActivity(intent)
        }) {
             BottomNavItem(icon = Icons.Default.Notifications, label = "Notific.", selected = false)
        }

        BottomNavItem(icon = Icons.Default.Person, label = "Perfil", selected = true)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean) {
    val tintColor = if (selected) Color(0xFFFFC107) else Color.White

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
