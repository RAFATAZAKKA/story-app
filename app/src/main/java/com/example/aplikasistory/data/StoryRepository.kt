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
            emit(Result.Loading)
            try {
                val response = apiService.getStoryDetail(storyId)
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }


    suspend fun getStories(): Flow<Result<List<ListStoryItem>>> {
        return flow {
            emit(Result.Loading)
            try {

                val token = userPreference.getUser().first().token
                if (token.isEmpty()) throw Exception("Token is missing")


                val response = apiService.getStories("Bearer $token")
                val nonNullList = response.listStory?.filterNotNull() ?: emptyList()
                emit(Result.Success(nonNullList))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }

    suspend fun clearSession() {
        userPreference.clearSession()
    }
}
