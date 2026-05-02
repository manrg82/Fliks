package com.fliks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliks.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val supabase = SupabaseClient.client
    var cargando by mutableStateOf(false)
        private set
    var exitoLogin by mutableStateOf(false)
        private set
    var mensajeError by mutableStateOf<String?>(null)
        private set
    init {
        comprobarSesionActual()
    }

    private fun comprobarSesionActual() {
        val sesion = supabase.auth.currentSessionOrNull()
        if (sesion != null) {
            exitoLogin = true
        }
    }
    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "No puedes dejar campos vacíos"
            return
        }
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                supabase.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }
                exitoLogin = true
            } catch (e: Exception) {
                mensajeError = cuandoFallaElAuth(e)
            } finally {
                cargando = false
            }
        }
    }
    fun registrarCuenta(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.length < 6) {
            mensajeError = "El correo es obligatorio y la clave debe tener 6+ letras"
            return
        }

        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                supabase.auth.signUpWith(Email) {
                    email = correo
                    password = contrasena
                }
                exitoLogin = true
            } catch (e: Exception) {
                mensajeError = "Error al crear cuenta: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }
    fun limpiarError() {
        mensajeError = null
    }
    private fun cuandoFallaElAuth(e: Exception): String {
        return when {
            e.message?.contains("Invalid login credentials", true) == true -> "Correo o contraseña incorrectos"
            e.message?.contains("network", true) == true -> "Sin conexión a internet"
            else -> e.localizedMessage ?: "Ha ocurrido un error inesperado"
        }
    }
}