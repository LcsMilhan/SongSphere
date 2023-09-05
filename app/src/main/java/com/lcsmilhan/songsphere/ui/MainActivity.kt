package com.lcsmilhan.songsphere.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.lcsmilhan.songsphere.presentation.screens.HomeScreen
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import com.lcsmilhan.songsphere.presentation.viewmodel.UIEvents
import com.lcsmilhan.songsphere.service.player.SongService
import com.lcsmilhan.songsphere.ui.theme.SongSphereTheme
import dagger.hilt.android.AndroidEntryPoint


@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SongViewModel by viewModels()
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SongSphereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = viewModel.progress,
                        onProgress = { viewModel.onUiEvents(UIEvents.SeekTo(it)) },
                        isSongPlaying = viewModel.isPlaying,
                        currentPlayingSong = viewModel.currentSelectedSong,
                        songList = viewModel.songList,
                        onStart = { viewModel.onUiEvents(UIEvents.PlayPause) },
                        onItemClick = {
                            viewModel.onUiEvents(UIEvents.SelectedSongChange(it))
                            startService()
                        },
                        onNext = { viewModel.onUiEvents(UIEvents.SeekToNext) },
                        onPrevious = { viewModel.onUiEvents(UIEvents.SeekToPrevious) }
                    )
                }
            }
        }
    }
    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, SongService::class.java)
            startForegroundService(intent)
            isServiceRunning = true
        }
    }
}
