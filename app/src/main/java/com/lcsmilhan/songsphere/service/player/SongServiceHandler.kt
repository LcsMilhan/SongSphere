package com.lcsmilhan.songsphere.service.player

import androidx.media3.common.MediaItem
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

    private var job: Job? = null

    init {
        player.addListener(this)
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        player.setMediaItems(mediaItems)
        player.prepare()
    }

    suspend fun onMediaEvents(
        mediaEvent: MediaEvent,
        selectedSongIndex: Int = -1,
        seekPosition: Long = 0
    ) {
        when (mediaEvent) {
            MediaEvent.PlayPause -> playOrPause()
            MediaEvent.SeekTo -> player.seekTo(seekPosition)
            MediaEvent.SeekToNext -> player.seekToNext()
            MediaEvent.SeekToPrevious -> player.seekToPrevious()
            MediaEvent.SelectedSongChange -> {
                when (selectedSongIndex) {
                    player.currentMediaItemIndex -> playOrPause()
                    else -> {
                        player.seekToDefaultPosition(selectedSongIndex)
                        _mediaState.value = MediaState.Playing(true)
                        player.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            MediaEvent.Stop -> stopProgressUpdate()
            is MediaEvent.UpdateProgress -> {
                player.seekTo(
                    (player.duration * mediaEvent.newProgress).toLong()
                )
            }
        }
    }

    private suspend fun playOrPause() {
        if (player.isPlaying) {
            player.pause()
            stopProgressUpdate()
        }
        else {
            player.play()
            _mediaState.value = MediaState.Playing(true)
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(500)
            _mediaState.value = MediaState.Progress(player.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _mediaState.value = MediaState.Playing(false)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                _mediaState.value = MediaState.Buffering(player.currentPosition)
            }
            ExoPlayer.STATE_READY -> {
                _mediaState.value = MediaState.Ready(player.duration)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _mediaState.value = MediaState.Playing(isPlaying)
        _mediaState.value = MediaState.CurrentPlaying(player.currentMediaItemIndex)
        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        }
        else { stopProgressUpdate() }
    }

}

sealed class MediaEvent {
    object PlayPause : MediaEvent()
    object SelectedSongChange : MediaEvent()
    object SeekToNext : MediaEvent()
    object SeekToPrevious : MediaEvent()
    object SeekTo : MediaEvent()
    object Stop : MediaEvent()
    data class UpdateProgress(val newProgress: Float) : MediaEvent()
}

sealed class MediaState {
    object Initial : MediaState()
    data class Ready(val duration: Long) : MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
    data class CurrentPlaying(val mediaItemIndex: Int) : MediaState()
}