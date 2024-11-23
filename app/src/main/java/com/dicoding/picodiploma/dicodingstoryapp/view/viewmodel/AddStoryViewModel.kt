package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.AddStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import com.dicoding.picodiploma.dicodingstoryapp.utils.reduceFileImage
import com.dicoding.picodiploma.dicodingstoryapp.utils.uriToFile
import kotlinx.coroutines.launch
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _currentImgUri = MutableLiveData<Uri?>()
    val currentImgUri: LiveData<Uri?> = _currentImgUri

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResult = MutableLiveData<Result<AddStoryResponse>>()
    val uploadResult: LiveData<Result<AddStoryResponse>> = _uploadResult

    fun setCurrentImage(uri: Uri?) {
        _currentImgUri.value = uri
    }

    fun uploadImage(context: Context, description: String, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentImgUri.value?.let { uri ->
                    val imageFile = uriToFile(uri, context).reduceFileImage()
                    val result = repository.addStory(description, imageFile, lat, lon)
                    _uploadResult.value = result
                } ?: run {
                    _uploadResult.value = Result.Error(context.getString(R.string.no_image_selected))
                }
            } catch (e: Exception) {
                _uploadResult.value = Result.Error(e.message ?: context.getString(R.string.an_error_occurred))
            } finally {
                _isLoading.value = false
            }
        }
    }
}