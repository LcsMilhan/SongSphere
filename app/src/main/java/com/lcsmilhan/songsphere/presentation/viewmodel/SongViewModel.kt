package com.lcsmilhan.songsphere.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.domain.repository.SongRepository
import com.lcsmilhan.songsphere.service.MediaState
import com.lcsmilhan.songsphere.service.PlayerEvent
import com.lcsmilhan.songsphere.service.SongServiceHandler
import com.lcsmilhan.songsphere.utils.collectPlayerState
import com.lcsmilhan.songsphere.utils.formatTime
import com.lcsmilhan.songsphere.utils.launchPlaybackStateJob
import com.lcsmilhan.songsphere.utils.resetSongs
import com.lcsmilhan.songsphere.utils.toMediaItemList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SongViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val repository: SongRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SongState>(SongState.Initial)
    val state = _state.asStateFlow()

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs = _allSongs.asStateFlow()

    private val _isSongPlay = MutableStateFlow(false)
    var isSongPlay = _isSongPlay.asStateFlow()

    private val _selectedSong = MutableStateFlow<Song?>(null)
    var selectedSong = _selectedSong.asStateFlow()

    private var _selectedSongIndex = MutableStateFlow(-1)
    var selectedSongIndex = _selectedSongIndex.asStateFlow()

    private var _progress = MutableStateFlow(0F)
    val progress = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var _progressString = MutableStateFlow("00:00")
    val progressString = _progressString.asStateFlow()

    private var _songTransitions = MutableStateFlow(false)
    val songTransitions = _songTransitions.asStateFlow()

    private var _nextTrackAvailable = MutableStateFlow(false)
    val nextTrackAvailable = _nextTrackAvailable.asStateFlow()

    private var _previousTrackAvailable = MutableStateFlow(false)
    val previousTrackAvailable = _previousTrackAvailable.asStateFlow()

    private val _playbackState = MutableStateFlow(SongState.PlaybackState(0L, 0L))
    val playbackState = _playbackState.asStateFlow()

    private var playbackStateJob: Job? = null

    init {
        viewModelScope.launch {
            _allSongs.value = repository.getAllSongs()
            songServiceHandler.addMediaItemList(_allSongs.value.toMediaItemList())
            observePlayerState()

            songServiceHandler.mediaState.collect { mediaState ->
                when (mediaState) {
                    is MediaState.Buffering -> {
                        calculateProgressValue(mediaState.progress)
                    }
                    MediaState.Initial -> _state.value = SongState.Initial
                    is MediaState.Playing -> _isSongPlay.value = mediaState.isPlaying
                    is MediaState.Progress -> {
                        if (_duration.value > 0) {
                            calculateProgressValue(mediaState.progress)
                        }
                    }
                    is MediaState.Ready -> {
                        _duration.value = mediaState.duration
                        _state.value = SongState.Ready
                    }
                    MediaState.End -> {
                        _state.value = SongState.Ended
                    }
                    MediaState.Next -> {
                        _state.value = SongState.Next
                    }
                }
            }
        }
    }


    fun onEvent(songEvent: SongEvent) {
        viewModelScope.launch {
            when (songEvent) {
                SongEvent.Backward -> songServiceHandler.onPlayerEvent(PlayerEvent.Backward)
                SongEvent.Forward -> songServiceHandler.onPlayerEvent(PlayerEvent.Forward)
                SongEvent.PlayPause -> songServiceHandler.onPlayerEvent(PlayerEvent.PlayPause)
                SongEvent.Next -> songServiceHandler.onPlayerEvent(PlayerEvent.Next)
                SongEvent.Previous -> songServiceHandler.onPlayerEvent(PlayerEvent.Previous)
                SongEvent.Stop -> songServiceHandler.onPlayerEvent(PlayerEvent.Stop)
                is SongEvent.UpdateProgress -> {
                    _progress.value = songEvent.newProgress
                    songServiceHandler.onPlayerEvent(
                        PlayerEvent.UpdateProgress(
                            songEvent.newProgress
                        )
                    )
                }
            }
        }
    }

    private fun onSongSelected(index: Int) {
        if (_selectedSongIndex.value == -1) _isSongPlay.value = true
        if (_selectedSongIndex.value == -1 || _selectedSongIndex.value != index) {
            _allSongs.value.resetSongs()
            _selectedSongIndex.value = index
            setUpSong()
        }
    }

    private fun setUpSong() {
        songServiceHandler.setUpSong(
            _selectedSongIndex.value,
            _isSongPlay.value
        )
    }

    private fun updateState(state: MediaState) {
        if (_selectedSongIndex.value != -1) {
            _isSongPlay.value = state == MediaState.Playing(true)
            _allSongs.value[_selectedSongIndex.value].state = state
            _allSongs.value[_selectedSongIndex.value].isSelected = true
            _selectedSong.value = null
            _selectedSong.value = _allSongs.value[_selectedSongIndex.value]

            updatePlaybackState(state)
            if (state == MediaState.Next) {
                songServiceHandler.nextSong
            }
            if (state == MediaState.End) onSongSelected(0)
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

    private fun calculateProgressValue(currentProgress: Long) {
        _progress.value =
            if (currentProgress > 0) (currentProgress.toFloat() / _duration.value) else 0f
        _progressString.value = currentProgress.formatTime()
    }

}