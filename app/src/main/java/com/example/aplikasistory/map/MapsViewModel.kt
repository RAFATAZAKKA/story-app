package com.example.aplikasistory.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.data.response.Story
import kotlinx.coroutines.launch


class MapsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> get() = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getStoriesWithLocation("Bearer $token")
                if (response.error == false && response.listStory != null) {
                    _stories.value = response.listStory.toStories() // Konversi data di sini
                } else {
                    _errorMessage.value = response.message ?: "Failed to fetch stories"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }
}

fun List<ListStoryItem?>?.toStories(): List<Story> {
    return this?.mapNotNull { listItem ->
        listItem?.let {
            Story(
                id = it.id ?: "",
                name = it.name ?: "",
                description = it.description ?: "",
                photoUrl = it.photoUrl ?: "",
                createdAt = it.createdAt ?: "",
                lat = it.lat,
                lon = it.lon
            )
        }
    } ?: emptyList()
}


