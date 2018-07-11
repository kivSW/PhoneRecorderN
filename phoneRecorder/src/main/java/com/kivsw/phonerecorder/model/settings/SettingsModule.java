package com.kivsw.phonerecorder.model.settings;

import android.content.Context;

import com.kivsw.phonerecorder.model.metrica.IMetrica;

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
    ISettings provideSettings(Context cntx, IMetrica metrica)
    {
        ISettings settings= new Settings(cntx);
        metrica.onSettingsCreate(settings);
        return settings;
    }
}
