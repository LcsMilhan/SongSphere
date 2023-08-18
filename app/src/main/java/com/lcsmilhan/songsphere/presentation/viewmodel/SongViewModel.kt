package com.lcsmilhan.songsphere.presentation.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import com.lcsmilhan.songsphere.service.PlaybackState
import com.lcsmilhan.songsphere.service.player.MediaEvent
import com.lcsmilhan.songsphere.service.player.MediaState
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import com.lcsmilhan.songsphere.utils.collectPlayerState
import com.lcsmilhan.songsphere.utils.launchPlaybackStateJob
import com.lcsmilhan.songsphere.utils.resetSongs
import com.lcsmilhan.songsphere.utils.toMediaItemList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@SuppressLint("AutoboxingStateCreation")
@HiltViewModel
class SongViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val repository: SongRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    var progress by savedStateHandle.saveable { mutableStateOf(0f) }

    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song> get() = _songs

    private var isSongPlay: Boolean = false

    var selectedSong: Song? by mutableStateOf(null)
        private set

    private var selectedSongIndex: Int by mutableStateOf(-1)

    private val _playbackState = MutableStateFlow(PlaybackState(0L, 0L))
    val playbackState: StateFlow<PlaybackState> get() = _playbackState

    private var playbackStateJob: Job? = null
    private var isAuto: Boolean = false
    val isServiceRunning = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _songs.addAll(repository.getAllSongs())
            songServiceHandler.initPlayer(songs.toMediaItemList())
            observePlayerState()
        }
    }

    fun onMediaEvent(mediaEvent: MediaEvent) = viewModelScope.launch {
        when (mediaEvent) {
            MediaEvent.PlayPause -> {
                songServiceHandler.onMediaEvent(MediaEvent.PlayPause)
            }
            MediaEvent.Next -> {
                if (selectedSongIndex < songs.size - 1) {
                    onSongSelected(selectedSongIndex + 1)
                    songServiceHandler.onMediaEvent(MediaEvent.Next)
                }
            }
            MediaEvent.Previous -> {
                if (selectedSongIndex > 0 ){
                    onSongSelected(selectedSongIndex - 1)
                    songServiceHandler.onMediaEvent(MediaEvent.Previous)
                }
            }
            MediaEvent.Stop -> {
                songServiceHandler.onMediaEvent(MediaEvent.Stop)
            }
            is MediaEvent.SongClick -> {
                onSongClick(mediaEvent.song)
            }
            is MediaEvent.UpdateProgress -> {
                progress = mediaEvent.newProgress
                songServiceHandler.onMediaEvent(
                    MediaEvent.UpdateProgress(
                        mediaEvent.newProgress
                    )
                )
            }
        }
    }

    private fun onSongClick(song: Song) {
        onSongSelected(songs.indexOf(song))
    }

    private fun onSongSelected(index: Int) {
        if (selectedSongIndex == -1) isSongPlay = true
        if (selectedSongIndex == -1 || selectedSongIndex != index) {
            _songs.resetSongs()
            selectedSongIndex = index
            setUpSong()
        }
    }

    private fun setUpSong() {
        if (!isAuto){
            songServiceHandler.setUpSong(
                selectedSongIndex,
                isSongPlay
            )
            isAuto = false
        }
    }

    private fun updateState(state: MediaState) {
        if (selectedSongIndex != -1) {
            isSongPlay = state == MediaState.Playing(true)
            _songs[selectedSongIndex].state = state
            _songs[selectedSongIndex].isSelected = true
            selectedSong = null
            selectedSong = songs[selectedSongIndex]

            updatePlaybackState(state)
            if (state == MediaState.Ended) onSongSelected(0)
        }
    }

    private fun updatePlaybackState(state: MediaState) {
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
}
