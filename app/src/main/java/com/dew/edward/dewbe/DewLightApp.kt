package com.dew.edward.dewbe

import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import android.support.v4.content.LocalBroadcastManager
import com.google.android.youtube.player.YouTubePlayer


/**
 * Created by Edward on 6/26/2018.
 */
class DewLightApp : MultiDexApplication() {

    companion object {
        lateinit var localBroadcastManager: LocalBroadcastManager
        var mYoutubePlayer: YouTubePlayer? = null
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {

        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        sharedPreferences = getSharedPreferences("DewApp", Context.MODE_PRIVATE)
    }
}