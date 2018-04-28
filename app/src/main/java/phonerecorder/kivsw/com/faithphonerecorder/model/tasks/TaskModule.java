package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;

import com.kivsw.cloud.disk.IDiskRepresenter;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class TaskModule {
    @Provides
    @Singleton
    CallRecorder provideCallRecorder(Context context, ISettings settings, IPersistentData persistentData, TaskExecutor taskExecutor)
    {
        return new CallRecorder(context,settings,persistentData, taskExecutor);
    };


    @Provides
    @Singleton
    RecordSender provideRecordSender(Context context, ISettings settings, IPersistentData persistentData, List<IDiskRepresenter> diskList, TaskExecutor taskExecutor)
    {
        return new RecordSender(context,settings,persistentData,diskList, taskExecutor);
    };
}


