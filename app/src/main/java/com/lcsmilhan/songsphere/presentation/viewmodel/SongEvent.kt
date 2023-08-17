package com.lcsmilhan.songsphere.presentation.viewmodel

sealed class SongEvent {
    object PlayPause : SongEvent()
    object Backward : SongEvent()
    object Forward : SongEvent()
    object Next : SongEvent()
    object Previous : SongEvent()
    object Stop : SongEvent()
    data class UpdateProgress(val newProgress: Float) : SongEvent()
}
