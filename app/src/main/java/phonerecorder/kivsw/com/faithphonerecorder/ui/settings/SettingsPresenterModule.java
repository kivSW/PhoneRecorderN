package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.ITaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.ui.notification.AntiTaskKillerNotification;

/**
 * Created by ivan on 3/21/18.
 */

@Module
public class SettingsPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public SettingsContract.ISettingsPresenter providePresenter(Context context, ISettings settings, DiskContainer diskList, ITaskExecutor taskExecutor, IErrorProcessor errorProcessor, AntiTaskKillerNotification antiTaskKillerNotification)
    {
        return new SettingsPresenter(context, settings, diskList, taskExecutor, errorProcessor, antiTaskKillerNotification);
    }

}
