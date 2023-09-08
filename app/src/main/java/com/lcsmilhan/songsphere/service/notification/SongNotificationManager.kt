package com.lcsmilhan.songsphere.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.lcsmilhan.songsphere.R
import com.lcsmilhan.songsphere.common.Constants.NOTIFICATION_CHANNEL_ID
import com.lcsmilhan.songsphere.common.Constants.NOTIFICATION_CHANNEL_NAME
import com.lcsmilhan.songsphere.common.Constants.NOTIFICATION_ID
import com.lcsmilhan.songsphere.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SongNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer
) {
    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)
    private lateinit var playerNotificationManager: PlayerNotificationManager

    @UnstableApi
    private var notificationListener: PlayerNotificationManager.NotificationListener =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                super.onNotificationCancelled(notificationId, dismissedByUser)
            }

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                super.onNotificationPosted(notificationId, notification, ongoing)
            }
        }

    init {
        createNotificationChannel()
    }

    @UnstableApi
    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ) {
        buildNotification(mediaSession)
        startForegroundNotification(mediaSessionService)
    }

    @UnstableApi
    private fun buildNotification(mediaSession: MediaSession) {
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        ).setMediaDescriptionAdapter(
            SongNotificationAdapter(
                context = context,
                pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    FLAG_IMMUTABLE
                )
            )
        ).setSmallIconResourceId(R.drawable.wavesound).build().also {
            it.setUseNextAction(true)
            it.setUsePreviousAction(true)
            it.setUseRewindActionInCompactView(false)
            it.setUseFastForwardActionInCompactView(false)
            it.setPriority(NotificationCompat.PRIORITY_LOW)
            it.setMediaSessionToken(mediaSession.sessionCompatToken)
            it.setPlayer(player)
        }
    }

    private fun startForegroundNotification(mediaSessionService: MediaSessionService) {
        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}