package com.kivsw.phonerecorder.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import phonerecorder.kivsw.com.phonerecorder.R

private var hasBeenInitialized=false
internal fun initNotificationChannel(context:Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if(hasBeenInitialized) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = ""
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(AntiTaskKillerNotification.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }
    hasBeenInitialized=true
}