package com.dicoding.picodiploma.dicodingstoryapp.view.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.squareup.picasso.Picasso
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.StoryItem
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiConfig
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.UserPreference
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.dataStore
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiService
import com.dicoding.picodiploma.dicodingstoryapp.view.detail.DetailActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {
    private val mWidgetItems = ArrayList<StoryItem>()
    private lateinit var userPreference: UserPreference
    private lateinit var apiService: ApiService
    private var isLoggedIn = false

    override fun onCreate() {
        userPreference = UserPreference.getInstance(mContext.dataStore)
        apiService = ApiConfig.getApiService(userPreference)

        runBlocking {
            isLoggedIn = userPreference.getSession().first().isLogin
        }
    }

    override fun onDataSetChanged() {
        try {
            runBlocking {
                val userModel = userPreference.getSession().first()
                isLoggedIn = userModel.isLogin

                if (isLoggedIn) {
                    var retryCount = 0
                    var success = false

                    while (retryCount < 3 && !success) {
                        try {
                            val response = apiService.getAllStory(page = 1, size = 10)
                            if (response.error == false && !response.listStory.isNullOrEmpty()) {
                                mWidgetItems.clear()
                                mWidgetItems.addAll(response.listStory)
                                success = true
                            }
                        } catch (e: Exception) {
                            retryCount++
                            if (retryCount >= 3) {
                                throw e
                            }
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                } else {
                    mWidgetItems.clear()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mWidgetItems.clear()
        }
    }

    override fun onDestroy() {
        mWidgetItems.clear()
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)

        try {
            if (!isLoggedIn) {
                return rv
            }

            val story = mWidgetItems[position]
            val bitmap: Bitmap = runBlocking {
                try {
                    Picasso.get()
                        .load(story.photoUrl)
                        .resize(300, 300)
                        .centerCrop()
                        .get()
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_place_holder)
                }
            }

            rv.setImageViewBitmap(R.id.imageView, bitmap)
            rv.setTextViewText(R.id.tvDescription, story.description ?: "No description")

            val fillInIntent = Intent().apply {
                putExtra(DetailActivity.EXTRA_ID, story.id)
            }
            rv.setOnClickFillInIntent(R.id.widgetItemContainer, fillInIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}