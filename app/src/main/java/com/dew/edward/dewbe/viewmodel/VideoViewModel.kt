package com.dew.edward.dewbe.viewmodel

import android.arch.lifecycle.*
import android.support.v4.app.FragmentActivity
import com.dew.edward.dewbe.model.QueryData
import com.dew.edward.dewbe.model.Type
import com.dew.edward.dewbe.repository.YoutubeRepository
import com.dew.edward.dewbe.util.PAGEDLIST_PAGE_SIZE


/**
 * Created by Edward on 6/26/2018.
 */
class VideoViewModel(private val repository: YoutubeRepository): ViewModel() {

    private val queryString = MutableLiveData<String>()
    private val relatedToVideoId = MutableLiveData<String>()
    private var queryData = MediatorLiveData<QueryData>()
    init {
        queryData.addSource(relatedToVideoId){related ->
            queryData.value = QueryData(related ?: "", Type.RELATED_VIDEO_ID)
        }

        queryData.addSource(queryString) {
            queryData.value = QueryData(it ?: "",Type.QUERY_STRING)
        }
    }

    private val searchResult =
            Transformations.map(queryData) {queryData ->
                repository.getRepository().searchVideoYoutube(queryData, PAGEDLIST_PAGE_SIZE)
            }
    val videoList = Transformations.switchMap(searchResult) { it.pagedList }!!
    val networkState = Transformations.switchMap(searchResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(searchResult) { it.refreshState }!!
    val downloadState: LiveData<Boolean> = Transformations.map(repository.isSucessful) {it -> it }

    fun download(url: String){
        repository.downloadV(url, relatedToVideoId.value ?: "video")
    }
    fun refresh() {
        searchResult.value?.refresh?.invoke()
    }

    fun showSearchQuery(searchQuery: String): Boolean =
            if (queryString.value == searchQuery) false
            else {
                queryString.value = searchQuery
                true
            }

    fun showRelatedToVideoId(videoId: String): Boolean =
            if (relatedToVideoId.value == videoId) false
            else {
                relatedToVideoId.value = videoId
                true
            }

    fun retry(){
        val listing = searchResult?.value
        listing?.retry?.invoke()
    }

    fun currentQuery(): String? = queryString.value

    companion object {
        @SuppressWarnings("Unchecked cast")
        fun getViewModel(context: FragmentActivity): VideoViewModel =
                ViewModelProviders.of(context, object : ViewModelProvider.Factory {
                    val repository = YoutubeRepository.getInstance()
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                            VideoViewModel(repository) as T
                })[VideoViewModel::class.java]
    }
}

