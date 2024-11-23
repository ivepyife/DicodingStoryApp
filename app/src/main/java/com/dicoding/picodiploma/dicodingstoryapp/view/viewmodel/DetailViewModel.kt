package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.DetailStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _storyDetail = MutableLiveData<Result<DetailStoryResponse>>()
    val storyDetail: LiveData<Result<DetailStoryResponse>> = _storyDetail

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            _storyDetail.value = Result.Loading
            _storyDetail.value = storyRepository.getStoryDetail(id)
        }
    }
}