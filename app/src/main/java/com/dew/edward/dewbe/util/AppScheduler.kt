package com.dew.edward.dewbe.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Created by Edward on 7/22/2018.
 */
class AppScheduler : DewScheduler {
    override fun networkExecutor(): Executor = Executors.newFixedThreadPool(8)

    override fun onceExecutor(): Executor = Executors.newSingleThreadExecutor()

    override fun mainThread(): Scheduler =AndroidSchedulers.mainThread()

    override fun io(): Scheduler = Schedulers.io()


}