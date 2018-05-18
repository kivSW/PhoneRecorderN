package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentDataKeeper;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IJournal;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.ui.notification.NotificationShower;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class TaskModule {
    @Provides
    @Singleton
    CallRecorder provideCallRecorder(Context context, ISettings settings, IPersistentDataKeeper callInfoKeeper, TaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new CallRecorder(context,settings, callInfoKeeper, taskExecutor, notification, errorProcessor);
    };


    @Provides
    @Singleton
    RecordSender provideRecordSender(Context context, ISettings settings, IJournal persistentData, DiskContainer disks, TaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new RecordSender(context,settings,persistentData,disks, taskExecutor, notification, errorProcessor);
    };

    @Provides
    @Singleton
    SmsReader provideSmsReader(Context context, ISettings settings, IJournal journal, IPersistentDataKeeper persistentData, TaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        return new SmsReader(context,settings,journal, persistentData, taskExecutor, notification, errorProcessor);
    };
}


