package com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.picodiploma.dicodingstoryapp.DataDummy
import com.dicoding.picodiploma.dicodingstoryapp.MainDispatcherRule
import com.dicoding.picodiploma.dicodingstoryapp.data.local.entity.StoryEntity
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.source.UserRepository
import com.dicoding.picodiploma.dicodingstoryapp.getOrAwaitValue
import com.dicoding.picodiploma.dicodingstoryapp.view.adapter.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import android.content.Context
import androidx.lifecycle.LiveData

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var context: Context

    private lateinit var mainViewModel: MainViewModel

    @Test
    fun `when Get Story Should Not Null and Return Success`() = runTest {
        val dummyStories = DataDummy.generateDummyQuoteResponse()
        val data = StoryPagingSource.snapshot(dummyStories.map {
            StoryEntity(
                id = it.id ?: "",
                name = it.name ?: "",
                description = it.description ?: "",
                photoUrl = it.photoUrl ?: "",
                createdAt = it.createdAt ?: "",
                lat = it.lat ?: 0.0,
                lon = it.lon ?: 0.0
            )
        })

        val expectedStory: LiveData<PagingData<StoryEntity>> = MutableLiveData(data)

        Mockito.`when`(storyRepository.getStoryPaged()).thenReturn(expectedStory)

        mainViewModel = MainViewModel(userRepository, storyRepository, context)

        val actualStory = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0].name ?: "", differ.snapshot()[0]?.name)
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data = StoryPagingSource.snapshot(emptyList())
        val expectedStory: LiveData<PagingData<StoryEntity>> = MutableLiveData(data)

        Mockito.`when`(storyRepository.getStoryPaged()).thenReturn(expectedStory)

        mainViewModel = MainViewModel(userRepository, storyRepository, context)

        val actualStory = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}

class StoryPagingSource : PagingSource<Int, StoryEntity>() {
    companion object {
        fun snapshot(items: List<StoryEntity>): PagingData<StoryEntity> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}