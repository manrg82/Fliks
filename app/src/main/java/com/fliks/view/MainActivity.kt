package com.fliks.view

import android.content.Intent
import com.fliks.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fliks.data.TMDBClient
import com.fliks.model.PeliculaTMDB
import com.fliks.ui.theme.FliksTheme
import com.fliks.viewmodel.MoviesViewModel

class MainActivity : ComponentActivity() {
    private val moviesViewModel: MoviesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("USUARIO_EMAIL") ?: "Usuario"

        setContent {
            FliksTheme {
                MainScreen(
                    email = userEmail,
                    moviesViewModel = moviesViewModel,
                    onMovieClick = { pelicula ->
                        val intent = Intent(this@MainActivity, DetalleActivity::class.java).apply {
                            putExtra("PELI_TITULO", pelicula.title)
                            putExtra("PELI_SINOPSIS", pelicula.overview)
                        }
                        startActivity(intent)
                    },
                    onProfileClick = {
                        val intent = Intent(this@MainActivity, PerfilActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    email: String,
    moviesViewModel: MoviesViewModel,
    onMovieClick: (PeliculaTMDB) -> Unit,
    onProfileClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val azulFondo = Color(0xFF001220)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF001220).copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = Color(0xFF326691).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.home),
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio", fontSize = 12.sp) }, // Vuelto a 12.sp directo
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007BFF),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF007BFF),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Buscar"
                        )
                    },
                    label = { Text("Buscar", fontSize = 12.sp) }, // Vuelto a 12.sp directo
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007BFF),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF007BFF),
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onProfileClick() },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.person),
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil", fontSize = 12.sp) }, // Vuelto a 12.sp directo
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(Color(0xFF002B4D), azulFondo)))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Hola, ${email.split("@")[0]}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Tendencias",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(moviesViewModel.listaPeliculas) { peli ->
                        CardPeliculaTMDB(peli) { onMovieClick(peli) }
                    }
                }
            }
        }
    }
}

@Composable
fun CardPeliculaTMDB(pelicula: PeliculaTMDB, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 140.dp, height = 200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        AsyncImage(
            model = "${TMDBClient.IMAGE_BASE_URL}${pelicula.posterPath}",
            contentDescription = pelicula.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}