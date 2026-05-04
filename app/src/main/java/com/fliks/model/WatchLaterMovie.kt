package com.fliks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchLaterMovie(
    @SerialName("user_id") val userId: String,
    @SerialName("movie_id") val movieId: Int,
    @SerialName("title") val title: String,
    @SerialName("poster_path") val posterPath: String,
    @SerialName("is_seen") val isSeen: Boolean
)