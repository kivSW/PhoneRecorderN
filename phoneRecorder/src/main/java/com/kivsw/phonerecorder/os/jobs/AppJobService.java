package com.kivsw.phonerecorder.os.jobs;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.model.task_executor.tasks.ITask;
import com.kivsw.phonerecorder.model.task_executor.tasks.ITaskProvider;
import com.kivsw.phonerecorder.os.MyApplication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

//http://www.vogella.com/tutorials/AndroidTaskScheduling/article.html

@TargetApi(22)
public class AppJobService extends JobService {

    private Map<String, List<JobParameters>> activeTasks;
    @Inject  protected ITaskProvider taskProvider;
    @Inject  protected IJournal journal;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        activeTasks = new HashMap<>();
        MyApplication.getComponent().inject(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {

       boolean start =  params.getExtras().getBoolean(EXTRA_START);
       String taskId =    params.getExtras().getString(TASK_NAME);

        journal.journalAdd("AppJobService.onStartJob(): "+taskId+"start="+start);

       boolean hasBeenStarted=false;
        ITask task=taskProvider.getTask(taskId);
        if(task!=null)
        {
            if(start)
            {
                if(task.startTask()) {
                    hasBeenStarted=true;
                    addTask(taskId, params);
                }
            }
            else
            {
                task.stopTask();
                removeTask(taskId);
            }
        }
        else
            removeTask(taskId);

        return hasBeenStarted;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        journal.journalAdd("AppJobService.onStopJob() ");
        return false;
    }


    protected void addTask(String taskId, JobParameters job)
    {
        List<JobParameters> jobParamList= activeTasks.get(taskId);
        if(jobParamList==null)
        {
            jobParamList=new LinkedList<>();
            activeTasks.put(taskId, jobParamList);
        }
        jobParamList.add(job);
        journal.journalAdd("AppJobService.addTask(): job.id="+String.valueOf(job.getJobId()) );

    };

    protected void removeTask(String taskId)
    {
        List<JobParameters> jobParamList= activeTasks.get(taskId);
        if(jobParamList!=null) {
            JobParameters job=jobParamList.remove(0);
            if(jobParamList.size()==0) activeTasks.remove(taskId);

            jobFinished(job, false);
            journal.journalAdd("AppJobService.removeTask(): job.id="+String.valueOf(job.getJobId()) );
        }


    };


    final static String EXTRA_START="EXTRA_START",
                        TASK_NAME="TASK_NAME";
    static int jobId=0;
    protected static void startService(Context context, String taskId, boolean start)
    {
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle extra =new PersistableBundle();
        extra.putBoolean(EXTRA_START, start);
        extra.putString(TASK_NAME, taskId);

        JobInfo.Builder builder = new JobInfo.Builder(jobId++, new ComponentName(context, AppJobService.class));
        builder.setExtras(extra)
                .setMinimumLatency (0);
        if(taskId.equals(ITaskProvider.TASK_SEND_FILES)) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                   .setOverrideDeadline(5*60000);
        }
        else
        {
                builder.setOverrideDeadline(100);
        }

        jobScheduler.schedule(builder.build());

    }
    synchronized public static void startTask(Context context, String taskId)
    {
        startService(context, taskId, true);
    }
    synchronized public static void stopTask(Context context, String taskId)
    {
        startService(context, taskId, false);
    }
}
