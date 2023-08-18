package com.lcsmilhan.songsphere.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lcsmilhan.songsphere.R
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel

@Composable
fun SongListItem(
    song: Song,
    onSongClick: () -> Unit,
    viewModel: SongViewModel = hiltViewModel()
) {
    val bgColor = if (song.isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (song.isSelected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val isPlaying by viewModel.isPlaying.collectAsState()

    Row(
        Modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onSongClick)
    ) {
        SongImage(
            songImage = song.imageUrl,
            modifier = Modifier.size(64.dp)
        )
        Column(
            Modifier
                .padding(start = 10.dp, end = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = song.songName,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )
        }
        if (isPlaying) LottieAudioWave()
    }
}

@Composable
fun LottieAudioWave() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.audio_wave))
    LottieAnimation(
        composition = composition,
        iterations = Int.MAX_VALUE,
        modifier = Modifier.size(64.dp)
    )
}