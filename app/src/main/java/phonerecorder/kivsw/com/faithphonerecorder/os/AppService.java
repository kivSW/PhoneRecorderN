package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.ITask;

/**
 * this service is used just to indicate to the framework that we have some background work
 */

public class AppService extends android.app.Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    int lastStartId;
    private Map<String, Integer> activeActions;
    final static String EXTRA_START="EXTRA_START";

    public void onCreate() {
        super.onCreate();
        activeActions = new HashMap<>();
    };

    public void onDestroy()
    {
        super.onDestroy();
    };


    ITask getTask(String task)
    {
        switch(task)
        {
            case TASK_CALL_RECORDING:
                return MyApplication.getComponent().getCallRecorder();
            case TASK_SEND_FILES:
                return MyApplication.getComponent().getRecordSender();
        };

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastStartId = startId;
        String action = intent.getAction();
        boolean start=intent.getBooleanExtra(EXTRA_START,false);


        ITask task=getTask(action);
        if(task!=null)
        {
            if(start)
            {
                task.startTask();
                addTask(action);
            }
            else
            {
                task.stopTask();
                removeTask(action);
            }
        }
        else
            removeTask(action);


        return START_NOT_STICKY;
    }

    protected void addTask(String action)
    {
        Integer count=activeActions.get(action);
        int newCount;
        if(count!=null)
            newCount = count.intValue()+1;
        else
            newCount = 1;

        activeActions.put(action, Integer.valueOf(newCount) );
    };

    protected void removeTask(String action)
    {
        Integer count=activeActions.get(action);
        if(count!=null) {
            int newCount = count.intValue()-1;
            if(newCount<=0) activeActions.remove(action);
            else activeActions.put(action, Integer.valueOf(newCount) );
        }

        if(activeActions.isEmpty())
        {
            stopSelfResult(lastStartId);
        }
    };

    protected static void startService(Context context, String action, boolean start)
    {
        Intent intent=new Intent(context, AppService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_START, start);
        context.startService(intent);
    }

    final static public String TASK_CALL_RECORDING ="TASK_CALL_RECORDING";
    final static public String TASK_SEND_FILES ="TASK_SEND_FILES";

    synchronized public static void startTask(Context context, String action)
    {
        startService(context, action, true);
    }
    synchronized public static void stopTask(Context context, String action)
    {
        startService(context, action, false);
    }

}
