package com.kivsw.phonerecorder.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.kivsw.phonerecorder.model.settings.ISettings
import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam
import com.kivsw.phonerecorder.os.AppReceiver
import io.reactivex.annotations.Nullable
import phonerecorder.kivsw.com.phonerecorder.R
import javax.inject.Inject

//import android.support.v4.app.NotificationCompat;

/**
 * Notification is used only to avoid killing application
 */
const val NOTIFICATION_CLICK_ACTION = "phonerecorder.kivsw.com.faithphonerecorder.ui.notification.AntiTaskKillerNotification.NOTIFICATION_CLICK_ACTION"
open class AntiTaskKillerNotification
@Inject
internal constructor(protected var context: Context, protected var settings: ISettings, notificationId: Int) {
    var notificationId = 0
        protected set
    var isVisible: Boolean = false
        protected set


    init {
        this.notificationId = notificationId
        isVisible = false
        initNotificationChannel(context)
    }



    @Nullable
    fun createNotification(): Notification {
        return createNotification(settings.antiTaskKillerNotification)
    }

    protected fun createNotification(param: AntiTaskKillerNotificationParam): Notification {

        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        val smallIconResourceId = getSmallIconId(param.iconNum)
        mBuilder.setSmallIcon(smallIconResourceId)
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.resources, smallIconResourceId))
        mBuilder.setOngoing(true)
        //mBuilder.setLargeIcon( BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_phonerecorder) );
        //mBuilder.setContentTitle(param.text);
        //mBuilder.setContentText("text");


        val intent = Intent(NOTIFICATION_CLICK_ACTION)
        intent.setClass(context, AppReceiver::class.java)
        mBuilder.setContentIntent(PendingIntent.getBroadcast(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT))

        return mBuilder.build()
    }

    fun show() {
        val param = settings.antiTaskKillerNotification
        if (!param.visible) return

        val n = createNotification(param)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // notificationId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, n)
        isVisible = true

    }

    //---------------------------------------------------------------------------------
    fun hide() {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationId)
        isVisible = false
    }

    private fun getSmallIconId(iconIndex: Int): Int {
        var iconIndex = iconIndex
        if (iconIndex < 0) iconIndex = 0
        if (iconIndex >= notificationSmallIcons.size) iconIndex = notificationSmallIcons.size - 1

        return notificationSmallIcons[iconIndex]
    }

    companion object {

        val CHANNEL_ID = NotificationShower.CHANNEL_ID
        private var mNotificationChannel:NotificationChannel?=null


        val notificationSmallIcons: IntArray
        val notificationIcons: IntArray

        init {
            notificationSmallIcons = IntArray(3)
            notificationSmallIcons[0] = R.drawable.ic_notification_small_horns
            notificationSmallIcons[1] = R.drawable.ic_notification_small_android
            notificationSmallIcons[2] = R.drawable.ic_notification_small_empty

            notificationIcons = IntArray(3)
            notificationIcons[0] = R.drawable.ic_notification_horns
            notificationIcons[1] = R.drawable.ic_notification_android
            notificationIcons[2] = R.drawable.ic_notification_empty
        }
    }

}
