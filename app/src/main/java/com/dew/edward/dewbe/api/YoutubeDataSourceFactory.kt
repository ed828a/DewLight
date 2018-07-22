package com.dew.edward.dewbe.api

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.dew.edward.dewbe.model.QueryData
import com.dew.edward.dewbe.model.VideoModel
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Created by Edward on 7/4/2018.
 */
class YoutubeDataSourceFactory @Inject constructor(
        private val youtubeApi: YoutubeAPI,
        private val searchQuery: QueryData,
        private val retryExecutor: Executor) : DataSource.Factory<String, VideoModel>() {

    val sourceLiveData = MutableLiveData<PageKeyedYoutubeDataSource>()

    override fun create(): DataSource<String, VideoModel> {
        val source = PageKeyedYoutubeDataSource(youtubeApi, searchQuery, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}