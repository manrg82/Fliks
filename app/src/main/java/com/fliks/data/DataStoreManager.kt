package com.fliks.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fliks_preferencias")

object DataStoreManager {
    private val PELICULAS_KEY = stringPreferencesKey("peliculas_guardadas")
    suspend fun guardarPeliculas(context: Context, jsonPeliculas: String) {
        context.dataStore.edit { preferences ->
            preferences[PELICULAS_KEY] = jsonPeliculas
        }
    }
    fun obtenerPeliculas(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PELICULAS_KEY]
        }
    }
}