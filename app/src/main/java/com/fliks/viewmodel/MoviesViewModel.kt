package com.fliks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliks.data.TMDBClient
import com.fliks.model.DetallePeliculaTMDB
import com.fliks.model.PeliculaTMDB
import com.fliks.model.RespuestaTMDB
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {
    var listaPeliculas by mutableStateOf<List<PeliculaTMDB>>(emptyList())
        private set
    init {
        obtenerPeliculasPopulares()
    }
    private fun obtenerPeliculasPopulares() {
        viewModelScope.launch {
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

                listaPeliculas = peliculasConDetalles
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}