package com.lcsmilhan.songsphere.utils

import androidx.media3.common.MediaItem
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.viewmodel.SongState
import com.lcsmilhan.songsphere.service.MediaState
import com.lcsmilhan.songsphere.service.SongServiceHandler
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
    return this.map { MediaItem.fromUri(it.songUrl) }
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
    playbackStateFlow: MutableStateFlow<SongState.PlaybackState>,
    state: MediaState,
    songServiceHandler: SongServiceHandler
) = launch {
    do {
        playbackStateFlow.emit(
            SongState.PlaybackState(
                currentPlaybackPosition = songServiceHandler.currentPlaybackPosition,
                currentTrackDuration = songServiceHandler.currentSongDuration
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