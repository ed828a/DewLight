package com.dew.edward.dewbe

import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import android.support.v4.content.LocalBroadcastManager
import com.dew.edward.dewbe.di.AppComponent
import com.dew.edward.dewbe.di.AppModule
import com.dew.edward.dewbe.di.DaggerAppComponent
import com.google.android.youtube.player.YouTubePlayer


/**
 * Created by Edward on 6/26/2018.
 */
class DewLightApp : MultiDexApplication() {

    companion object {
        lateinit var sharedPreferences: SharedPreferences
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        sharedPreferences = getSharedPreferences("DewApp", Context.MODE_PRIVATE)
    }
}