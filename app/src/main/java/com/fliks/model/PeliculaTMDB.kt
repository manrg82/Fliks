package com.fliks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespuestaTMDB(
    val results: List<PeliculaTMDB>
)

@Serializable
data class PeliculaTMDB(//dataclass de pelicula con los campos que me interesan de la api de tmdb
    val id: Int,
    val title: String,
    @SerialName("poster_path") val posterPath: String?,
    val overview: String?,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    val runtime: Int? = null,
    val certification: String? = null
)

@Serializable
data class DetallePeliculaTMDB(
    val runtime: Int? = null,
    @SerialName("release_dates") val releaseDates: ReleaseDatesResponse? = null
)

@Serializable
data class ReleaseDatesResponse(
    val results: List<ReleaseDatesResult> = emptyList()
)

@Serializable
data class ReleaseDatesResult(
    @SerialName("iso_3166_1") val countryCode: String,
    @SerialName("release_dates") val releaseDates: List<ReleaseDateItem> = emptyList()
)

@Serializable
data class ReleaseDateItem(
    val certification: String? = null
)