package com.fliks.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object TMDBClient {
    const val API_KEY = "d30ee3758477f93540cbb63bef8e7506"
    const val BASE_URL = "https://api.themoviedb.org/3"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
}