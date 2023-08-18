package com.lcsmilhan.songsphere.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lcsmilhan.songsphere.domain.model.Song


@Composable
fun BottomPlayerTab(
    selectedSong: Song,
    playResourceProvider: () -> Int,
    playerEvents: (SongEvent) -> Unit,
    onBottomTabClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onBottomTabClick)
            .padding(15.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongImage(
                songImage = selectedSong.imageUrl,
                modifier = Modifier.size(70.dp)
            )
            SongName(
                songName = selectedSong.songName,
                modifier = Modifier.weight(1f)
            )
            PreviousIcon(
                onClick =  {
                    playerEvents(SongEvent.Next)
                },
                isBottomTab = true
            )
            PlayPauseIcon(
                onClick = {
                    playerEvents(SongEvent.PlayPause)
                },
                playResourceProvider = playResourceProvider,
                isBottomTab = true
            )
            NextIcon(
                onClick = {
                    playerEvents(SongEvent.Next)
                },
                isBottomTab = true
            )
        }
    }
}