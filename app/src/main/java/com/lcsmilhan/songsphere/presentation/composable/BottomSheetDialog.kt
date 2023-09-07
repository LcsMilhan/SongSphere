package com.lcsmilhan.songsphere.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import com.lcsmilhan.songsphere.presentation.viewmodel.UIEvents

@Composable
fun BottomSheetDialog(
    selectedSong: Song,
    onUIEvents: (UIEvents) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.5f))
    ) {
        SongInfo(
            songImage = selectedSong.imageUrl,
            songName = selectedSong.songName,
            songArtist = selectedSong.artist
        )
        SongProgressSlider(onUIEvents = onUIEvents)
        SongControls(
            onPreviousClick = { onUIEvents(UIEvents.SeekToPrevious) },
            onPlayPauseClick = { onUIEvents(UIEvents.PlayPause) },
            onNextClick = { onUIEvents(UIEvents.SeekToNext) }
        )
    }
}

@Composable
fun SongInfo(
    songImage: String,
    songName: String,
    songArtist: String
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(MaterialTheme.colorScheme.primary.copy(0.6f))
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
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
    Text(
        text = songArtist,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
fun SongProgressSlider(
    viewModel: SongViewModel = hiltViewModel(),
    onUIEvents: (UIEvents) -> Unit,
) {
    val newProgressValue = remember { mutableStateOf(0f) }
    val useNewProgressValue = remember { mutableStateOf(false) }

    Slider(
        value = if (useNewProgressValue.value) newProgressValue.value else viewModel.progress,
        onValueChange = { newValue ->
            useNewProgressValue.value = true
            newProgressValue.value = newValue
            onUIEvents(UIEvents.SeekTo(newValue))
        },
        onValueChangeFinished = { useNewProgressValue.value = false },
        valueRange = 0f..100f,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary.copy(0.7f)
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = viewModel.progressString, style = MaterialTheme.typography.labelLarge)
        Text(text = viewModel.duration.formatTime(), style = MaterialTheme.typography.labelLarge)
    }
}

fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun SongControls(
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
        PreviousIcon(onClick = onPreviousClick, isBottomTab = false)
        PlayPauseIcon(onClick = onPlayPauseClick, isBottomTab = false)
        NextIcon(onClick = onNextClick, isBottomTab = false)
    }
}