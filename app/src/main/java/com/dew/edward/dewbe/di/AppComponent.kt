package com.dew.edward.dewbe.di

import com.dew.edward.dewbe.ui.ExoVideoPlayActivity
import com.dew.edward.dewbe.ui.MainActivity
import dagger.Component
import javax.inject.Singleton


/**
 * Created by Edward on 7/22/2018.
 */

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(target: MainActivity)
    fun inject(target: ExoVideoPlayActivity)
}