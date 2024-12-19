package com.example.aplikasistory

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ListStoryItem

class StoryPagingSource(
    private val apiService: ApiService,
    private val dataStoreHelper: DataStoreHelper
) : PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val token = dataStoreHelper.getToken() ?: throw Exception("Token is missing")
            val position = params.key ?: 1
            val response = apiService.getStories("Bearer $token", position, params.loadSize)
            val stories = response.listStory?.filterNotNull() ?: emptyList()
            LoadResult.Page(
                data = stories,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}





