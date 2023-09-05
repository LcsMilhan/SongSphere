package com.lcsmilhan.songsphere.presentation.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.*
import androidx.media3.session.*
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import com.lcsmilhan.songsphere.service.player.MediaEvent
import com.lcsmilhan.songsphere.service.player.MediaState
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("AutoboxingStateCreation")
@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class SongViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val repository: SongRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var duration by savedStateHandle.saveable { mutableStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var songList by savedStateHandle.saveable { mutableStateOf(listOf<Song>()) }

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _currentSelectedSong = MutableStateFlow<Song?>(null)
    val currentSelectedSong = _currentSelectedSong.asStateFlow()

    init {
        viewModelScope.launch {
            val job1 = launch {
                loadData()
            }

            val job2 = launch {
                songServiceHandler.mediaState.collectLatest { mediaState ->
                    when (mediaState) {
                        MediaState.Initial -> _uiState.value = UIState.Initial
                        is MediaState.Buffering -> calculateProgressValue(mediaState.progress)
                        is MediaState.CurrentPlaying -> {
                            val selectedSongIndex = mediaState.mediaItemIndex
                            songList.forEachIndexed { index, song ->
                                song.setIsSelected(index == selectedSongIndex)
                            }
                            _currentSelectedSong.value = songList[selectedSongIndex]
                        }
                        is MediaState.Playing -> isPlaying = mediaState.isPlaying
                        is MediaState.Progress -> calculateProgressValue(mediaState.progress)
                        is MediaState.Ready -> {
                            duration = mediaState.duration
                            _uiState.value = UIState.Ready
                        }
                    }
                }
            }
            job1.join()
            job2.join()
        }
    }

    fun onUiEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when (uiEvents) {
            UIEvents.PlayPause -> songServiceHandler.onMediaEvents(MediaEvent.PlayPause)
            UIEvents.SeekToNext -> songServiceHandler.onMediaEvents(MediaEvent.SeekToNext)
            UIEvents.SeekToPrevious -> songServiceHandler.onMediaEvents(MediaEvent.SeekToPrevious)
            is UIEvents.SeekTo -> {
                songServiceHandler.onMediaEvents(
                    MediaEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }
            is UIEvents.SelectedSongChange -> {
                songServiceHandler.onMediaEvents(
                    MediaEvent.SelectedSongChange,
                    selectedSongIndex = uiEvents.index
                )
            }
            is UIEvents.UpdateProgress -> {
                songServiceHandler.onMediaEvents(
                    MediaEvent.UpdateProgress(uiEvents.newProgress)
                )
                progress = uiEvents.newProgress
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val song = repository.getAllSongs()
            songList = song
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        songList.map { song ->
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
        }.also {
            songServiceHandler.setMediaItemList(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
            else 0f
        progressString = formatDuration(currentProgress)
    }

    private fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
    }

    override fun onCleared() {
        viewModelScope.launch {
            songServiceHandler.onMediaEvents(MediaEvent.Stop)
        }
        super.onCleared()
    }

}

sealed class UIEvents {
    object PlayPause : UIEvents()
    object SeekToNext : UIEvents()
    object SeekToPrevious : UIEvents()
    data class SelectedSongChange(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}