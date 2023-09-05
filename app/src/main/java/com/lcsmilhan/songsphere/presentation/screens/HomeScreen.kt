@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
package com.lcsmilhan.songsphere.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.lcsmilhan.songsphere.R
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    progress: Float,
    onProgress: (Float) -> Unit,
    isSongPlaying: Boolean,
    currentPlayingSong: StateFlow<Song?>,
    songList: List<Song>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomBarPlayer(
                progress = progress,
                onProgress = onProgress,
                song = currentPlayingSong.value ?: run { Song() },
                onStart = onStart,
                onNext = onNext,
                onPrevious = onPrevious,
                isSongPlaying = isSongPlaying
            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(songList) { index, song ->
                SongItem(
                    song = song,
                    onItemClick = { onItemClick(index) }
                )
            }
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SongItem(
    song: Song,
    onItemClick: () -> Unit,
    viewModel: SongViewModel = hiltViewModel()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                onItemClick()
            },
        backgroundColor = if (song.isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp)
        ) {
            GlideImage(
                model = song.imageUrl,
                contentDescription = "SongImage",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = song.songName,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            if (viewModel.isPlaying && song.isSelected) LottieAudioWave()
            Spacer(modifier = Modifier.size(8.dp))
        }

    }
}

@Composable
fun LottieAudioWave() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.audio_wave))
    LottieAnimation(
        composition = composition,
        iterations = Int.MAX_VALUE,
        modifier = Modifier.size(size = 64.dp)
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BottomBarPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    song: Song,
    isSongPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    BottomAppBar(
        backgroundColor = MaterialTheme.colorScheme.tertiary,
        content = {
            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    GlideImage(
                        model = song.imageUrl,
                        contentDescription = "SongImage",
                        modifier = Modifier.size(46.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtistInfo(
                            song = song,
                            modifier = Modifier.weight(1f),
                        )
                        MediaPlayerController(
                            isAudioPlaying = isSongPlaying,
                            onStart = onStart,
                            onNext = onNext,
                            onPrevious = onPrevious
                        )
                        Slider(
                            value = progress,
                            onValueChange = { onProgress(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.inversePrimary,
                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(0.5f)
                            )
                        )
                    }
                }

            }
        }
    )
}


@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.previous),
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    onPrevious()
                },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.inversePrimary
        )
        PlayerIconItem(
            icon = if (isAudioPlaying) painterResource(R.drawable.pause)
            else painterResource(R.drawable.play)
        ) {
            onStart()
        }
        Icon(
            painter = painterResource(R.drawable.next),
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    onNext()
                },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.inversePrimary
        )
    }

}

@Composable
fun ArtistInfo(
    modifier: Modifier = Modifier,
    song: Song,
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = painterResource(R.drawable.holder),
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {}
        Spacer(modifier = Modifier.size(4.dp))
        Column {
            Text(
                text = song.songName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = song.artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PlayerIconItem(
    icon: Painter,
    borderStroke: BorderStroke? = null,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp),
            contentAlignment = Alignment.Center,

            ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier
                    .size(35.dp)
            )
        }
    }
}