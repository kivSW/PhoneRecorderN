package com.kivsw.phonerecorder.model.player;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 4/19/18.
 */
@Module
public class AndroidPlayerModule {

    @Singleton
    @Provides
    AndroidPlayer  provideAndroidPlayer()
    {
        return new AndroidPlayer();
    }

}
