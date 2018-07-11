package com.kivsw.phonerecorder.ui.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.os.AppReceiver;

import javax.inject.Inject;

import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Notification is used only to avoid killing application
 */

public class AntiTaskKillerNotification {
    public final static String NOTIFICATION_CLICK_ACTION="phonerecorder.kivsw.com.faithphonerecorder.ui.notification.AntiTaskKillerNotification.NOTIFICATION_CLICK_ACTION";
    protected int notificationId=0;
    protected Context context;
    protected ISettings settings;
    protected boolean isVisible;

    //---------------------------------------------------------------------------------
    @Inject
    AntiTaskKillerNotification(Context context, ISettings settings, int notificationId)
    {
        this.notificationId = notificationId;
        this.context = context;
        this.settings = settings;
        isVisible=false;
    };

    public void show( )
    {
        AntiTaskKillerNotificationParam param = settings.getAntiTaskKillerNotification();

        if(!param.visible) return;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        int smallIconResourceId=getSmallIconId(param.iconNum);
        mBuilder.setSmallIcon(smallIconResourceId);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),smallIconResourceId));
        mBuilder.setOngoing(true);
        //mBuilder.setLargeIcon( BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_phonerecorder) );
        //mBuilder.setContentTitle(param.text);
        //mBuilder.setContentText("text");


        Intent intent=new Intent(NOTIFICATION_CLICK_ACTION);
        intent.setClass(context, AppReceiver.class);
        mBuilder.setContentIntent(PendingIntent.getBroadcast(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT ));

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
        isVisible=true;

    }
    public boolean isVisible()
    {
        return isVisible;
    }
    //---------------------------------------------------------------------------------
    public void hide()
    {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);
        isVisible=false;
    }


    static public final int notificationSmallIcons[],
                  notificationIcons[];
    static{
        notificationSmallIcons = new int[3];
        notificationSmallIcons[0]=R.drawable.ic_notification_small_horns;
        notificationSmallIcons[1]=R.drawable.ic_notification_small_android;
        notificationSmallIcons[2]=R.drawable.ic_notification_small_empty;

        notificationIcons = new int[3];
        notificationIcons[0]=R.drawable.ic_notification_horns;
        notificationIcons[1]=R.drawable.ic_notification_android;
        notificationIcons[2]=R.drawable.ic_notification_empty;
    }

    private int getSmallIconId(int iconIndex)
    {
        if(iconIndex<0)iconIndex=0;
        if(iconIndex>=notificationSmallIcons.length) iconIndex=notificationSmallIcons.length-1;

        return notificationSmallIcons[iconIndex];
    }

}
