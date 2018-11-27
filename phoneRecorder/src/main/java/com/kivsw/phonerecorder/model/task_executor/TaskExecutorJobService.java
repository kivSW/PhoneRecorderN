package com.kivsw.phonerecorder.model.task_executor;

import android.content.Context;

import com.kivsw.phonerecorder.model.task_executor.tasks.ITaskProvider;
import com.kivsw.phonerecorder.os.jobs.AppJobService;

/**
 * Created by ivan on 4/26/18.
 */

class TaskExecutorJobService implements ITaskExecutor{

    Context context;
    TaskExecutorJobService(Context context)
    {
        this.context = context;
    }

    @Override
    public void startCallRecording()
    {
        AppJobService.startTask(context, ITaskProvider.TASK_CALL_RECORDING);
    }
    @Override
    public void stopCallRecording()
    {
        AppJobService.stopTask(context, ITaskProvider.TASK_CALL_RECORDING);
    }

    @Override
    public void startFileSending()
    {
        AppJobService.startTask(context, ITaskProvider.TASK_SEND_FILES);
    }
    @Override
    public void stopFileSending()
    {
        AppJobService.stopTask(context, ITaskProvider.TASK_SEND_FILES);
    }

    @Override
    public void startSMSreading()
    {
        AppJobService.startTask(context, ITaskProvider.TASK_SMS_READING);
    }
    @Override
    public void stopSMSreading()
    {
        AppJobService.stopTask(context, ITaskProvider.TASK_SMS_READING);
    }

    @Override
    public void startAddrBookReading()
    {
        AppJobService.startTask(context, ITaskProvider.TASK_ADDRBOOK_READING);
    };
    @Override
    public void stopAddrBookReading(){
        AppJobService.stopTask(context, ITaskProvider.TASK_ADDRBOOK_READING);
    };


}
