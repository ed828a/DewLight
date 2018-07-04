package com.dew.edward.dewbe.ui

import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.dew.edward.dewbe.util.KEY_DB_TYPE
import com.dew.edward.dewbe.util.KEY_IS_DB_ENABLED
import com.dew.edward.dewbe.util.KEY_PLAYER_TYPE
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
        Log.d("DewApp", "onCreate() called")
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        sharedPreferences = getSharedPreferences("DewApp", Context.MODE_PRIVATE)
    }
}