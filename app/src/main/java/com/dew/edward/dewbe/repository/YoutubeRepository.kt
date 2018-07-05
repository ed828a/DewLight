package com.dew.edward.dewbe.repository

import com.dew.edward.dewbe.api.YoutubeAPI
import java.util.concurrent.Executors


/**
 * Created by Edward on 7/4/2018.
 */
class YoutubeRepository private constructor() {

    private val networkExecutor = Executors.newFixedThreadPool(5)
    private val api by lazy { YoutubeAPI.create() }

    fun getRepository() =
            InMemoryByPageKeyedRepository (youtubeApi = api, networkExecutor = this.networkExecutor)

    companion object {
        private val LOCK = Any()
        private var instance: YoutubeRepository? = null
        fun getInstance(): YoutubeRepository {
            instance
                    ?: synchronized(LOCK) {
                        instance
                                ?: YoutubeRepository().also { instance = it }
                    }
            return instance!!
        }
    }
}