package com.dew.edward.dewbe.util

import io.reactivex.Scheduler
import java.util.concurrent.Executor


/**
 * Created by Edward on 7/22/2018.
 */
interface DewScheduler {
    fun mainThread(): Scheduler
    fun io(): Scheduler
    fun networkExecutor(): Executor
    fun onceExecutor(): Executor
}