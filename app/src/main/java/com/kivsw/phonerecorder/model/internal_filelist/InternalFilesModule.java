package com.kivsw.phonerecorder.model.internal_filelist;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.settings.ISettings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class InternalFilesModule {
    @Singleton
    @Provides
    IInternalFiles provideInternalFiles( ISettings settings, IErrorProcessor errorProcessor)
    {
        return new InternalFiles(settings, errorProcessor);
    }
}
