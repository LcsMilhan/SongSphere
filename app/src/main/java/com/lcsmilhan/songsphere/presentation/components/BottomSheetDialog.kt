package com.lcsmilhan.songsphere.presentation.components

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lcsmilhan.songsphere.domain.model.Song

@Composable
fun BottomSheetDialog(
    selectedSong: Song,
    playResourceProvider: () -> Int,
    playerEvents: (SongEvent) -> Unit,
    durationString: String,
    progressString: String,
    progress: Float
) {

    val newProgressValue = remember { mutableStateOf(0f) }
    val useNewProgressValue = remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth()
    ) {
        SongInfo(
            songImage = selectedSong.imageUrl,
            songName = selectedSong.songName,
            artist = selectedSong.artist
        )
        Slider(
            value = if (useNewProgressValue.value) newProgressValue.value else progress,
            onValueChange = { newValue ->
                useNewProgressValue.value = true
                newProgressValue.value = newValue
                playerEvents(SongEvent.UpdateProgress(newProgress = newValue))
            },
            onValueChangeFinished = {
                useNewProgressValue.value = false
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = progressString,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = durationString,
                style = MaterialTheme.typography.bodySmall
            )
        }
        SongControls(
            playResourceProvider = playResourceProvider,
            onPreviousClick = {
                playerEvents(SongEvent.Previous)
            },
            onPlayPauseClick = {
                playerEvents(SongEvent.PlayPause)
            },
            onNextClick = {
                playerEvents(SongEvent.Next)
            }
        )
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
fun SongControls(
    playResourceProvider: () -> Int,
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
           onClick = onPlayPauseClick,
           playResourceProvider = playResourceProvider,
           isBottomTab = false
       )
       NextIcon(
           onClick = onNextClick,
           isBottomTab = false
       )
   }
}