package com.lcsmilhan.songsphere.service.player

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lcsmilhan.songsphere.service.notification.SongNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class SongService : MediaSessionService() {

    @Inject
    lateinit var notificationManager: SongNotificationManager

    @Inject
    lateinit var mediaSession: MediaSession
    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.startNotificationService(
            this,
            mediaSession
        )
        return super.onStartCommand(intent, flags, startId)
    }

    @UnstableApi
    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession = mediaSession

}