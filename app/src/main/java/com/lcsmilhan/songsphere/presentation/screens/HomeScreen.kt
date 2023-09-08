package com.lcsmilhan.songsphere.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcsmilhan.songsphere.domain.model.Song
import com.lcsmilhan.songsphere.presentation.composable.BottomPlayerTab
import com.lcsmilhan.songsphere.presentation.composable.BottomSheetDialog
import com.lcsmilhan.songsphere.presentation.composable.SongListItem
import com.lcsmilhan.songsphere.presentation.viewmodel.SongViewModel
import com.lcsmilhan.songsphere.presentation.viewmodel.UIEvents
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: SongViewModel = hiltViewModel(),
    startService: () -> Unit
) {
    startService()

    val fullScreenState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    val onBottomTabClick: () -> Unit = { scope.launch { fullScreenState.show() } }

    val selectedSong by viewModel.currentSelectedSong.collectAsStateWithLifecycle()

    SongList(
        songs = viewModel.songList,
        selectedSong = selectedSong,
        fullScreenState = fullScreenState,
        onUIEvents = viewModel::onUiEvents,
        onBottomTabClick = onBottomTabClick
    )

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongList(
    songs: List<Song>,
    selectedSong: Song?,
    fullScreenState: ModalBottomSheetState,
    onUIEvents: (UIEvents) -> Unit,
    onBottomTabClick: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = {
            if (selectedSong != null) BottomSheetDialog(
                selectedSong = selectedSong,
                onUIEvents = onUIEvents
            )
        },
        sheetState = fullScreenState,
        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        sheetElevation = 8.dp,
    ) {
        Scaffold { paddingValues ->  
            Box(
                Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
               Column {
                   LazyColumn(
                       Modifier.weight(1f),
                       contentPadding = PaddingValues(5.dp)
                   ) {
                       itemsIndexed(songs) { index, song -> 
                           SongListItem(
                               song = song,
                               onSongClick = { onUIEvents(UIEvents.SelectedSongChange(index)) }
                           )
                       }
                   }
                   AnimatedVisibility(
                       visible = selectedSong != null,
                       enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight })
                   ) {
                       BottomPlayerTab(
                           selectedSong = selectedSong!!,
                           onUIEvents = onUIEvents,
                           onBottomTabClick = onBottomTabClick
                       )
                   }
               } 
            }
        }
    }
}