package com.fliks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespuestaTMDB(
    val results: List<PeliculaTMDB>
)

@Serializable
data class PeliculaTMDB(
    val id: Int,
    val title: String,
    @SerialName("poster_path") val posterPath: String?,
    val overview: String?
)