package com.lcsmilhan.songsphere.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lcsmilhan.songsphere.SongApplication.Companion.ACTION_NEXT
import com.lcsmilhan.songsphere.SongApplication.Companion.ACTION_PREVIOUS
import com.lcsmilhan.songsphere.service.player.SongService
import com.lcsmilhan.songsphere.service.player.SongServiceHandler
import javax.inject.Inject

class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var songServiceHandler: SongServiceHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        val actionName = intent!!.action
        val serviceIntent = Intent(context, SongService::class.java)
        if (actionName != null) {
            when (actionName) {
                ACTION_NEXT -> {
                    serviceIntent.putExtra("actionName", "next")
                    context?.startService(serviceIntent)
                }
                ACTION_PREVIOUS -> {
                    serviceIntent.putExtra("actionName", "previous")
                    context?.startService(serviceIntent)
                }
            }
        }
    }
}