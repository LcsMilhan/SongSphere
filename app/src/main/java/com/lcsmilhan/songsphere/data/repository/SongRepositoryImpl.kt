package com.lcsmilhan.songsphere.data.repository

import com.google.firebase.firestore.CollectionReference
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val songCollection: CollectionReference
) : SongRepository {

    override suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}