package phonerecorder.kivsw.com.faithphonerecorder.model.task_executor;

import android.content.Context;

import phonerecorder.kivsw.com.faithphonerecorder.os.Service;

/**
 * Created by ivan on 4/26/18.
 */

public class TaskExecutor {

    Context context;
    TaskExecutor(Context context)
    {
        this.context = context;
    };

    public void startCallRecording()
    {
        Service.startTask(context, Service.TASK_CALL_RECORDING);
    };
    public void stopCallRecording()
    {
        Service.stopTask(context, Service.TASK_CALL_RECORDING);
    };

    public void startFileSending()
    {
        Service.startTask(context, Service.TASK_SEND_FILES);
    };
    public void stopFileSending()
    {
        Service.stopTask(context, Service.TASK_SEND_FILES);
    };

}
