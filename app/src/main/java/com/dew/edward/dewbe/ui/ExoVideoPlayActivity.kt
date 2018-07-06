package com.dew.edward.dewbe.ui

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.dew.edward.dewbe.R
import com.dew.edward.dewbe.adapter.VideoModelAdapter
import com.dew.edward.dewbe.model.NetworkState
import com.dew.edward.dewbe.model.VideoModel
import com.dew.edward.dewbe.util.*
import com.dew.edward.dewbe.viewmodel.VideoViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_exo_video_play.*
import kotlinx.android.synthetic.main.content_list.*

class ExoVideoPlayActivity : AppCompatActivity() {

    private lateinit var videoModel: VideoModel
    private var isRelatedVideo: Boolean = false
    private lateinit var queryViewModel: VideoViewModel
    private lateinit var adapter: VideoModelAdapter
    private lateinit var listView: RecyclerView

    private lateinit var extractor:YouTubeExtractor

    // bandwidth meter to measure and estimate bandwidth
    private var player: SimpleExoPlayer? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true
    private var videoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_video_play)

        if (ContextCompat.checkSelfPermission(this@ExoVideoPlayActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ExoVideoPlayActivity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        videoModel = intent.getParcelableExtra(VIDEO_MODEL)

        extractor = YouTubeExtractor.Builder().okHttpClientBuilder(null).build()

        if (savedInstanceState != null) { // when Rotation, no need to search on the net.
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION)
            videoUrl = savedInstanceState.getString(VIDEO_URL)

            Log.d("onCreate", "playbackPosition = $playbackPosition")
        } else {
            extractUrl()
        }

        queryViewModel = VideoViewModel.getViewModel(this)

        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            textVideoPlayTitle?.text = videoModel.title

            initRelatedList()
            initSearch()
            initDownload()
            queryViewModel.showRelatedToVideoId(videoModel.videoId)
        }

    }

    private fun extractUrl(){
        extractor.extract(videoModel.videoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { extraction ->
                            bindVideoToPlayer(extraction)
                        },
                        { error ->
                            errorHandler(error)
                        }
                )
    }

    private fun initDownload(){
        queryViewModel.downloadState.observe(this, Observer{ isSuccessful ->
            if (isSuccessful != null && isSuccessful){
                Toast.makeText(this@ExoVideoPlayActivity,
                        "Downloading completed successfully.",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ExoVideoPlayActivity,
                        "Downloading failed.", Toast.LENGTH_SHORT).show()
            }
        })
        buttonDownload.setOnClickListener {
            queryViewModel.download(videoUrl)
        }
    }

    private fun initRelatedList() {
        listView = recyclerRelatedListView
        listView.layoutManager = GridLayoutManager(this, 2)
        adapter = VideoModelAdapter(
                { queryViewModel.retry() },
                {
                    extractor.extract(it.videoId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe (
                                    {extraction ->
                                        bindVideoToPlayer(extraction)
                                    },
                                    {error ->
                                        errorHandler(error)
                                    }
                            )

                    textVideoPlayTitle?.text = it.title
                    isRelatedVideo = true
                    intent.putExtra(VIDEO_MODEL, it)

                    if (queryViewModel.showRelatedToVideoId(it.videoId)) {
                        listView.scrollToPosition(0)
                        (listView.adapter as? VideoModelAdapter)?.submitList(null)
                    }
                })
        listView.adapter = adapter

        queryViewModel.videoList.observe(this, videoListObserver)
        queryViewModel.networkState.observe(this, networkStateObserver)
    }

    private fun initSearch() {
        buttonSearch.setOnSearchClickListener {
            buttonDownload.visibility = View.GONE
            textVideoPlayTitle.visibility = View.GONE

            buttonSearch.onActionViewExpanded()
        }

        buttonSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()?.let {
                    if (it.isNotEmpty()) {
                        if (queryViewModel.showSearchQuery(it)) {
                            listView.scrollToPosition(0)
                            (listView.adapter as? VideoModelAdapter)?.submitList(null)
                        }
                    }
                }

                buttonSearch.onActionViewCollapsed()
                buttonDownload.visibility = View.VISIBLE
                textVideoPlayTitle.visibility = View.VISIBLE

                Log.d("onQueryTextSubmit", "queryString: $query")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val closeButton = buttonSearch.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener {
            buttonSearch.onActionViewCollapsed()
            buttonDownload.visibility = View.VISIBLE
            textVideoPlayTitle.visibility = View.VISIBLE
        }
    }

    private fun bindVideoToPlayer(result: YouTubeExtraction) {
        if (result.videoStreams.isEmpty()){
            Toast.makeText(this@ExoVideoPlayActivity,
                    "This video isn't playable. Please try others.", Toast.LENGTH_LONG).show()
            return
        }

        videoUrl = result.videoStreams.first().url
        playbackPosition = 0  // new video start
        Log.d("ExoMediaActivity", "videoUrl: $videoUrl")
        if (player != null) {
            releasePlayer()
        }
        initializePlayer(this, videoUrl)
    }

    private fun errorHandler(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this, "It failed to extract URL from YouTube.", Toast.LENGTH_SHORT).show()
    }

    private fun initializePlayer(context: Context, videoUrl: String) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    DefaultRenderersFactory(context),
                    DefaultTrackSelector(),
                    DefaultLoadControl())

            videoView.player = player
            player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)
            Log.d("initializePlayer", "playbackPosition = $playbackPosition")
        }
        val uri = Uri.parse(videoUrl)
        val mediaSource =
                ExtractorMediaSource.Factory(
                        DefaultHttpDataSourceFactory("exoPlayer"))
                        .createMediaSource(uri)
        player!!.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (Util.SDK_INT > 23) {
            playbackPosition = player?.currentPosition ?: 0
        }
        outState?.putLong(PLAYBACK_POSITION, playbackPosition)
        outState?.putString(VIDEO_URL, videoUrl)
        Log.d("onSaveInstanceState", "playbackPosition = $playbackPosition")
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onResume() {
        super.onResume()
//        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playbackPosition = player?.currentPosition ?: 0
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private val videoListObserver =
            Observer<PagedList<VideoModel>> { videoList -> adapter.submitList(videoList) }

    private val networkStateObserver =
            Observer<NetworkState?> { networkState -> adapter.setNetworkState(networkState) }

    fun fullscreen(view: View){
        requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT){
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

}
