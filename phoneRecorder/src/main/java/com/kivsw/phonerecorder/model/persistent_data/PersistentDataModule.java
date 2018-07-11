package com.kivsw.phonerecorder.model.persistent_data;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class PersistentDataModule {
    @Provides
    @Singleton
    IJournal provideJournal(Context cntx, ISettings settings)
    {
        return new Journal(cntx, settings);
    }

    @Provides
    @Singleton
    IPersistentDataKeeper provideCallInfoKeeper(Context cntx)
    {
        return new CallInfoKeeper(cntx);
    }
}
