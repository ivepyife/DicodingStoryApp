package com.dicoding.picodiploma.dicodingstoryapp.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.StoryItem
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiService

class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, StoryItem>() {
    override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getAllStory(page = position, size = params.loadSize)

            LoadResult.Page(
                data = responseData.listStory ?: emptyList(),
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}