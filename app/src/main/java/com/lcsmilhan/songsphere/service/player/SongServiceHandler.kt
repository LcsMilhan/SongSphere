package com.lcsmilhan.songsphere.service.player

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
import androidx.media3.exoplayer.ExoPlayer
import com.lcsmilhan.songsphere.service.PlayerStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class SongServiceHandler @Inject constructor(
    private val player: ExoPlayer
) : Player.Listener {

    private val _mediaState = MutableStateFlow(PlayerStates.STATE_IDLE)
    val mediaState = _mediaState.asStateFlow()

    val currentPlaybackPosition: Long
        get() = if (player.currentPosition > 0) player.currentPosition else 0L

    val currentSongDuration: Long
        get() = if (player.duration > 0) player.duration else 0L

    var currentIndex: Int by mutableStateOf(player.currentMediaItemIndex -1)

    init {
        player.addListener(this)
    }

    fun initPlayer(songList: MutableList<MediaItem>) {
        player.addListener(this)
        player.setMediaItems(songList)
        player.prepare()
    }

    fun setUpSong(index: Int, isSongPlay: Boolean) {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.seekTo(index, 0)
        if (isSongPlay) player.playWhenReady = true
    }

    fun setUpSongNotification(index: Int, isSongPlay: Boolean) {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.seekTo(index, 0)
        if (isSongPlay) player.playWhenReady = true
    }

    fun playPause() {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.playWhenReady = !player.playWhenReady
    }

    fun releasePlayer() {
        player.release()
    }

    fun seekToPosition(position: Long) {
        player.seekTo(position)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _mediaState.value = PlayerStates.STATE_ERROR
    }


    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (player.playbackState == Player.STATE_READY) {
            if (playWhenReady) {
                _mediaState.value = PlayerStates.STATE_PLAYING
            } else {
                _mediaState.value = PlayerStates.STATE_PAUSE
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == MEDIA_ITEM_TRANSITION_REASON_SEEK ||
            reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            _mediaState.tryEmit(PlayerStates.STATE_CHANGE_SONG)
            _mediaState.tryEmit(PlayerStates.STATE_PREVIOUS_SONG)
            _mediaState.value = PlayerStates.STATE_PLAYING
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                _mediaState.value = PlayerStates.STATE_IDLE
            }
            Player.STATE_BUFFERING -> {
                _mediaState.value = PlayerStates.STATE_BUFFERING
            }
            Player.STATE_READY -> {
                _mediaState.value = PlayerStates.STATE_READY
                if (player.playWhenReady) {
                    _mediaState.value = PlayerStates.STATE_PLAYING
                } else {
                    _mediaState.value = PlayerStates.STATE_PAUSE
                }
            }
            Player.STATE_ENDED -> {
                _mediaState.value = PlayerStates.STATE_END
            }
        }
    }
}