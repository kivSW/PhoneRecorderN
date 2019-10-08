package com.kivsw.phonerecorder.os.jobs;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.model.task_executor.tasks.ITask;
import com.kivsw.phonerecorder.model.task_executor.tasks.ITaskProvider;
import com.kivsw.phonerecorder.os.MyApplication;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

//https://www.spiria.com/en/blog/mobile-development/hiding-foreground-services-notifications-in-android/
/**
 * this service is used just to indicate to the framework that we have some background work
 */

public class AppService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int lastStartId;
    private Map<String, Integer> activeTasks;
    @Inject protected ITaskProvider taskProvider;
    @Inject protected IJournal journal;
    final static String EXTRA_START="EXTRA_START";

    protected static AppService instance;

    public void onCreate() {
        super.onCreate();
        activeTasks = new HashMap<>();
        MyApplication.getComponent().inject(this);
        instance = this;
    };

    public void onDestroy()
    {
        instance = null;
        journal.journalAdd("AppService.onDestroy()");
        super.onDestroy();
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String taskId="";
        boolean start=false;
        journal.journalAdd("AppService.onStartCommand()", intent);

        lastStartId = startId;
        if(intent!=null) {
            taskId = intent.getAction();
            start = intent.getBooleanExtra(EXTRA_START, false);
        }


        ITask task=taskProvider.getTask(taskId);
        if(task!=null)
        {
            if(start)
            {
                if(task.startTask())
                  addTask(taskId);
            }
            else
            {
                task.stopTask();
                removeTask(taskId);
            }
        }

        startForegroundIfNecessary();
        stopIfNecessary(); // stops this service

        return START_STICKY;
    }

    protected void addTask(String action)
    {
        Integer count= activeTasks.get(action);
        int newCount;
        if(count!=null)
            newCount = count.intValue()+1;
        else
            newCount = 1;

        activeTasks.put(action, Integer.valueOf(newCount) );
    };

    protected void removeTask(String action)
    {
        Integer count= activeTasks.get(action);
        if(count!=null) {
            int newCount = count.intValue()-1;
            if(newCount<=0) activeTasks.remove(action);
            else activeTasks.put(action, Integer.valueOf(newCount) );
        }
    };

    private boolean isForeground=false;
    protected void startForegroundIfNecessary()
    {
        if(isForeground) return;
        if(activeTasks.isEmpty()) return;

        isForeground=true;
        if (startService(new Intent(this, ForegroundEnablingService.class)) == null)
            journal.journalAdd("can't start service ForegroundEnablingService");

    }
    protected void stopIfNecessary()
    {
        if(activeTasks.isEmpty())
        {
            isForeground = false;
            stopSelfResult(lastStartId);
            releaseWakeLock();
        }
    }

    private static PowerManager.WakeLock wl=null;
    protected static void acquireWakeLock(Context context)
    {
        if(wl==null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "phoneRecorder:appService");
            wl.acquire();
        }
    }
    protected static void releaseWakeLock()
    {
        if(wl!=null) {
            wl.release();
            wl = null;
        }
    }

    protected static void startService(Context context, String action, boolean start)
    {
        Intent intent=new Intent(context, AppService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_START, start);
        context.startService(intent);
        if(start)
            acquireWakeLock(context);
    }



    synchronized public static void startTask(Context context, String action)
    {
        startService(context, action, true);
    }
    synchronized public static void stopTask(Context context, String action)
    {
        startService(context, action, false);
    }


    /**
     * this is an auxiliary service to make AppService foreground
     * https://stackoverflow.com/questions/10962418/how-to-startforeground-without-showing-notification
     */
    public static class ForegroundEnablingService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (AppService.instance == null)
                throw new RuntimeException(AppService.class.getSimpleName() + " not running");

            //Set both services to foreground using the same notification id, resulting in just one notification
            startForeground(AppService.instance);
            startForeground(this);

            //Cancel this service's notification, resulting in zero notifications
            stopForeground(true);

            //Stop this service so we don't waste RAM.
            //Must only be called *after* doing the work or the notification won't be hidden.
            stopSelf();

            return START_NOT_STICKY;
        }

        //private static final int NOTIFICATION_ID = 10;

        private void startForeground(Service service) {
            Notification notification = new Notification.Builder(service).getNotification();
            int NOTIFICATION_ID=com.kivsw.phonerecorder.ui.notification.NotificationShowerModule.AUXIALIARY_NOTIFICATION_ID;
            service.startForeground(NOTIFICATION_ID, notification);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

}
