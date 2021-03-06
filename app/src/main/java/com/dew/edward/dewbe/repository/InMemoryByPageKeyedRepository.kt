package com.dew.edward.dewbe.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import android.util.Log
import com.dew.edward.dewbe.api.YoutubeAPI
import com.dew.edward.dewbe.api.YoutubeDataSourceFactory
import com.dew.edward.dewbe.model.LiveDataPagedListing
import com.dew.edward.dewbe.model.NetworkState
import com.dew.edward.dewbe.model.QueryData
import com.dew.edward.dewbe.model.VideoModel
import com.dew.edward.dewbe.util.PAGEDLIST_PAGE_SIZE
import java.util.concurrent.Executor


/**
 * Created by Edward on 7/4/2018.
 */
class InMemoryByPageKeyedRepository(private val youtubeApi: YoutubeAPI,
                                    private val networkExecutor: Executor) {

    private val _networkState = MutableLiveData<NetworkState>()
    private val networkState: LiveData<NetworkState>
        get() = _networkState

    @MainThread  // this function will be called in ViewModel for search videos
    fun searchVideoYoutube(searchYoutube: QueryData, pageSize: Int): LiveDataPagedListing<VideoModel> {

        val sourceFactory = YoutubeDataSourceFactory(youtubeApi, searchYoutube, networkExecutor)

        val livePagedList =
                LivePagedListBuilder(sourceFactory, pageSize)
                        .setFetchExecutor(networkExecutor)
                        .build()

        val refreshTrigger = MutableLiveData<Unit>()

        return LiveDataPagedListing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
                refreshState = Transformations.switchMap(refreshTrigger) { refresh(searchYoutube) },
                refresh = { refreshTrigger.value = null },
                retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() }
        )
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(queryData: QueryData): LiveData<NetworkState> {
        searchVideoYoutube(queryData, PAGEDLIST_PAGE_SIZE)
        _networkState.postValue(NetworkState.LOADED)
        return networkState
    }
}