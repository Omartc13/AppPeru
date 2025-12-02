package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
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

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(onBackClick = { finish() })
        }
    }
}

@Composable
fun ProfileScreen(onBackClick: () -> Unit) {
    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Top Bar
            TopBar(onBackClick)

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Info
            ProfileInfo()

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
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
fun TopBar(onBackClick: () -> Unit) {
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
            text = "Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        IconButton(onClick = { /* Menu action */ }) {
            Icon(
                imageVector = Icons.Default.Menu, // Or dots vertical
                contentDescription = "Menu",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun ProfileInfo() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_person), // Placeholder if no specific image
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)

            )
            // Small badge icon (camera or edit)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722)) // Orange accent
                    .padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore), // Using explore as camera placeholder
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Esther Howard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

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
                text = "Santa Ana, Illinois",
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
        StatItem(count = "1k", label = "Followers")
        StatItem(count = "342", label = "Following")
        StatItem(count = "458", label = "Trips")
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
                text = "Post",
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
        Text(
            text = "Photos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
    }
}

@Composable
fun PhotoGrid() {
    // Sample data placeholders
    val photos = listOf(
        R.drawable.mask_cuzco,
        R.drawable.mask_lima,
        R.drawable.mask_arequipa,
        R.drawable.mask_loreto,
        R.drawable.mask_ancash,
        R.drawable.mask_piura
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(photos.size) { index ->
            Image(
                painter = painterResource(id = photos[index]),
                contentDescription = "Travel Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun BottomNavigationBar() {
    // Navy Blue Background #1F2937 (approx) or just Color.Black/DarkGray. 
    // User xml says "@color/navy_blue", assuming it exists or hardcoding.
    // I'll use a dark color to match the XML description.
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D3E50)) // Dark Navy
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val context = LocalContext.current
        // Cast context to Activity to call finish()
        val activity = context as? ComponentActivity

        Box(modifier = Modifier.clickable { activity?.finish() }) {
            BottomNavItem(icon = Icons.Default.Home, label = "Inicio", selected = false)
        }
        BottomNavItem(icon = Icons.Default.Search, label = "Explorar", selected = false)
        BottomNavItem(icon = Icons.Default.Notifications, label = "Notific.", selected = false)
        BottomNavItem(icon = Icons.Default.Person, label = "Perfil", selected = true)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean) {
    val color = if (selected) Color(0xFFFF5722) else Color.White // Active orange, inactive white
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White, // Keeping white as per XML description, or color if selected? 
                                // The XML had all white. But usually active is different. 
                                // I'll stick to White as per XML visual, or maybe highlight Profile.
                                // User said "like the image", usually implies highlight. 
                                // But to match "igualito" to the previous XML context, maybe all white?
                                // I will make Profile Orange to indicate selection.
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}
