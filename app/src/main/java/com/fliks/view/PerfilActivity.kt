package com.fliks.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fliks.R
import com.fliks.data.TMDBClient
import com.fliks.model.WatchLaterMovie
import com.fliks.ui.theme.FliksTheme
import com.fliks.viewmodel.AuthViewModel
import com.fliks.viewmodel.WatchLaterViewModel

class PerfilActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val watchLaterViewModel: WatchLaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userEmail = intent.getStringExtra("USUARIO_EMAIL") ?: "Usuario"

        setContent {
            FliksTheme {
                Scaffold(
                    bottomBar = { FliksNavigationBar(currentTab = 2, context = this, email = userEmail) }
                ) { paddingValues ->
                    PantallaPerfil(
                        email = userEmail,
                        viewModel = watchLaterViewModel,
                        onCerrarSesion = {
                            authViewModel.cerrarSesion()
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }, 300)
                        },
                        onMovieClick = { peli ->
                            val intent = Intent(this@PerfilActivity, DetalleActivity::class.java).apply {
                                putExtra("PELI_ID", peli.movieId)
                                putExtra("PELI_TITULO", peli.title)
                                putExtra("PELI_POSTER", peli.posterPath)
                            }
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Brush.verticalGradient(listOf(Color(0xFF002B4D), Color(0xFF001220))))
                    )
                }
            }
        }
    }

    // SOBREESCRIBIMOS ESTO PARA ACTUALIZAR SIEMPRE QUE SE ENTRE A LA PANTALLA
    override fun onResume() {
        super.onResume()
        watchLaterViewModel.obtenerLista()
    }
}

@Composable
fun PantallaPerfil(
    email: String,
    viewModel: WatchLaterViewModel,
    onCerrarSesion: () -> Unit,
    onMovieClick: (WatchLaterMovie) -> Unit,
    modifier: Modifier = Modifier
) {
    // Hemos eliminado el LaunchedEffect(Unit) de aquí porque ahora se encarga onResume

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color(0xFF001B33).copy(alpha = 0.6f),
            border = BorderStroke(2.dp, Color(0xFF007BFF))
        ) {
            Icon(
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = email,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Ver más tarde",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.listaVerMasTarde.isEmpty()) {
            Text(
                text = "No tienes películas pendientes.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.listaVerMasTarde) { peli ->
                    Card(
                        modifier = Modifier
                            .size(120.dp, 180.dp)
                            .clickable { onMovieClick(peli) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF001B33).copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, Color(0xFF326691).copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {

                            val esImagenLocal = peli.posterPath.isNotEmpty() && !peli.posterPath.startsWith("/")
                            val contextLocal = androidx.compose.ui.platform.LocalContext.current
                            val modeloImagen = if (esImagenLocal) {
                                contextLocal.resources.getIdentifier(peli.posterPath, "drawable", contextLocal.packageName)
                            } else {
                                "${TMDBClient.IMAGE_BASE_URL}${peli.posterPath}"
                            }

                            AsyncImage(
                                model = modeloImagen,
                                contentDescription = peli.title,
                                error = painterResource(R.drawable.logosvg),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { viewModel.marcarComoVisto(peli.movieId, peli.title, peli.posterPath) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(Color(0xFF007BFF).copy(alpha = 0.9f), CircleShape)
                            ) {
                                Icon(
                                    painter=painterResource(R.drawable.add),
                                    contentDescription = "Marcar como visto",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Películas Vistas",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.listaVistas.isEmpty()) {
            Text(
                text = "No has marcado películas como vistas.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.listaVistas) { peli ->
                    Card(
                        modifier = Modifier
                            .size(120.dp, 180.dp)
                            .clickable { onMovieClick(peli) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF001B33).copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, Color(0xFF326691).copy(alpha = 0.3f))
                    ) {
                        val esImagenLocal = peli.posterPath.isNotEmpty() && !peli.posterPath.startsWith("/")
                        val contextLocal = androidx.compose.ui.platform.LocalContext.current
                        val modeloImagen = if (esImagenLocal) {
                            contextLocal.resources.getIdentifier(peli.posterPath, "drawable", contextLocal.packageName)
                        } else {
                            "${TMDBClient.IMAGE_BASE_URL}${peli.posterPath}"
                        }

                        AsyncImage(
                            model = modeloImagen,
                            contentDescription = peli.title,
                            error = painterResource(R.drawable.logosvg),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
        ) {
            Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}