package org.android.prismplayer.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.android.prismplayer.data.model.GeminiContent
import org.android.prismplayer.data.model.GeminiPart
import org.android.prismplayer.data.model.GeminiRequest
import org.android.prismplayer.data.model.Song
import org.android.prismplayer.data.network.RetrofitClient

class GeminiRepository {

    suspend fun curateSongs(
        userQuery: String,
        apiKey: String,
        systemPrompt: String,
        allSongs: List<Song>
    ): Result<List<Song>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Settings."))
        }

        if (allSongs.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val compactSongs = allSongs.take(300).map { song ->
                buildMap<String, Any> {
                    put("id", song.id)
                    put("t", song.title.take(60))
                    put("a", song.artist.take(40))
                    if (!song.genre.isNullBOBlank()) put("g", song.genre)
                }
            }
            val songsJson = Gson().toJson(compactSongs)

            val fullPrompt = """
                $systemPrompt

                SONG LIBRARY (JSON array: id=ID, t=Title, a=Artist, g=Genre):
                $songsJson

                USER VIBE QUERY:
                $userQuery
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))
                )
            )

            val response = RetrofitClient.geminiApi.generateContent(apiKey, request)
            if (response.isSuccessful && response.body() != null) {
                val responseText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanJson = responseText.trim().removeSurrounding("```json", "```").trim()

                val listType = object : TypeToken<List<Long>>() {}.type
                val matchingIds: List<Long> = Gson().fromJson(cleanJson, listType) ?: emptyList()

                val songMap = allSongs.associateBy { it.id }
                val curatedList = matchingIds.mapNotNull { songMap[it] }

                Result.success(curatedList)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("Gemini error (${response.code()}): $errBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun String?.isNullBOBlank(): Boolean = this == null || this.isBlank() || this == "<unknown>"
}
