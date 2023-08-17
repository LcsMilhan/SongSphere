package com.lcsmilhan.songsphere.service.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lcsmilhan.songsphere.R


@UnstableApi
class SongNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.currentMediaItem?.mediaMetadata?.title ?: ""
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence {
        return player.currentMediaItem?.mediaMetadata?.artist ?: ""
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .placeholder(R.drawable.holder)
            .diskCacheKey(player.currentMediaItem?.mediaId)
            .diskCachePolicy(CachePolicy.ENABLED)
            .data(player.mediaMetadata.artworkUri)
            .target(
                onStart = {
                },
                onSuccess = { result ->
                    callback.onBitmap(result.toBitmap())
                },
                onError = {
                    callback.onBitmap(
                        (AppCompatResources.getDrawable(
                            context,
                            R.drawable.holder
                        ) as BitmapDrawable).bitmap
                    )
                }
            )
            .build()
        ImageLoader(context).enqueue(request)
        return null
    }
}

