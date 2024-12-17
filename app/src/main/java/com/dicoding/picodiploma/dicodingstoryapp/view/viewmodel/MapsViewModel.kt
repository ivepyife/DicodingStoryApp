package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.StoryItem
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import kotlinx.coroutines.launch

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<List<StoryItem>>()
    val stories: LiveData<List<StoryItem>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getStoriesWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = storyRepository.getStoryWithLocation()
            when (result) {
                is Result.Success -> {
                    _stories.value = result.data.listStory?.filter { it.lat != null && it.lon != null } ?: emptyList()
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _error.value = result.error
                    _isLoading.value = false
                }
                is Result.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }
}