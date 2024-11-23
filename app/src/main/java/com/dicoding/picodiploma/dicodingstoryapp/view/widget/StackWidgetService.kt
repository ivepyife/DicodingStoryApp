package com.dicoding.picodiploma.dicodingstoryapp.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.dicoding.picodiploma.dicodingstoryapp.view.adapter.StackRemoteViewsFactory

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}