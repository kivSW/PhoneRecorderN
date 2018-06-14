package com.kivsw.phonerecorder.model.tasks;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.persistent_data.IPersistentDataKeeper;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.ui.notification.NotificationShower;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class TaskModule {
    @Provides
    @Singleton
    CallRecorder provideCallRecorder(Context context, ISettings settings, IPersistentDataKeeper callInfoKeeper, ITaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new CallRecorder(context,settings, callInfoKeeper, taskExecutor, notification, errorProcessor);
    };


    @Provides
    @Singleton
    RecordSender provideRecordSender(Context context, ISettings settings, IJournal persistentData, DiskContainer disks, ITaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new RecordSender(context,settings,persistentData,disks, taskExecutor, notification, errorProcessor);
    };

    @Provides
    @Singleton
    SmsReader provideSmsReader(Context context, ISettings settings, IJournal journal, IPersistentDataKeeper persistentData, ITaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new SmsReader(context,settings,journal, persistentData, taskExecutor, notification, errorProcessor);
    };
}


