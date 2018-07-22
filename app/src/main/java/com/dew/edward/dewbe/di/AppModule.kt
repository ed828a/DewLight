package com.dew.edward.dewbe.di

import android.content.Context
import com.dew.edward.dewbe.DewLightApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Edward on 7/22/2018.
 */

@Module
class AppModule (private val app: DewLightApp){

    @Singleton
    @Provides
    fun provideContext(): Context = app
}