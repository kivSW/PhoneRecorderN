package phonerecorder.kivsw.com.faithphonerecorder.os;

import javax.inject.Singleton;

import dagger.Component;
import phonerecorder.kivsw.com.faithphonerecorder.model.CloudCacheModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.DiskRepresentativeModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.ErrorProcessorModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.PersistentDataModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.AndroidPlayerModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.SettingsModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutorModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.CallRecorder;
import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.RecordSender;
import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.TaskModule;
import phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity.MainActivity;
import phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity.MainActivityModule;
import phonerecorder.kivsw.com.faithphonerecorder.ui.record_list.RecordListFragment;
import phonerecorder.kivsw.com.faithphonerecorder.ui.record_list.RecordListPresenterModule;
import phonerecorder.kivsw.com.faithphonerecorder.ui.settings.SettingsFragment;
import phonerecorder.kivsw.com.faithphonerecorder.ui.settings.SettingsPresenterModule;

/**
 * Created by ivan on 3/21/18.
 */

@Component(modules={SettingsPresenterModule.class, RecordListPresenterModule.class, SettingsModule.class,
           ApplicationModule.class, DiskRepresentativeModule.class, AndroidPlayerModule.class,
           CloudCacheModule.class, PersistentDataModule.class, TaskExecutorModule.class,
           TaskModule.class, NotificationShowerModule.class, ErrorProcessorModule.class, MainActivityModule.class})
@Singleton
public interface ApplicationComponent {
    void inject(MainActivity activity);
    void inject(SettingsFragment fragment);
    void inject(RecordListFragment fragment);
    void inject(AppReceiver receiver);
    void inject(AppService service);

    CallRecorder getCallRecorder();
    RecordSender getRecordSender();

}
