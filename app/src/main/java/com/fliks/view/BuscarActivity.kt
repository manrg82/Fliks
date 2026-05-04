package com.fliks.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliks.R
import com.fliks.model.PeliculaTMDB
import com.fliks.ui.theme.FliksTheme
import com.fliks.viewmodel.MoviesViewModel
import com.fliks.viewmodel.WatchLaterViewModel

class BuscarActivity : ComponentActivity() {
    private val moviesViewModel: MoviesViewModel by viewModels()
    private val watchLaterViewModel: WatchLaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("USUARIO_EMAIL") ?: "Usuario"

        setContent {
            FliksTheme {
                Scaffold(
                    bottomBar = { FliksNavigationBar(currentTab = 1, context = this, email = userEmail) }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Brush.verticalGradient(listOf(Color(0xFF002B4D), Color(0xFF001220))))
                    ) {
                        PantallaBusqueda(
                            viewModel = moviesViewModel,
                            onMovieClick = { pelicula ->
                                val intent = Intent(this@BuscarActivity, DetalleActivity::class.java).apply {
                                    putExtra("PELI_ID", pelicula.id)
                                    putExtra("PELI_TITULO", pelicula.title)
                                    putExtra("PELI_SINOPSIS", pelicula.overview)
                                    putExtra("PELI_POSTER", pelicula.posterPath)
                                    putExtra("PELI_NOTA", (pelicula.voteAverage * 10).toInt())
                                    putExtra("PELI_GENEROS", obtenerNombreGeneros(pelicula.genreIds).ifEmpty { "Desconocido" })
                                    val duracion = pelicula.runtime?.let { if (it > 0) "${it / 60}h ${it % 60}m" else "N/D" } ?: "N/D"
                                    putExtra("PELI_DURACION", duracion)
                                    putExtra("PELI_CLASIFICACION", pelicula.certification ?: "N/D")
                                }
                                startActivity(intent)
                            },
                            onGuardarClick = { peli ->
                                watchLaterViewModel.agregarAVerMasTarde(
                                    movieId = peli.id,
                                    title = peli.title,
                                    posterPath = peli.posterPath ?: ""
                                )
                                Toast.makeText(this@BuscarActivity, "Añadida a Ver más tarde", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaBusqueda(viewModel: MoviesViewModel, onMovieClick: (PeliculaTMDB) -> Unit, onGuardarClick: (PeliculaTMDB) -> Unit) {
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar película...", color = Color.Gray) },
            leadingIcon = { Icon(painter=painterResource(R.drawable.search), contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007BFF),
                unfocusedBorderColor = Color(0xFF326691).copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF007BFF)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.buscarPelicula(query) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text("Buscar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.estaBuscando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF007BFF))
            }
        } else if (viewModel.errorBusqueda != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = viewModel.errorBusqueda!!, color = Color.Red, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.resultadosBusqueda) { peli ->
                    CardPeliculaTMDB(
                        pelicula = peli,
                        onGuardarClick = { onGuardarClick(peli) },
                        onClick = { onMovieClick(peli) }
                    )
                }
            }
        }
    }
}