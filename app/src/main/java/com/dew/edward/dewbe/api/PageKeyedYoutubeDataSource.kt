package com.dew.edward.dewbe.api

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import com.dew.edward.dewbe.model.*
import com.dew.edward.dewbe.util.extractDate
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor


/**
 * Created by Edward on 7/4/2018.
 */
class PageKeyedYoutubeDataSource(
        private val youtubeApi: YoutubeAPI,
        private val searchQuery: QueryData,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, VideoModel>() {

    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    val resultPageInfo = ResultPageInfo()

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }


    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, VideoModel>) {

        val request = if (searchQuery.type == Type.QUERY_STRING) {
            youtubeApi.searchVideo(query = searchQuery.query)
        } else {
            youtubeApi.getRelatedVideos(relatedToVideoId = searchQuery.query)
        }

        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        resultPageInfo.reset()
        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val data = response.body()
            val items = data?.items?.map {
                VideoModel(it.snippet.title, it.snippet.publishedAt.extractDate(), it.snippet.thumbnails.high.url, it.id.videoId)
            }
            // update pageTokens
            with(resultPageInfo) {
                prevPage = data?.prevPageToken ?: ""
                nextPage = data?.nextPageToken ?: ""
                totalResults = data?.pageInfo?.totalResults ?: ""
                if (items != null)
                    receivedItems += items.size
            }

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            if (items != null) {
                callback.onResult(items.toMutableList(), resultPageInfo.prevPage, resultPageInfo.nextPage)
            }
            Log.d("loadInitial", "nextPageToken: ${resultPageInfo.nextPage}, receivedItems: ${resultPageInfo.receivedItems} of ${resultPageInfo.totalResults}")
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, VideoModel>) {
        networkState.postValue(NetworkState.LOADING)
        val call = if (searchQuery.type == Type.QUERY_STRING) {
            youtubeApi.searchVideo(query = searchQuery.query, pageToken = resultPageInfo.nextPage)
        } else {
            youtubeApi.getRelatedVideos(relatedToVideoId = searchQuery.query, pageToken = resultPageInfo.nextPage)
        }

        call.enqueue(object : retrofit2.Callback<YoutubeResponseData> {
            override fun onFailure(call: Call<YoutubeResponseData>?, t: Throwable?) {
                retry = {
                    loadAfter(params, callback)
                }
                networkState.postValue(NetworkState.error(t?.message ?: "unknown error"))
            }

            override fun onResponse(call: Call<YoutubeResponseData>?, response: Response<YoutubeResponseData>?) {
                if (response != null && response.isSuccessful) {
                    val data = response.body()
                    val items = data?.items?.map {
                        VideoModel(it.snippet.title, it.snippet.publishedAt.extractDate(), it.snippet.thumbnails.high.url, it.id.videoId)
                    }
                    // update pageTokens
                    with(resultPageInfo) {
                        prevPage = data?.prevPageToken ?: ""
                        nextPage = data?.nextPageToken ?: ""
                        totalResults = data?.pageInfo?.totalResults ?: ""
                        if (items != null)
                            receivedItems += items.size
                    }


                    retry = null
                    callback.onResult(items as MutableList<VideoModel>, resultPageInfo.nextPage)
                    networkState.postValue(NetworkState.LOADED)
                    Log.d("loadAfter", "nextPageToken: ${resultPageInfo.nextPage}, receivedItems: ${resultPageInfo.receivedItems} of ${resultPageInfo.totalResults}")
                } else {
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(
                            NetworkState.error("error code: ${response?.code()}"))
                }
            }

        })
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, VideoModel>) {
        // ignored, since we only every append to our initial load.
    }
}