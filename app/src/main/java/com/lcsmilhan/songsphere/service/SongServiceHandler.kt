package com.lcsmilhan.songsphere.service

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

    private val _nextSong = MutableStateFlow(false)
    val nextSong = _nextSong.asStateFlow()

    private val _previousSong = MutableStateFlow(false)
    val previousSong = _previousSong.asStateFlow()

    val currentPlaybackPosition: Long
        get() = if (player.currentPosition > 0) player.currentPosition else 0L

    val currentSongDuration: Long
        get() = if (player.duration > 0) player.duration else 0L

    private var job: Job? = null

    init {
        player.addListener(this)
        job = Job()
    }

    fun addMediaItemList(mediaItemList: List<MediaItem>) {
        player.setMediaItems(mediaItemList)
        player.prepare()
    }

    fun setUpSong(index: Int, isSongPlay: Boolean) {
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
            player.seekTo(index, 0)
        }
        if (isSongPlay) player.playWhenReady = true
    }


    suspend fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            PlayerEvent.Backward -> player.seekBack()
            PlayerEvent.Forward -> player.seekForward()
            PlayerEvent.PlayPause -> {
                if (player.isPlaying) {
                    player.pause()
                    stopProgressUpdate()
                } else {
                    player.play()
                    _mediaState.value = MediaState.Playing(isPlaying = true)
                    startProgressUpdate()
                }
            }
            PlayerEvent.Next -> player.seekToNext()
            PlayerEvent.Previous -> player.seekToPrevious()
            PlayerEvent.Stop -> {
                stopProgressUpdate()
                player.stop()
            }
            is PlayerEvent.UpdateProgress -> {
                player.seekTo(
                    (player.duration * playerEvent.newProgress / 100).toLong()
                )
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_TIMEOUT -> {
                player.seekToNext()
                player.prepare()
                player.playWhenReady = true
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
            reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
        ) {
            _nextSong.value = player.hasNextMediaItem()
            _previousSong.value = player.hasPreviousMediaItem()
        }
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
            }
            ExoPlayer.STATE_ENDED -> {
                _mediaState.value = MediaState.End
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _mediaState.value = MediaState.Playing(isPlaying)
        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
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

}

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object Backward : PlayerEvent()
    object Forward : PlayerEvent()
    object Stop : PlayerEvent()
    object Next : PlayerEvent()
    object Previous : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class MediaState {
    object Initial : MediaState()
    object Next : MediaState()
    object End : MediaState()
    data class Ready(val duration: Long) : MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
}