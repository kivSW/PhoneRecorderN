package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import com.kivsw.cloud.DiskContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;

/**
 * Created by ivan on 3/21/18.
 */

@Module
public class SettingsPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public SettingsContract.ISettingsPresenter providePresenter(ISettings settings, DiskContainer diskList, TaskExecutor taskExecutor, IErrorProcessor errorProcessor)
    {
        return new SettingsPresenter(settings, diskList, taskExecutor, errorProcessor);
    }

}
