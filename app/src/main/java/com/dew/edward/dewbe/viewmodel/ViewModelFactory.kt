package com.dew.edward.dewbe.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.dew.edward.dewbe.repository.YoutubeRepository
import javax.inject.Inject


/**
 * Created by Edward on 7/23/2018.
 */
class ViewModelFactory @Inject constructor(
        private val repository: YoutubeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoViewModel::class.java))
            return VideoViewModel(repository) as T
        else
            throw IllegalArgumentException("Unknown ViewModel Class")
    }
}