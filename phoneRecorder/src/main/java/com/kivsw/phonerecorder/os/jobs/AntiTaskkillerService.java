package com.kivsw.phonerecorder.os.jobs;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.os.MyApplication;
import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

import javax.inject.Inject;

/**
 * this is en empty foreground service that does nothing
 * it's meant to keep Android task-killer off this app
 */
public class AntiTaskkillerService  extends android.app.Service {
        @Inject AntiTaskKillerNotification antiTaskKillerNotification;
        @Inject ISettings settings;
        private int lastStartId;

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public void onCreate() {
            super.onCreate();
            MyApplication.getComponent().inject(this);
        };

        public void onDestroy()
        {
            super.onDestroy();
        };


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            lastStartId = startId;
            String taskId=START_SERVICE;
            int iconNum=0;

            if(intent!=null) {
                taskId = intent.getAction();
                iconNum = intent.getIntExtra(ICON_NUMBER, 0);
            }
            if(taskId==null) taskId=STOP_SERVICE;

            if(taskId.equals(START_SERVICE))
            {
                AntiTaskKillerNotificationParam param=new AntiTaskKillerNotificationParam(true, iconNum);
                startForeground(antiTaskKillerNotification.getNotificationId(), antiTaskKillerNotification.createNotification(param));
            }
            else
            {
                stopSelf();
            }

            return START_REDELIVER_INTENT;
        }


        static private String START_SERVICE="AntiTaskkillerService.START_SERVICE",
                STOP_SERVICE="AntiTaskkillerService.STOP_SERVICE",
                ICON_NUMBER="AntiTaskkillerService.ICON_INDEX";
        protected static void doStartService(Context context, boolean start, int iconNum)
        {
            Intent intent=new Intent(context, AntiTaskkillerService.class);

            intent.putExtra(ICON_NUMBER, iconNum);

            if(start)  intent.setAction(START_SERVICE);
            else    intent.setAction(STOP_SERVICE);

            context.startService(intent);
        }



        synchronized public static void start(Context context, int iconNum)
        {
            doStartService(context, true, iconNum);
        }
        synchronized public static void stop(Context context)
        {
            doStartService(context, false, 0);
        }


}
