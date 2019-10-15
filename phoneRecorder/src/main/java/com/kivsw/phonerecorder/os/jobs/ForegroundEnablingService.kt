package com.kivsw.phonerecorder.os.jobs

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * this is an auxiliary service to make AppService invisible foreground
 * https://stackoverflow.com/questions/10962418/how-to-startforeground-without-showing-notification
 */
class ForegroundEnablingService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Set both services to foreground using the same notification id, resulting in just one notification
        AppService.instance?.let{
            val notificationId = it.serviceNotification.notificationId
            val notification = it.createForegroundNotification()
            it.startForeground(notificationId, notification)
            startForeground(notificationId, notification)
            //Cancel this service's notification, resulting in zero notifications
            stopForeground(true)
        }

        //Stop this service so we don't waste RAM.
        //Must only be called *after* doing the work or the notification won't be hidden.
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}