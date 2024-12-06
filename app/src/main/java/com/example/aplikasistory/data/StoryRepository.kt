package com.example.aplikasistory.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.data.response.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout


class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference)
            }
    }

    fun getStoryDetail(storyId: String): LiveData<Result<Story>> {
        return liveData(Dispatchers.IO) {
            emit(Result.Loading) // Menandakan data sedang dimuat
            try {
                val response = apiService.getStoryDetail(storyId) // Ganti endpoint sesuai API Anda
                emit(Result.Success(response)) // Jika berhasil
            } catch (e: Exception) {
                emit(Result.Error(e)) // Jika terjadi error
            }
        }
    }

    // Fungsi untuk mengambil list story
    suspend fun getStories(): Flow<Result<List<ListStoryItem>>> {
        return flow {
            emit(Result.Loading)  // Menandakan data sedang dimuat
            try {
                // Ambil token dari UserPreference
                val token = userPreference.getUser().first().token
                if (token.isEmpty()) throw Exception("Token is missing")

                // Panggil API dengan token
                val response = apiService.getStories("Bearer $token")
                val nonNullList = response.listStory?.filterNotNull() ?: emptyList()  // Pastikan tidak null
                emit(Result.Success(nonNullList))
            } catch (e: Exception) {
                emit(Result.Error(e))  // Jika error
            }
        }
    }

    suspend fun clearSession() {
        userPreference.clearSession()
    }
}
