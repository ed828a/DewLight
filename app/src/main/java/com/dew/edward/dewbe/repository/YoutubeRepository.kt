package com.dew.edward.dewbe.repository

import android.arch.lifecycle.MutableLiveData
import android.os.Environment
import android.util.Log
import com.dew.edward.dewbe.api.YoutubeAPI
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.concurrent.Executors


/**
 * Created by Edward on 7/4/2018.
 */
class YoutubeRepository private constructor() {

    private val networkExecutor = Executors.newFixedThreadPool(5)
    private val onceExecutor = Executors.newSingleThreadExecutor()
        private val api by lazy { YoutubeAPI.create() }

    val isSucessful = MutableLiveData<Boolean>()


    fun getRepository() =
            InMemoryByPageKeyedRepository(youtubeApi = api, networkExecutor = this.networkExecutor)

    companion object {
        private val TAG = YoutubeRepository::class.java.simpleName
        private val LOCK = Any()
        private var instance: YoutubeRepository? = null
        fun getInstance(): YoutubeRepository {
            instance
                    ?: synchronized(LOCK) {
                        instance
                                ?: YoutubeRepository().also { instance = it }
                    }
            return instance!!
        }
    }

    fun downloadV(urlString: String, fileName: String) {

    }

    private fun writeResponseBodyToDisk(fileName: String, responseBody: ResponseBody): Boolean {
        val fileFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d(TAG, "filename = $fileFolder/$fileName")
        val file = File(fileFolder, "$fileName.mp4")
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = responseBody.contentLength()
            var fileSizeDownloaded: Long = 0

            inputStream = responseBody.byteStream()
            outputStream = FileOutputStream(file)

            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                fileSizeDownloaded += read.toLong()

                Log.d(TAG, "Downloading progress: $fileSizeDownloaded of $fileSize")
            }
            outputStream.flush()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private val functions = fun(urlString: String, fileName: String) {
        val call = api.downloadByUrlStream(urlString)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                t?.printStackTrace()
                Log.d(YoutubeRepository.TAG, "downloading failed: ${t?.message}")
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response != null && response.isSuccessful) {
                    onceExecutor.execute {
                        val isFinished = writeResponseBodyToDisk(fileName, response.body()!!)
                        isSucessful.postValue(isFinished)
                        Log.d(YoutubeRepository.TAG, "downloading completed successfully.")
                    }
                } else {
                    isSucessful.postValue(false)
                    Log.d(YoutubeRepository.TAG, "Response Error: ${response?.message()}")
                }
            }
        })
    }
}