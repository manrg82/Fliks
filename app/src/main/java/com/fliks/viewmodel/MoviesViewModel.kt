package com.fliks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliks.data.TMDBClient
import com.fliks.model.PeliculaTMDB
import com.fliks.model.RespuestaTMDB
import io.ktor.client.call.*
import io.ktor.client.request.*
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

                listaPeliculas = respuesta.results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}