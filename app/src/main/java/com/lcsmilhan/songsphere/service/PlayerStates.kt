package com.lcsmilhan.songsphere.service

enum class PlayerStates {
    STATE_IDLE,
    STATE_READY,
    STATE_BUFFERING,
    STATE_ERROR,
    STATE_END,
    STATE_PLAYING,
    STATE_PAUSE,
    STATE_CHANGE_SONG,
    STATE_NEXT_SONG,
    STATE_PREVIOUS_SONG
}