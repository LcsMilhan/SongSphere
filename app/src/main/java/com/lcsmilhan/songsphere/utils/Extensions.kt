package com.lcsmilhan.songsphere.utils

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.service.PlaybackState
import com.lcsmilhan.songsphere.service.player.MediaState
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


fun List<Song>.resetSongs() {
    this.forEach { song ->
        song.isSelected = false
        song.state = MediaState.Initial
    }
}

fun List<Song>.toMediaItemList(): List<MediaItem> {
    return this.map { MediaItem.fromUri(it.songUrl.toUri()) }
}


fun CoroutineScope.collectPlayerState(
    songServiceHandler: SongServiceHandler,
    updateState: (MediaState) -> Unit
) {
    this.launch {
        songServiceHandler.mediaState.collect {
            updateState(it)
        }
    }
}

fun CoroutineScope.launchPlaybackStateJob(
    playbackStateFlow: MutableStateFlow<PlaybackState>,
    state: MediaState,
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
    } while (state == MediaState.Playing(true) && isActive)
}

fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}