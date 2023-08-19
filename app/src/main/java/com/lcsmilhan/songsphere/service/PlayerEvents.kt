package com.lcsmilhan.songsphere.service

import com.lcsmilhan.songsphere.domain.model.Song

interface PlayerEvents {
    fun onPlayPauseClick()
    fun onPreviousClick()
    fun onNextClick()
    fun onSongClick(song: Song)
    fun onSeekBarPositionChanged(position: Long)
}