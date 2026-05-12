package com.fliks.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.fliks.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fliks.data.TMDBClient
import com.fliks.ui.theme.FliksTheme
import com.fliks.viewmodel.WatchLaterViewModel

class DetalleActivity : ComponentActivity() {
    private val watchLaterViewModel: WatchLaterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //recoge todo lo que nos han mandado por el intent
        val id = intent.getIntExtra("PELI_ID", 0)
        val titulo = intent.getStringExtra("PELI_TITULO") ?: getString(R.string.genero_desconocido)
        val sinopsis = intent.getStringExtra("PELI_SINOPSIS") ?: getString(R.string.sin_sinopsis)
        val poster = intent.getStringExtra("PELI_POSTER") ?: ""
        val nota = intent.getIntExtra("PELI_NOTA", 0)
        val generos = intent.getStringExtra("PELI_GENEROS") ?: ""
        val duracion = intent.getStringExtra("PELI_DURACION") ?: ""
        val clasificacion = intent.getStringExtra("PELI_CLASIFICACION") ?: ""
        setContent {
            FliksTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    PantallaDetalle(
                        id = id,
                        titulo = titulo,
                        sinopsis = sinopsis,
                        posterPath = poster,
                        nota = nota,
                        generos = generos,
                        duracion = duracion,
                        clasificacion = clasificacion,
                        viewModel = watchLaterViewModel,
                        onVolver = { finish() },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF002B4D), Color(0xFF001220))))
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun PantallaDetalle(
    id: Int,
    titulo: String,
    sinopsis: String,
    posterPath: String,
    nota: Int,
    generos: String,
    duracion: String,
    clasificacion: String,
    viewModel: WatchLaterViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    //chequea en Supabase si ya teníamos esta peli guardada en alguna lista de antes
    LaunchedEffect(id) {
        viewModel.limpiarEstadoPelicula()
        viewModel.verificarEstadoPelicula(id)
    }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            //poster de la peli
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val esImagenLocal = posterPath.isNotEmpty() && !posterPath.startsWith("/")
                val contextLocal = androidx.compose.ui.platform.LocalContext.current
                val modeloImagen = if (esImagenLocal) {
                    contextLocal.resources.getIdentifier(posterPath, "drawable", contextLocal.packageName)
                } else {
                    "${TMDBClient.IMAGE_BASE_URL}$posterPath"
                }
                AsyncImage(
                    model = modeloImagen,
                    contentDescription = titulo,
                    error = painterResource(R.drawable.logosvg),
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = generos,
                    color = Color.Gray,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$nota%",
                        color = Color(0xFF007BFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { nota / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF007BFF),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipInfoDetalle(text = duracion)
                    ChipInfoDetalle(text = clasificacion)
                }
                Spacer(modifier = Modifier.height(24.dp))

                //logica de los botones de "ver más tarde" y "marcar como visto"
                if (viewModel.esVisto) {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF001220).copy(alpha = 0.8f),
                            disabledContentColor = Color.Gray
                        ),
                        border = BorderStroke(1.dp, Color(0xFF326691).copy(alpha = 0.3f))
                    ) {
                        Text(stringResource(R.string.marcada_como_vista), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.agregarAVerMasTarde(id, titulo, posterPath) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.esVerMasTarde) Color(0xFF007BFF).copy(alpha = 0.3f) else Color(0xFF001B33).copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF007BFF))
                        ) {
                            Text(if (viewModel.esVerMasTarde) stringResource(R.string.guardado) else stringResource(R.string.ver_mas_tarde), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.marcarComoVisto(id, titulo, posterPath) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                        ) {
                            Text(stringResource(R.string.visto), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF001B33).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF326691).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.sinopsis),
                            color = Color(0xFF007BFF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = sinopsis,
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
        //boton de volver
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = stringResource(R.string.volver_desc),
                tint = Color.White
            )
        }
    }
}
@Composable
fun ChipInfoDetalle(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF000A14), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}