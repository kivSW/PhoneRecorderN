/** this class shows and hides Notofication
 *
 */
package com.kivsw.phonerecorder.ui.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.kivsw.phonerecorder.model.settings.ISettings
import com.kivsw.phonerecorder.ui.main_activity.MainActivity
import phonerecorder.kivsw.com.phonerecorder.R
import javax.inject.Inject

open class NotificationShower
@Inject
internal constructor(protected var context: Context, internal var settings: ISettings, internal var notificationId: Int) {

    init {
        initNotificationChannel(context)
    }

    //---------------------------------------------------------------------------------
    //https://kotlinlang.org/docs/reference/java-interop.html#finalize
    @Throws(Throwable::class)
    fun finalize() {
        //super.finalize() // no needs to invoke it in Kotlin
        hide()
    }

    //---------------------------------------------------------------------------------
    fun createNotification(title:String, text: String, iconId:Int, largeIcon: Bitmap,
                                     percent:Int=-1, pendingIntent:PendingIntent?=null ): Notification
    {
        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        mBuilder.setSmallIcon(iconId)
        mBuilder.setLargeIcon(largeIcon)
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(text)
        mBuilder.setOngoing(true)

        if (percent >= 0 && percent <= 100)
            mBuilder.setProgress(100, percent, false)

        pendingIntent?.let{
            mBuilder.setContentIntent(pendingIntent)
        }

        return mBuilder.build()
    }
    //---------------------------------------------------------------------------------
    // informs the user about this SW
    fun show(text: String, openActivity: Boolean) {
        show(text, -1, openActivity)
    }

    //---------------------------------------------------------------------------------
    fun show(text: String, percents: Int, openActivity: Boolean) {
        if (settings.hiddenMode) return

        val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val n=createNotification(context.getText(R.string.app_name).toString(),
                text,
                R.drawable.ic_notification_small_horns,
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_phonerecorder),
                percents,
                PendingIntent.getActivity(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT))

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // notificationId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, n)
    }

    //---------------------------------------------------------------------------------
    fun hide() {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationId)
    }

    companion object {
        var CHANNEL_ID = "com.kivsw.phonerecorder.notification_channel"
    }

}

