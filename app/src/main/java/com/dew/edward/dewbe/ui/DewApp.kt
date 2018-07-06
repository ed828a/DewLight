package com.dew.edward.dewbe.ui

import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.youtube.player.YouTubePlayer


/**
 * Created by Edward on 6/26/2018.
 */
class DewApp : MultiDexApplication() {

    companion object {
        lateinit var localBroadcastManager: LocalBroadcastManager
        var mYoutubePlayer: YouTubePlayer? = null
        lateinit var sharedPreferences: SharedPreferences
        lateinit var playerType: String
        var dbType: String = ""
        var isDbEnabled = false
    }

    override fun onCreate() {
        Log.d("DewApp", "onCreate() called")
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        sharedPreferences = getSharedPreferences("DewApp", Context.MODE_PRIVATE)

        Log.d("DewApp", "playerType = $playerType, isDbEnabled = $isDbEnabled, dbType = $dbType")
    }
}