package com.lcsmilhan.songsphere

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SongApplication : Application() {
    companion object {
        val ACTION_NEXT = "actionNext"
        val ACTION_PREVIOUS = "actionPrevious"
    }
}