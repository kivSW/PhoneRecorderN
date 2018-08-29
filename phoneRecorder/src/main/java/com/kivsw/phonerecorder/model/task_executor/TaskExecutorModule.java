package com.kivsw.phonerecorder.model.task_executor;

import android.content.Context;
import android.os.Build;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class TaskExecutorModule {
    @Singleton
    @Provides
    ITaskExecutor provideTaskExecutor(Context context)
    {
        if(Build.VERSION.SDK_INT<22)
              return new TaskExecutor(context);
        return new TaskExecutorJobService(context);
    };
}
