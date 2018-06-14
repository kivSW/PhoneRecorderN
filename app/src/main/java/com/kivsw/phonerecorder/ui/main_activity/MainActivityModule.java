package com.kivsw.phonerecorder.ui.main_activity;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 5/10/18.
 */
@Module
public class MainActivityModule {
    @Provides
    @Singleton
    @NonNull
    MainActivityContract.IMainActivityPresenter providePresenter(Context context, ISettings settings)
    {
        return new MainActivityPresenter(context, settings);
    }
}
