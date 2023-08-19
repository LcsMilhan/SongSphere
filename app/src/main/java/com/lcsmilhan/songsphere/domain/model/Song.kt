package com.lcsmilhan.songsphere.domain.model

import com.lcsmilhan.songsphere.service.PlayerStates

data class Song(
    val mediaId: String = "",
    val artist: String = "",
    val songName: String = "",
    val songUrl: String = "",
    val imageUrl: String = "",
    var isSelected: Boolean = false,
    var state: PlayerStates = PlayerStates.STATE_IDLE
)
