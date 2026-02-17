package org.android.prismplayer.data.network

import org.android.prismplayer.data.model.LyricsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String?,
        @Query("duration") duration: Int
    ): Response<LyricsResponse>
}