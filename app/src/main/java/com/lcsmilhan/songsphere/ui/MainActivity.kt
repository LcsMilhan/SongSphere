package com.lcsmilhan.songsphere.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.lcsmilhan.songsphere.presentation.screens.HomeScreen
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import com.lcsmilhan.songsphere.service.player.SongService
import com.lcsmilhan.songsphere.ui.theme.SongSphereTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess


@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SongSphereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreen(viewModel, ::startService)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    private fun startService() {
        if (!viewModel.isServiceRunning.value) {
            val intent = Intent(this, SongService::class.java)
            startForegroundService(intent)
            viewModel.isServiceRunning.value = true
        }
    }

    private fun stopService() {
        if (viewModel.isServiceRunning.value) {
            val intent = Intent(this, SongService::class.java)
            stopService(intent)
            viewModel.isServiceRunning.value = false
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
}
