package com.fliks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliks.data.SupabaseClient
import com.fliks.model.WatchLaterMovie
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class WatchLaterViewModel : ViewModel() {

    private val supabase = SupabaseClient.client

    var listaVerMasTarde by mutableStateOf<List<WatchLaterMovie>>(emptyList())
        private set

    var listaVistas by mutableStateOf<List<WatchLaterMovie>>(emptyList())
        private set

    var esVisto by mutableStateOf(false)
        private set

    var esVerMasTarde by mutableStateOf(false)
        private set

    fun obtenerLista() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val todas = supabase.postgrest["watch_later"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<WatchLaterMovie>()

                listaVerMasTarde = todas.filter { !it.isSeen }
                listaVistas = todas.filter { it.isSeen }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun verificarEstadoPelicula(movieId: Int) {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val movie = supabase.postgrest["watch_later"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("movie_id", movieId)
                        }
                    }.decodeList<WatchLaterMovie>().firstOrNull()

                if (movie != null) {
                    esVisto = movie.isSeen
                    esVerMasTarde = !movie.isSeen
                } else {
                    esVisto = false
                    esVerMasTarde = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarAVerMasTarde(movieId: Int, title: String, posterPath: String) {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val movie = WatchLaterMovie(userId, movieId, title, posterPath, false)
                supabase.postgrest["watch_later"].upsert(movie)
                esVerMasTarde = true
                esVisto = false
                obtenerLista()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marcarComoVisto(movieId: Int, title: String, posterPath: String) {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                val movie = WatchLaterMovie(userId, movieId, title, posterPath, true)
                supabase.postgrest["watch_later"].upsert(movie)
                esVisto = true
                esVerMasTarde = false
                obtenerLista()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun limpiarEstadoPelicula() {
        esVisto = false
        esVerMasTarde = false
    }
}