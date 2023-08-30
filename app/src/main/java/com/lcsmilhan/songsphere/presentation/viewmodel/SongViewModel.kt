package com.lcsmilhan.songsphere.presentation.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.*
import androidx.media3.session.*
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import com.lcsmilhan.songsphere.service.PlaybackState
import com.lcsmilhan.songsphere.service.PlayerEvents
import com.lcsmilhan.songsphere.service.PlayerStates
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import com.lcsmilhan.songsphere.utils.collectPlayerState
import com.lcsmilhan.songsphere.utils.launchPlaybackStateJob
import com.lcsmilhan.songsphere.utils.resetSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("AutoboxingStateCreation")
@HiltViewModel
class SongViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val repository: SongRepository,
) : ViewModel(), PlayerEvents {

    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song> get() = _songs

    private var isSongPlay: Boolean = false

    private val _selectSong = MutableStateFlow<Song?>(null)
    val selectSong = _selectSong.asStateFlow()

    private var currentIndex = songServiceHandler.currentIndex

    private val _playbackState = MutableStateFlow(PlaybackState(0L, 0L))
    val playbackState: StateFlow<PlaybackState> get() = _playbackState


    var isServiceRunning = false
    private var playbackStateJob: Job? = null

    private var isAuto: Boolean = false

    init {
        loadData()
        observePlayerState()
    }

    private fun loadData() = viewModelScope.launch {
        _songs.addAll(repository.getAllSongs())
        songServiceHandler.initPlayer(
            _songs.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.mediaId)
                    .setUri(song.songUrl.toUri())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.songName)
                            .setArtist(song.artist)
                            .setArtworkUri(song.imageUrl.toUri())
                            .build()
                    ).build()
            }.toMutableList()
        )
    }

    private fun onSongSelected(index: Int) {
        if (currentIndex == -1) isSongPlay = true
        if (currentIndex == -1 || currentIndex != index) {
            _songs.resetSongs()
            currentIndex = index
//            Log.w("viewmodel", "onSongSelected index = $currentIndex")
            setUpSong()
        }
    }

    private fun setUpSong() {
        if (!isAuto) {
            songServiceHandler.setUpSong(
                currentIndex,
                isSongPlay
            )
            isAuto = false
        }
    }

    private fun updateState(state: PlayerStates) {
        if (currentIndex != -1) {
            isSongPlay = state == PlayerStates.STATE_PLAYING
            _songs[currentIndex].state = state
            _songs[currentIndex].isSelected = true
            _selectSong.value = null
            _selectSong.value = _songs[currentIndex]

            updatePlaybackState(state)
            Log.e("viewmodel", "$state")
            if (state == PlayerStates.STATE_CHANGE_SONG) {
                isAuto = true
                songServiceHandler.setUpSongNotification(
                    currentIndex,
                    isSongPlay
                )
            }
            if (state == PlayerStates.STATE_PREVIOUS_SONG) {
                isAuto = false
                songServiceHandler.setUpSongNotification(
                    currentIndex,
                    isSongPlay
                )
            }
            if (state == PlayerStates.STATE_END) {
                onSongSelected(0)
            }
        }
    }

    private fun updatePlaybackState(state: PlayerStates) {
        playbackStateJob?.cancel()
        playbackStateJob = viewModelScope
            .launchPlaybackStateJob(
                _playbackState,
                state,
                songServiceHandler
            )
    }

    private fun observePlayerState() {
        viewModelScope.collectPlayerState(songServiceHandler, ::updateState)
    }

    override fun onCleared() {
        super.onCleared()
        songServiceHandler.releasePlayer()
    }

    override fun onPlayPauseClick() {
        songServiceHandler.playPause()
    }

    override fun onPreviousClick() {
        Log.d("viewmodel", "fun onPreviousClick() currentIndex = $currentIndex")
        if (currentIndex > 0) {
            onSongSelected(currentIndex - 1)
            Log.d("viewmodel", "fun onPreviousClick() onSongSelected = $currentIndex")
        }
    }

    override fun onNextClick() {
        Log.i("viewmodel", "fun onNextClick() currentIndex = $currentIndex")
        if (currentIndex < _songs.size - 1) {
            onSongSelected(currentIndex + 1)
            Log.i("viewmodel", "fun onNextClick() onSongSelected = $currentIndex")
        }
    }

    override fun onSongClick(song: Song) {
        onSongSelected(_songs.indexOf(song))
    }

    override fun onSeekBarPositionChanged(position: Long) {
        viewModelScope.launch { songServiceHandler.seekToPosition(position) }
    }
}

