package com.example.aplikasistory.ui

import org.junit.Assert.*
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.aplikasistory.DataDummy
import com.example.aplikasistory.MainDispatcherRule
import com.example.aplikasistory.StoryPagingSource
import com.example.aplikasistory.StoryViewModel
import com.example.aplikasistory.data.StoryAdapter
import com.example.aplikasistory.data.UserRepository
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Test
    fun `when Get Story Should Not Null and Data Size Same As Expected`() = runTest {

        val dummyStory = DataDummy.generateDummyQuoteResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStory)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(userRepository.getPagedStories()).thenReturn(flowOf(data))


        val storyViewModel = StoryViewModel(userRepository)
        val actualStory = storyViewModel.pagedStories.getOrAwaitValue()


        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory, differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)


        val firstItem = differ.snapshot().firstOrNull()
        assertNotNull(firstItem)
        assertEquals(dummyStory.first(), firstItem)
    }

    @Test
    fun `when Get Empty Story Should Have Zero Size`() = runTest {

        val emptyData: PagingData<ListStoryItem> = StoryPagingSource.snapshot(emptyList())
        `when`(userRepository.getPagedStories()).thenReturn(flowOf(emptyData))

        val storyViewModel = StoryViewModel(userRepository)
        val emptyActualStory = storyViewModel.pagedStories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(emptyActualStory)

        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    companion object {
        private const val TOKEN = "Bearer token"
    }
}

class StoryPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {

    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int? {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
