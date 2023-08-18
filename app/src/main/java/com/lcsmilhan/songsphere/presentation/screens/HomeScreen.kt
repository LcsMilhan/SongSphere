@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
package com.lcsmilhan.songsphere.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.components.BottomPlayerTab
import com.lcsmilhan.songsphere.presentation.components.BottomSheetDialog
import com.lcsmilhan.songsphere.presentation.components.SongListItem
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: SongViewModel = hiltViewModel(),
    startService: () -> Unit
) {
    val fullScreenState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    val onBottomTabClick: () -> Unit = { scope.launch { fullScreenState.show() } }



    LaunchedEffect(true) {
        startService()
    }

    SongList(
        songs = viewModel.allSongs,
        selectedSong = viewModel.selectedSong,
        fullScreenState = fullScreenState,
        playerEvents = viewModel,
        onBottomTabClick = onBottomTabClick,
        playResourceProvider = playResourceProvider,
        durationString = durationString,
        progressString = progressString,
        progress = progress
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongList(
    songs: List<Song>,
    selectedSong: Song?,
    fullScreenState: ModalBottomSheetState,
    playerEvents: (SongEvent) -> Unit,
    onBottomTabClick: () -> Unit,
    playResourceProvider: () -> Int,
    durationString: String,
    progressString: String,
    progress: Float
) {
    ModalBottomSheetLayout(
        sheetContent = {
            if (selectedSong != null) {
                BottomSheetDialog(
                    selectedSong = selectedSong,
                    playerEvents = playerEvents,
                    playResourceProvider = playResourceProvider,
                    durationString = durationString,
                    progressString = progressString,
                    progress = progress
                )
            }
        },
        sheetState = fullScreenState,
        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        sheetElevation = 8.dp
    ) {
        Scaffold { paddingValues ->
            Box(
                Modifier.padding(top = paddingValues.calculateTopPadding())
            ) {
                Column {
                    LazyColumn(
                        Modifier.weight(1f),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        items(songs) {
                            SongListItem(
                                song = it,
                                onSongClick = { playerEvents.onSongClick(it) }
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = selectedSong != null,
                        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight })
                    ) {
                        BottomPlayerTab(selectedSong!!, playerEvents, onBottomTabClick)
                    }
                }
            }
        }
    }

}