package com.lcsmilhan.songsphere.presentation.composable

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
import com.lcsmilhan.songsphere.presentation.viewmodel.UIEvents

@Composable
fun BottomPlayerTab(
    selectedSong: Song,
    onUIEvents: (UIEvents) -> Unit,
    onBottomTabClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = { onBottomTabClick() })
            .padding(7.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongImage(songImage = selectedSong.imageUrl, modifier = Modifier.size(50.dp))
            SongName(songName = selectedSong.songName, modifier = Modifier.weight(1f))
            PreviousIcon(onClick = { onUIEvents(UIEvents.SeekToPrevious) }, isBottomTab = true)
            PlayPauseIcon(onClick = { onUIEvents(UIEvents.PlayPause) }, isBottomTab = true)
            NextIcon(onClick = { onUIEvents(UIEvents.SeekToNext) }, isBottomTab = true)
        }
    }
}