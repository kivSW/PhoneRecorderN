package com.kivsw.phonerecorder.model.task_executor;

import android.content.Context;

import com.kivsw.phonerecorder.model.task_executor.tasks.ITaskProvider;
import com.kivsw.phonerecorder.os.jobs.AppService;

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
        AppService.startTask(context, ITaskProvider.TASK_CALL_RECORDING);
    }
    @Override
    public void stopCallRecording()
    {
        AppService.stopTask(context, ITaskProvider.TASK_CALL_RECORDING);
    }

    @Override
    public void startFileSending()
    {
        AppService.startTask(context, ITaskProvider.TASK_SEND_FILES);
    }
    @Override
    public void stopFileSending()
    {
        AppService.stopTask(context, ITaskProvider.TASK_SEND_FILES);
    }

    @Override
    public void startSMSreading()
    {
        AppService.startTask(context, ITaskProvider.TASK_SMS_READING);
    }
    @Override
    public void stopSMSreading()
    {
        AppService.stopTask(context, ITaskProvider.TASK_SMS_READING);
    }

    @Override
    public void startAddrBookReading()
    {
        AppService.startTask(context, ITaskProvider.TASK_ADDRBOOK_READING);
    };
    @Override
    public void stopAddrBookReading(){
        AppService.stopTask(context, ITaskProvider.TASK_ADDRBOOK_READING);
    };

}
