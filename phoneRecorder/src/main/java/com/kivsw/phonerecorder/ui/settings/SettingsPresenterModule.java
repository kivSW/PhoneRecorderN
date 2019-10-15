package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
//import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

/**
 * Created by ivan on 3/21/18.
 */

@Module
public class SettingsPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public SettingsContract.ISettingsPresenter providePresenter(Context context, ISettings settings, DiskContainer diskList, ITaskExecutor taskExecutor, IErrorProcessor errorProcessor)
    {
        return new SettingsPresenter(context, settings, diskList, taskExecutor, errorProcessor);
    }

}
