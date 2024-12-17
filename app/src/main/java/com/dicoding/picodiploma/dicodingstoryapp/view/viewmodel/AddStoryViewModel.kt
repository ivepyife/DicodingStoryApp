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
import android.location.Location

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _currentImgUri = MutableLiveData<Uri?>()
    val currentImgUri: LiveData<Uri?> = _currentImgUri

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResult = MutableLiveData<Result<AddStoryResponse>>()
    val uploadResult: LiveData<Result<AddStoryResponse>> = _uploadResult

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation

    fun setCurrentImage(uri: Uri?) {
        _currentImgUri.value = uri
    }

    fun setCurrentLocation(location: Location?) {
        _currentLocation.value = location
    }

    fun uploadImage(
        context: Context,
        description: String,
        includeLocation: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentImgUri.value?.let { uri ->
                    val imageFile = uriToFile(uri, context).reduceFileImage()

                    val lat = if (includeLocation) _currentLocation.value?.latitude else null
                    val lon = if (includeLocation) _currentLocation.value?.longitude else null

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