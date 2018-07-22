package com.dew.edward.dewbe.di

import android.content.Context
import com.dew.edward.dewbe.DewLightApp
import com.dew.edward.dewbe.repository.YoutubeRepository
import com.dew.edward.dewbe.viewmodel.ViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Edward on 7/22/2018.
 */

@Module
class AppModule(private val app: DewLightApp) {

    @Singleton
    @Provides
    fun provideContext(): Context = app


    @Singleton
    @Provides
    fun provideYoutubeRepository(): YoutubeRepository =
            YoutubeRepository.getInstance()

    @Singleton
    @Provides
    fun provideViewModelFactory(repository: YoutubeRepository): ViewModelFactory =
            ViewModelFactory(repository)

}