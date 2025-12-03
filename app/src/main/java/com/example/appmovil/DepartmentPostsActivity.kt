package com.example.appmovil

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.data.Foto
import com.example.appmovil.data.Notificacion
import com.example.appmovil.data.Publicacion
import com.example.appmovil.data.PublicacionLike
import com.example.appmovil.utils.SessionManager
import kotlinx.coroutines.launch

class DepartmentPostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val deptName = intent.getStringExtra("DEPT_NAME") ?: "Departamento"

        setContent {
            DepartmentPostsScreen(
                deptName = deptName,
                onBackClick = { finish() }
            )
        }
    }
}

data class PostWithPhoto(
    val publicacion: Publicacion,
    val foto: Foto?,
    var isLiked: Boolean = false, // For visitor
    var likesCount: Int = 0       // For owner/visitor
)

@Composable
fun DepartmentPostsScreen(deptName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val activity = context as? ComponentActivity
    val intentTargetId = activity?.intent?.getIntExtra("TARGET_USER_ID", -1) ?: -1
    
    var posts by remember { mutableStateOf<List<PostWithPhoto>>(emptyList()) }
    var currentCoverPhoto by remember { mutableStateOf<Foto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Visitor Logic
    var currentUserId by remember { mutableStateOf(-1) }
    var isMyMap by remember { mutableStateOf(true) }
    var targetUserId by remember { mutableStateOf(-1) }

    // Dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<PostWithPhoto?>(null) }

    // Function to refresh data
    fun refreshData() {
        scope.launch {
            if (targetUserId != -1) {
                val dept = db.appDao().getDepartamentoByName(deptName)
                if (dept != null) {
                    // 1. Get all posts
                    val pubs = db.appDao().getPublicacionesByUserAndDept(targetUserId, dept.id_departamento)
                    val resultList = mutableListOf<PostWithPhoto>()
                    
                    for (pub in pubs) {
                        val fotos = db.appDao().getFotosByPublicacion(pub.id_publicacion)
                        // Get Likes Info
                        val count = db.appDao().countLikesForPublicacion(pub.id_publicacion)
                        val liked = if (currentUserId != -1) {
                            db.appDao().hasUserLikedPublicacion(currentUserId, pub.id_publicacion) > 0
                        } else false
                        
                        resultList.add(PostWithPhoto(pub, fotos.firstOrNull(), liked, count))
                    }
                    posts = resultList
                    
                    // 2. Get current cover photo
                    currentCoverPhoto = db.appDao().getFotoPrincipalMapaObject(targetUserId, deptName)
                }
            }
            isLoading = false
        }
    }

    // Initial Load
    LaunchedEffect(deptName) {
        currentUserId = SessionManager.getUserId(context)
        if (intentTargetId != -1 && intentTargetId != currentUserId) {
             targetUserId = intentTargetId
             isMyMap = false
        } else {
             targetUserId = currentUserId
             isMyMap = true
        }
        refreshData()
    }

    // Set as Cover Function (Only if my map)
    fun setAsCover(fotoId: Int) {
        if (!isMyMap) return
        scope.launch {
            if (targetUserId != -1) {
                db.appDao().clearPrincipalMapaForDept(targetUserId, deptName)
                db.appDao().setPrincipalMapa(fotoId)
                refreshData()
                Toast.makeText(context, "Portada actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Toggle Like Function (Visitor)
    fun toggleLike(item: PostWithPhoto) {
        scope.launch {
            if (item.isLiked) {
                db.appDao().deletePublicacionLike(currentUserId, item.publicacion.id_publicacion)
            } else {
                db.appDao().insertPublicacionLike(PublicacionLike(currentUserId, item.publicacion.id_publicacion))
                // Optional: Notification for owner
                if (!isMyMap) {
                    db.appDao().insertNotificacion(
                        Notificacion(
                            fk_usuario_destino = targetUserId,
                            tipo = "LIKE_FOTO",
                            mensaje = "Le gustó una foto de tu viaje a $deptName",
                            leido = false
                        )
                    )
                }
            }
            refreshData() // Update UI
        }
    }

    // Delete Logic
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar foto?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            postToDelete?.let { item ->
                                try {
                                    db.appDao().deletePublicacion(item.publicacion)
                                    refreshData() 
                                    Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showDeleteDialog = false
                            postToDelete = null
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D3E50))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = deptName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No hay fotos guardadas.", color = Color.Gray)
                }
            }
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header: Current Cover Photo (Only show title if exists)
                    item {
                        if (currentCoverPhoto != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Light Orange
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Portada Actual del Mapa",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFFF9800)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(currentCoverPhoto!!.fotoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Portada",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }

                    // List of Posts
                    items(posts) { item ->
                        val isCover = currentCoverPhoto?.id_foto == item.foto?.id_foto
                        PostItem(
                            item = item,
                            isCover = isCover,
                            isEditable = isMyMap,
                            onDeleteClick = {
                                postToDelete = item
                                showDeleteDialog = true
                            },
                            onSetCoverClick = {
                                item.foto?.let { setAsCover(it.id_foto) }
                            },
                            onLikeClick = { toggleLike(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    item: PostWithPhoto, 
    isCover: Boolean,
    isEditable: Boolean,
    onDeleteClick: () -> Unit,
    onSetCoverClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image Section
            Box {
                if (item.foto != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.foto.fotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.publicacion.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin imagen", color = Color.White)
                    }
                }
            }
            
            // Info & Action Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title
                    Text(
                        text = item.publicacion.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // LIKE BUTTON (For everyone)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLikeClick) {
                            Icon(
                                imageVector = if (item.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (item.isLiked) Color(0xFFE91E63) else Color.Gray // Pink vs Gray
                            )
                        }
                        if (item.likesCount > 0) {
                            Text(
                                text = item.likesCount.toString(),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (!item.publicacion.reseña.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.publicacion.reseña,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Edit Actions (Only if editable)
                if (isEditable) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Star / Cover Button
                        IconButton(onClick = onSetCoverClick) {
                            Icon(
                                imageVector = if (isCover) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Establecer Portada",
                                tint = if (isCover) Color(0xFFFFC107) else Color.Gray
                            )
                        }

                        // Delete Button
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFE57373)
                            )
                        }
                    }
                }
            }
        }
    }
}