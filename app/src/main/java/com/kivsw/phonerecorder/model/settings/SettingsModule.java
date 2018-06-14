package com.kivsw.phonerecorder.model.settings;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 3/21/18.
 */
@Module
public class SettingsModule {
    @Provides
    @Singleton
    ISettings provideSettings(Context cntx)
    {
        return new Settings(cntx);
    }
}
