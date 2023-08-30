package com.lcsmilhan.songsphere.utils

import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.service.PlaybackState
import com.lcsmilhan.songsphere.service.PlayerStates
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


fun MutableList<Song>.resetSongs() {
    this.forEach { song ->
        song.isSelected = false
        song.state = PlayerStates.STATE_IDLE
    }
}

fun CoroutineScope.collectPlayerState(
    songServiceHandler: SongServiceHandler,
    updateState: (PlayerStates) -> Unit
) {
    this.launch {
        songServiceHandler.mediaState.collect {
            updateState(it)
        }
    }
}

fun CoroutineScope.launchPlaybackStateJob(
    playbackStateFlow: MutableStateFlow<PlaybackState>,
    state: PlayerStates,
    songServiceHandler: SongServiceHandler
) = launch {
    do {
        playbackStateFlow.emit(
            PlaybackState(
                currentPlaybackPosition = songServiceHandler.currentPlaybackPosition,
                currentSongDuration = songServiceHandler.currentSongDuration
            )
        )
        delay(1000)
    } while (state == PlayerStates.STATE_PLAYING && isActive)
}

fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}