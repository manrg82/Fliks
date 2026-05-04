package com.fliks.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fliks.data.DataStoreManager
import com.fliks.data.TMDBClient
import com.fliks.model.DetallePeliculaTMDB
import com.fliks.model.PeliculaTMDB
import com.fliks.model.RespuestaTMDB
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MoviesViewModel(application: Application) : AndroidViewModel(application) {

    var listaPeliculas by mutableStateOf<List<PeliculaTMDB>>(emptyList())
        private set

    var estaCargando by mutableStateOf(false)
        private set

    var resultadosBusqueda by mutableStateOf<List<PeliculaTMDB>>(emptyList())
        private set

    var errorBusqueda by mutableStateOf<String?>(null)
        private set

    var estaBuscando by mutableStateOf(false)
        private set

    private val peliculasOffline = listOf(
        PeliculaTMDB(
            id = 120,
            title = "El Señor de los Anillos: La Comunidad del Anillo",
            posterPath = "poster1",
            overview = "En la Tierra Media, el Señor Oscuro Sauron forjó los Grandes Anillos de Poder...",
            voteAverage = 8.8,
            genreIds = listOf(12, 14, 28),
            runtime = 178,
            certification = "+13"
        ),
        PeliculaTMDB(
            id = 121,
            title = "El Señor de los Anillos: Las Dos Torres",
            posterPath = "poster2",
            overview = "La Comunidad se ha dividido. Frodo y Sam continúan su viaje hacia Mordor para destruir el Anillo...",
            voteAverage = 8.7,
            genreIds = listOf(12, 14, 28),
            runtime = 179,
            certification = "+13"
        ),
        PeliculaTMDB(
            id = 122,
            title = "El Señor de los Anillos: El Retorno del Rey",
            posterPath = "poster3",
            overview = "Las fuerzas de Sauron han asediado Minas Tirith, la capital de Gondor, en su intento final de dominar el mundo...",
            voteAverage = 8.9,
            genreIds = listOf(12, 14, 28),
            runtime = 201,
            certification = "+13"
        )
    )

    init {
        cargarPeliculas(false)
    }

    fun cargarPeliculas(forzarActualizacion: Boolean) {
        viewModelScope.launch {
            estaCargando = true
            val context = getApplication<Application>().applicationContext

            if (!forzarActualizacion) {
                val jsonGuardado = DataStoreManager.obtenerPeliculas(context).firstOrNull()
                if (!jsonGuardado.isNullOrEmpty()) {
                    try {
                        val peliculasCacheadas = Json.decodeFromString<List<PeliculaTMDB>>(jsonGuardado)
                        listaPeliculas = peliculasOffline + peliculasCacheadas.filter { cacheada ->
                            peliculasOffline.none { offline -> offline.id == cacheada.id }
                        }
                        estaCargando = false
                        return@launch
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            try {
                val respuesta: RespuestaTMDB = TMDBClient.httpClient.get(
                    "${TMDBClient.BASE_URL}/movie/popular?api_key=${TMDBClient.API_KEY}&language=es-ES"
                ).body()

                val peliculasConDetalles = respuesta.results.map { peli ->
                    async {
                        try {
                            val detalle: DetallePeliculaTMDB = TMDBClient.httpClient.get(
                                "${TMDBClient.BASE_URL}/movie/${peli.id}?api_key=${TMDBClient.API_KEY}&language=es-ES&append_to_response=release_dates"
                            ).body()

                            val usRelease = detalle.releaseDates?.results?.find { it.countryCode == "US" }
                            val cert = usRelease?.releaseDates?.firstOrNull { it.certification?.isNotEmpty() == true }?.certification

                            peli.copy(runtime = detalle.runtime, certification = cert)
                        } catch (e: Exception) {
                            peli
                        }
                    }
                }.awaitAll()

                val jsonParaGuardar = Json.encodeToString(peliculasConDetalles)
                DataStoreManager.guardarPeliculas(context, jsonParaGuardar)

                listaPeliculas = peliculasOffline + peliculasConDetalles.filter { apiPeli ->
                    peliculasOffline.none { offline -> offline.id == apiPeli.id }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                listaPeliculas = peliculasOffline
            } finally {
                estaCargando = false
            }
        }
    }

    fun buscarPelicula(query: String) {
        if (query.isBlank()) {
            resultadosBusqueda = emptyList()
            errorBusqueda = null
            return
        }

        viewModelScope.launch {
            estaBuscando = true
            errorBusqueda = null

            val locales = listaPeliculas.filter { it.title.contains(query, ignoreCase = true) }

            if (locales.isNotEmpty()) {
                resultadosBusqueda = locales
                estaBuscando = false
            } else {
                try {
                    val respuesta: RespuestaTMDB = TMDBClient.httpClient.get(
                        "${TMDBClient.BASE_URL}/search/movie?api_key=${TMDBClient.API_KEY}&language=es-ES&query=$query"
                    ).body()

                    if (respuesta.results.isEmpty()) {
                        errorBusqueda = "No se encontraron películas"
                        resultadosBusqueda = emptyList()
                    } else {
                        val peliculasConDetalles = respuesta.results.take(10).map { peli ->
                            async {
                                try {
                                    val detalle: DetallePeliculaTMDB = TMDBClient.httpClient.get(
                                        "${TMDBClient.BASE_URL}/movie/${peli.id}?api_key=${TMDBClient.API_KEY}&language=es-ES&append_to_response=release_dates"
                                    ).body()

                                    val usRelease = detalle.releaseDates?.results?.find { it.countryCode == "US" }
                                    val cert = usRelease?.releaseDates?.firstOrNull { it.certification?.isNotEmpty() == true }?.certification

                                    peli.copy(runtime = detalle.runtime, certification = cert)
                                } catch (e: Exception) {
                                    peli
                                }
                            }
                        }.awaitAll()
                        resultadosBusqueda = peliculasConDetalles
                    }
                } catch (e: Exception) {
                    errorBusqueda = "Error al buscar en Internet"
                    resultadosBusqueda = emptyList()
                } finally {
                    estaBuscando = false
                }
            }
        }
    }
}