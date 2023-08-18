package com.lcsmilhan.songsphere.service.player

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.lcsmilhan.songsphere.domain.model.Song
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongServiceHandler @Inject constructor(
    private val player: ExoPlayer
) : Player.Listener {

    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Initial)
    val mediaState = _mediaState.asStateFlow()

    private val _nextTrackAvailable = MutableStateFlow(false)
    val nextTrackAvailable = _nextTrackAvailable.asStateFlow()

    private val _previousTrackAvailable = MutableStateFlow(false)
    val previousTrackAvailable = _previousTrackAvailable.asStateFlow()

    val currentPlaybackPosition: Long
        get() = if (player.currentPosition > 0) player.currentPosition else 0L

    val currentSongDuration: Long
        get() = if (player.duration > 0) player.duration else 0L

    private var job: Job? = null

    fun initPlayer(songList: List<MediaItem>) {
        player.addListener(this)
        player.setMediaItems(songList)
        player.prepare()
    }

    suspend fun onMediaEvent(mediaEvent: MediaEvent) {
        when (mediaEvent) {
            MediaEvent.PlayPause -> {
                if (player.isPlaying) {
                    player.pause()
                    stopProgressUpdate()
                } else {
                    player.play()
                    startProgressUpdate()
                }
            }
            MediaEvent.Next -> player.seekToNext()
            MediaEvent.Previous -> player.seekToPrevious()
            MediaEvent.Stop -> {
                stopProgressUpdate()
                player.stop()
            }
            is MediaEvent.SongClick -> {}
            is MediaEvent.UpdateProgress -> {
                player.seekTo((player.duration * mediaEvent.newProgress/100).toLong())
            }
        }
    }


    fun setUpSong(index: Int, isSongPlay: Boolean) {
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
            player.seekTo(index, 0)
        }
        if (isSongPlay) player.playWhenReady = true
    }

    fun releasePlayer() {
        player.release()
    }

    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_TIMEOUT -> {
                player.seekToNext()
                player.prepare()
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (player.playbackState == Player.STATE_READY) {
            if (playWhenReady) {
                _mediaState.value = MediaState.Playing(true)
            } else {
                _mediaState.value = MediaState.Playing(false)
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
            reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
        ) {
            _nextTrackAvailable.value = player.hasNextMediaItem()
            _previousTrackAvailable.value = player.hasPreviousMediaItem()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _mediaState.value = MediaState.Playing(isPlaying)
        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                stopProgressUpdate()
            }
        }
        else {
            stopProgressUpdate()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsLoadingChanged(isLoading: Boolean) {
        super.onIsLoadingChanged(isLoading)
        _mediaState.value = MediaState.Loading(
            player.bufferedPercentage,
            player.duration
        )
        if (isLoading) {
            GlobalScope.launch(Dispatchers.Main) {
                startBufferedUpdate()
            }
        }
        else {
            stopBufferedUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(100)
            _mediaState.value = MediaState.Progress(player.currentPosition)
        }
    }
    private fun stopProgressUpdate() {
        job?.cancel()
        _mediaState.value = MediaState.Playing(isPlaying = false)
    }

    private suspend fun startBufferedUpdate() = job.run {
        while (true) {
            delay(500)
            _mediaState.value = MediaState.Loading(
                player.bufferedPercentage,
                player.duration
            )
        }
    }

    private fun stopBufferedUpdate() {
        job?.cancel()
        _mediaState.value = MediaState.Loading(player.bufferedPercentage, player.duration)
    }


    @SuppressLint("SwitchIntDef")
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_IDLE -> {
                _mediaState.value = MediaState.Initial
            }
            ExoPlayer.STATE_BUFFERING -> {
                _mediaState.value = MediaState.Buffering(player.currentPosition)
            }
            ExoPlayer.STATE_READY -> {
                _mediaState.value = MediaState.Ready(player.duration)
                if (player.playWhenReady) {
                    _mediaState.value = MediaState.Playing(true)
                } else {
                    _mediaState.value = MediaState.Playing(false)
                }
            }
            ExoPlayer.STATE_ENDED -> {
                _mediaState.value = MediaState.Ended
            }
        }
    }

}

sealed class MediaEvent {
    object PlayPause : MediaEvent()
    object Stop : MediaEvent()
    object Next : MediaEvent()
    object Previous : MediaEvent()
    data class SongClick(val song: Song) : MediaEvent()
    data class UpdateProgress(val newProgress: Float) : MediaEvent()
}

sealed class MediaState {
    object Initial : MediaState()
    object Ended : MediaState()
    data class Ready(val duration: Long) : MediaState()
    data class Loading(val bufferedPercentage: Int, val duration: Long): MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
}