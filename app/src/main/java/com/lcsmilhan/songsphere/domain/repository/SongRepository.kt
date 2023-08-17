package com.lcsmilhan.songsphere.domain.repository

import com.lcsmilhan.songsphere.domain.model.Song

interface SongRepository {

    suspend fun getAllSongs(): List<Song>

}