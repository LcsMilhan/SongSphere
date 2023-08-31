package com.lcsmilhan.songsphere.service.player

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
import androidx.media3.exoplayer.ExoPlayer
import com.lcsmilhan.songsphere.service.PlayerStates
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


class SongServiceHandler @Inject constructor(
    private val player: ExoPlayer
) : Player.Listener {

    val mediaState = MutableStateFlow(PlayerStates.STATE_IDLE)

    val currentPlaybackPosition: Long
        get() = if (player.currentPosition > 0) player.currentPosition else 0L

    val currentSongDuration: Long
        get() = if (player.duration > 0) player.duration else 0L

    private var job: Job? = null

    init {
        player.addListener(this)
        job = Job()
    }

    fun initPlayer(songList: MutableList<MediaItem>) {
        player.setMediaItems(songList)
        player.prepare()
    }

    fun setUpSong(index: Int, isSongPlay: Boolean) {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.seekTo(index, 0)
        if (isSongPlay) player.playWhenReady = true
        Log.d("service", "fun setUpSong()")
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
        mediaState.tryEmit(PlayerStates.STATE_ERROR)
        Log.d("service", "override fun onPlayerError(error = ${mediaState.value})")
    }


    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (player.playbackState == Player.STATE_READY) {
            if (playWhenReady) {
                mediaState.tryEmit(PlayerStates.STATE_PLAYING)
            } else {
                mediaState.tryEmit(PlayerStates.STATE_PAUSE)
            }
        }
    }

    var nextSong = false
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (reason == MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            if (!nextSong) {
                Log.i("service", "1= $nextSong")
                Log.i("service", "2= ${!nextSong}")
                mediaState.tryEmit(PlayerStates.STATE_NEXT_SONG)
                nextSong = false
            }
            mediaState.tryEmit(PlayerStates.STATE_PLAYING)
        }
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                mediaState.tryEmit(PlayerStates.STATE_IDLE)
            }

            Player.STATE_BUFFERING -> {
                mediaState.tryEmit(PlayerStates.STATE_BUFFERING)
            }

            Player.STATE_READY -> {
                mediaState.tryEmit(PlayerStates.STATE_READY)
                if (player.playWhenReady) {
                    mediaState.tryEmit(PlayerStates.STATE_PLAYING)
                } else {
                    mediaState.tryEmit(PlayerStates.STATE_PAUSE)
                }
            }
            Player.STATE_ENDED -> {
                mediaState.tryEmit(PlayerStates.STATE_END)
            }
        }
        Log.d("service", "override fun onPlaybackStateChanged(playbackState = $playbackState)")
    }
}