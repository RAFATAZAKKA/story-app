package com.example.aplikasistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.aplikasistory.data.UserRepository
import com.example.aplikasistory.data.Result
import com.example.aplikasistory.data.response.ListStoryItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StoryViewModel(private val userRepository: UserRepository) : ViewModel() {

    val pagedStories: LiveData<PagingData<ListStoryItem>> =
        userRepository.getPagedStories().cachedIn(viewModelScope).asLiveData()

    private val _stories = MutableLiveData<Result<List<ListStoryItem>>>()
    val stories: LiveData<Result<List<ListStoryItem>>> = _stories

    fun fetchStories() {
        viewModelScope.launch {
            userRepository.getStories()
                .catch { e ->
                    _stories.postValue(Result.Error(Exception(e.message ?: "Unknown error")))
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val filteredList = result.data?.filterNotNull() ?: emptyList()
                            _stories.postValue(Result.Success(filteredList))
                        }
                        is Result.Error -> _stories.postValue(Result.Error(result.exception))
                        is Result.Loading -> _stories.postValue(Result.Loading)
                    }
                }
        }
    }


    fun logout() {
        viewModelScope.launch {
            userRepository.clearSession()
        }
    }
}
