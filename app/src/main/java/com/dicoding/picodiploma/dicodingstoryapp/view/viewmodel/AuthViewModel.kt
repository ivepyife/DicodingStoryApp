package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.data.source.UserRepository
import com.dicoding.picodiploma.dicodingstoryapp.view.widget.ImageBannerWidget

class AuthViewModel(private val repository: UserRepository) : ViewModel()  {
    fun login(email: String, password: String) = repository.login(email, password)

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)

    fun updateWidgetAfterLogin(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, ImageBannerWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.stack_view)
    }
}