package com.dew.edward.dewbe.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.dew.edward.dewbe.model.NetworkState


/**
 * Created by Edward on 6/26/2018.
 */

fun String.dbquery(): String {
    val strings = this.split(" ")
    var stringA = "%${strings[0]}%"
    if (strings.size > 1){
        for (i in 1 until strings.size){
            stringA = "$stringA | %${strings[i]}%"
        }
    }

    return stringA
}

fun String.extractDate(): String {
    val stringArray = this.split('T')

    return stringArray[0]
}

