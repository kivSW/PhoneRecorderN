package com.kivsw.phonerecorder.ui.ErrorMessage;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 6/1/18.
 */
@Module
public class MvpErrorMessageBuilderModule {

    @Singleton
    @Provides
    MvpErrorMessageBuilder providesErrorMessageBoxBuilder(Context context)
    {
        return new MvpErrorMessageBuilder(context);
    }
}
