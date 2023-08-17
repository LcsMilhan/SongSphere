package com.lcsmilhan.songsphere.presentation.viewmodel

sealed class SongState {
    object Initial : SongState()
    object Ready : SongState()
    object Ended : SongState()
    object Next : SongState()
    data class PlaybackState(
        val currentPlaybackPosition: Long,
        val currentTrackDuration: Long
    )
}

