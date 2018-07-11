package com.kivsw.phonerecorder.model.task_executor;

import android.content.Context;

import com.kivsw.phonerecorder.os.AppService;

/**
 * Created by ivan on 4/26/18.
 */

class TaskExecutor implements ITaskExecutor{

    Context context;
    TaskExecutor(Context context)
    {
        this.context = context;
    }

    @Override
    public void startCallRecording()
    {
        AppService.startTask(context, AppService.TASK_CALL_RECORDING);
    }
    @Override
    public void stopCallRecording()
    {
        AppService.stopTask(context, AppService.TASK_CALL_RECORDING);
    }

    @Override
    public void startFileSending()
    {
        AppService.startTask(context, AppService.TASK_SEND_FILES);
    }
    @Override
    public void stopFileSending()
    {
        AppService.stopTask(context, AppService.TASK_SEND_FILES);
    }

    @Override
    public void startSMSreading()
    {
        AppService.startTask(context, AppService.TASK_SMS_READING);
    }
    @Override
    public void stopSMSreading()
    {
        AppService.stopTask(context, AppService.TASK_SMS_READING);
    }

}
