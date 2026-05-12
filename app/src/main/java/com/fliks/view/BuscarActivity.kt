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
import androidx.compose.ui.res.stringResource
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
        //guardo el email para que el botton bar lo mantenga al cambiar de pestaña
        val userEmail = intent.getStringExtra("USUARIO_EMAIL") ?: getString(R.string.usuario_default)
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
                                    putExtra("PELI_GENEROS", obtenerNombreGeneros(pelicula.genreIds).ifEmpty { getString(R.string.genero_desconocido) })
                                    val duracion = pelicula.runtime?.let { if (it > 0) "${it / 60}h ${it % 60}m" else getString(R.string.no_disponible) } ?: getString(R.string.no_disponible)
                                    putExtra("PELI_DURACION", duracion)
                                    putExtra("PELI_CLASIFICACION", pelicula.certification ?: getString(R.string.no_disponible))
                                }
                                startActivity(intent)
                            },
                            onGuardarClick = { peli ->
                                watchLaterViewModel.agregarAVerMasTarde(
                                    movieId = peli.id,
                                    title = peli.title,
                                    posterPath = peli.posterPath ?: ""
                                )
                                Toast.makeText(this@BuscarActivity, getString(R.string.toast_anadida_ver_mas_tarde), Toast.LENGTH_SHORT).show()
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
    //guardo lo que el usuario está tecleando
    var query by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.buscar_pelicula_placeholder), color = Color.Gray) },
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
        //lanza la búsqueda en el viewmodel al pulsar
        Button(
            onClick = { viewModel.buscarPelicula(query) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text(stringResource(R.string.buscar_boton), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (viewModel.estaBuscando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF007BFF))
            }
        } else if (viewModel.errorBusqueda != null) {
            //muestra el error si lo hay
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = viewModel.errorBusqueda!!, color = Color.Red, fontSize = 16.sp)
            }
        } else {
            //lista con las pelis encontradas
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