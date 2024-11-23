package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.ListStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.source.UserRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.UserModel
import com.dicoding.picodiploma.dicodingstoryapp.view.widget.ImageBannerWidget
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository,
    private val context: Context
) : ViewModel() {

    private val _story = MutableLiveData<Result<ListStoryResponse>>()
    val story: LiveData<Result<ListStoryResponse>> = _story

    fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, ImageBannerWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.stack_view)
    }

    init {
        getStory()
    }

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            updateWidget()
        }
    }

    fun getStory() {
        viewModelScope.launch {
            _story.value = Result.Loading
            val result = storyRepository.getStory()
            _story.value = result

            if (result is Result.Success) {
                updateWidget()
            }
        }
    }
}