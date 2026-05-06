package com.fliks.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        val userEmail = intent.getStringExtra("USUARIO_EMAIL") ?: getString(R.string.usuario_default)

        setContent {
            FliksTheme {
                Scaffold(
                    bottomBar = { FliksNavigationBar(currentTab = 2, context = this, email = userEmail) }
                ) { paddingValues ->
                    PantallaPerfil(
                        email = userEmail,
                        authViewModel = authViewModel,
                        watchLaterViewModel = watchLaterViewModel,
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

    override fun onResume() {
        super.onResume()
        watchLaterViewModel.obtenerLista()
        authViewModel.cargarAvatar()
    }
}

@Composable
fun PantallaPerfil(
    email: String,
    authViewModel: AuthViewModel,
    watchLaterViewModel: WatchLaterViewModel,
    onCerrarSesion: () -> Unit,
    onMovieClick: (WatchLaterMovie) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            authViewModel.subirAvatar(context, uri)
        }
    }
    LaunchedEffect(authViewModel.mensajeError) {
        authViewModel.mensajeError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            authViewModel.limpiarError()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .clickable {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
        ) {
            if (authViewModel.cargando) {
                CircularProgressIndicator(color = Color(0xFF007BFF))
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color(0xFF001B33).copy(alpha = 0.6f),
                    border = BorderStroke(2.dp, Color(0xFF007BFF))
                ) {
                    if (authViewModel.avatarUrl != null) {
                        AsyncImage(
                            model = authViewModel.avatarUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.person),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Toca para cambiar foto", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = email,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.ver_mas_tarde),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (watchLaterViewModel.listaVerMasTarde.isEmpty()) {
            Text(
                text = stringResource(R.string.no_peliculas_pendientes),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(watchLaterViewModel.listaVerMasTarde) { peli ->
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
                            val contextLocal = LocalContext.current
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
                                onClick = { watchLaterViewModel.marcarComoVisto(peli.movieId, peli.title, peli.posterPath) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(Color(0xFF007BFF).copy(alpha = 0.9f), CircleShape)
                            ) {
                                Icon(
                                    painter=painterResource(R.drawable.add),
                                    contentDescription = stringResource(R.string.marcar_visto_desc),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.peliculas_vistas),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (watchLaterViewModel.listaVistas.isEmpty()) {
            Text(
                text = stringResource(R.string.no_peliculas_vistas),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(watchLaterViewModel.listaVistas) { peli ->
                    Card(
                        modifier = Modifier
                            .size(120.dp, 180.dp)
                            .clickable { onMovieClick(peli) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF001B33).copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, Color(0xFF326691).copy(alpha = 0.3f))
                    ) {
                        val esImagenLocal = peli.posterPath.isNotEmpty() && !peli.posterPath.startsWith("/")
                        val contextLocal = LocalContext.current
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
        ) {
            Text(stringResource(R.string.cerrar_sesion), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}