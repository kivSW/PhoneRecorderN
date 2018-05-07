package phonerecorder.kivsw.com.faithphonerecorder.model.task_executor;

import android.content.Context;

import phonerecorder.kivsw.com.faithphonerecorder.os.AppService;

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
        AppService.startTask(context, AppService.TASK_CALL_RECORDING);
    };
    public void stopCallRecording()
    {
        AppService.stopTask(context, AppService.TASK_CALL_RECORDING);
    };

    public void startFileSending()
    {
        AppService.startTask(context, AppService.TASK_SEND_FILES);
    };
    public void stopFileSending()
    {
        AppService.stopTask(context, AppService.TASK_SEND_FILES);
    };

}
