package com.fliks.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fliks.R
import com.fliks.data.TMDBClient
import com.fliks.model.PeliculaTMDB
import com.fliks.ui.theme.FliksTheme
import com.fliks.ui.theme.azulBorde
import com.fliks.ui.theme.azulBrillante
import com.fliks.ui.theme.fondoTarjeta
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
                    icon = { Icon(painter = painterResource(R.drawable.home), contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 12.sp) },
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
                    icon = { Icon(painter = painterResource(R.drawable.search), contentDescription = "Buscar") },
                    label = { Text("Buscar", fontSize = 12.sp) },
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
                    icon = { Icon(painter = painterResource(R.drawable.person), contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 12.sp) },
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
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
    val azulBrillante = Color(0xFF007BFF)
    val fondoTarjeta = Color(0xFF001B33).copy(alpha = 0.6f)
    val azulBorde = Color(0xFF326691).copy(alpha = 0.3f)

    val porcentaje = (pelicula.voteAverage * 10).toInt()
    val progreso = (pelicula.voteAverage / 10).toFloat()
    val generosTexto = obtenerNombreGeneros(pelicula.genreIds).ifEmpty { "Desconocido" }
    val clasificacion = pelicula.certification ?: "N/D"

    val duracionFormateada = pelicula.runtime?.let {
        if (it > 0) "${it / 60}h ${it % 60}m" else "N/D"
    } ?: "N/D"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondoTarjeta),
        border = BorderStroke(0.5.dp, azulBorde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 170.dp)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = "${TMDBClient.IMAGE_BASE_URL}${pelicula.posterPath}",
                contentDescription = pelicula.title,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = pelicula.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add1),
                            contentDescription = "Guardar",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = generosTexto,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$porcentaje%",
                        color = azulBrillante,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = azulBrillante,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipInfo(text = duracionFormateada)
                    ChipInfo(text = clasificacion)
                }
            }
        }
    }
}
@Composable
fun ChipInfo(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF000A14), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
fun obtenerNombreGeneros(ids: List<Int>): String {
    val mapaGeneros = mapOf(
        28 to "Acción", 12 to "Aventura", 16 to "Animación", 35 to "Comedia",
        80 to "Crimen", 99 to "Documental", 18 to "Drama", 10751 to "Familiar",
        14 to "Fantasía", 36 to "Historia", 27 to "Terror", 10402 to "Música",
        9648 to "Misterio", 10749 to "Romance", 878 to "Ciencia Ficción",
        10770 to "TV", 53 to "Suspense", 10752 to "Bélica", 37 to "Western"
    )
    return ids.mapNotNull { mapaGeneros[it] }.take(2).joinToString(" / ")
}