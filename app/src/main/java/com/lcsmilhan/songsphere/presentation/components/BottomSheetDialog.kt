package com.lcsmilhan.songsphere.presentation.components


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.service.PlaybackState
import com.lcsmilhan.songsphere.service.PlayerEvents
import com.lcsmilhan.songsphere.utils.formatTime
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BottomSheetDialog(
    selectedSong: Song,
    playerEvents: PlayerEvents,
    playbackState: StateFlow<PlaybackState>

) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        SongInfo(
            songImage = selectedSong.imageUrl,
            songName = selectedSong.songName,
            artist = selectedSong.artist
        )
        Log.d("bottomSheetScreen", "SongInfo(songName = ${selectedSong.songName})")
        SongProgressSlider(playbackState = playbackState) {
            playerEvents.onSeekBarPositionChanged(it)
        }
        SongControls(
            selectedSong = selectedSong,
            onPreviousClick = playerEvents::onPreviousClick,
            onPlayPauseClick = playerEvents::onPlayPauseClick,
            onNextClick = playerEvents::onNextClick
        )
        Log.w("bottomSheetScreen", "SongControls(selectedSong = ${selectedSong.artist}")
    }
}

@Composable
fun SongInfo(
    songImage: String,
    songName: String,
    artist: String
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        SongImage(
            songImage = songImage,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
    Text(
        text = songName,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    )
    Text(
        text = artist,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
fun SongProgressSlider(
    playbackState: StateFlow<PlaybackState>,
    onSeekBarPositionChanged: (Long) -> Unit
) {
    val playbackStateValue = playbackState.collectAsState(
        initial = PlaybackState(0L, 0L)
    ).value
    var currentMediaProgress = playbackStateValue.currentPlaybackPosition.toFloat()
    var currentPosTemp by rememberSaveable { mutableStateOf(0f) }

    Slider(
        value = if (currentPosTemp == 0f) currentMediaProgress else currentPosTemp,
        onValueChange = { currentPosTemp = it },
        onValueChangeFinished = {
            currentMediaProgress = currentPosTemp
            currentPosTemp = 0f
            onSeekBarPositionChanged(currentMediaProgress.toLong())
        },
        valueRange = 0f..playbackStateValue.currentSongDuration.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = playbackStateValue.currentPlaybackPosition.formatTime(),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = playbackStateValue.currentSongDuration.formatTime(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SongControls(
    selectedSong: Song,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreviousIcon(
            onClick = onPreviousClick,
            isBottomTab = false
        )
        PlayPauseIcon(
            selectedSong = selectedSong,
            onClick = onPlayPauseClick,
            isBottomTab = false
        )
        NextIcon(
            onClick = onNextClick,
            isBottomTab = false
        )
    }
}