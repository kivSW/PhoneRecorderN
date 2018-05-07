package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ivan on 5/7/18.
 */

public class WatchdogTimer {
    public final static String ACTION_WATCHDOG_TIMER = "WatchdogTimer.ACTION_WATCHDOG_TIMER";

    private static PendingIntent getAlarmIntent(Context context)
    {
        Intent intent;
        PendingIntent alarmIntent;
        intent = new Intent(); // forms and creates appropriate Intent and pass it to AlarmManager
        intent.setAction(ACTION_WATCHDOG_TIMER);
        intent.setClass(context, AppReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return alarmIntent;
    };

    static public void setTimer(Context context)
    {
        int timeout_ms = 60000;
        PendingIntent alarmIntent = getAlarmIntent(context);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout_ms, alarmIntent);

    }
    static public void cancelTimer(Context context)
    {
        PendingIntent alarmIntent = getAlarmIntent(context);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent);
    }
}
