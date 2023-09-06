package com.lcsmilhan.songsphere.presentation.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.lcsmilhan.songsphere.R
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SongImage(
    songImage: String,
    modifier: Modifier
) {
    GlideImage(
        model = songImage,
        contentDescription = "SongImage",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(8.dp))
    )
}

@Composable
fun SongName(
    songName: String,
    modifier: Modifier
) {
    Text(
        text = songName,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.padding(start = 16.dp, end = 8.dp)
    )
}

@Composable
fun PreviousIcon(
    onClick: () -> Unit,
    isBottomTab: Boolean,
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.previous),
            contentDescription = "Previous Icon",
            tint = if (isBottomTab) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun PlayPauseIcon(
    onClick: () -> Unit,
    isBottomTab: Boolean,
    viewModel: SongViewModel = hiltViewModel()
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(
                if (viewModel.isPlaying) R.drawable.pause else R.drawable.play
            ),
            contentDescription = "Play/Pause Icon",
            tint = if (isBottomTab) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun NextIcon(
    onClick: () -> Unit,
    isBottomTab: Boolean
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.next),
            contentDescription = "Next Icon",
            tint = if (isBottomTab) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(30.dp)
        )
    }
}