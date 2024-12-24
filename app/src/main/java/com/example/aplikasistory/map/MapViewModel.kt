package com.example.aplikasistory.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.aplikasistory.data.StoryRepository
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.Story
import com.example.aplikasistory.data.response.StoryResponse
import kotlinx.coroutines.launch
import com.example.aplikasistory.data.Result
import com.example.aplikasistory.data.UserRepository


class MapViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun getStoriesWithLocation(token: String): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = storyRepository.getStoriesWithLocation(token)
            emit(Result.Success(response))
        } catch (e: Exception) {

            emit(Result.Error(Throwable(e.message)))
        }
    }
}


