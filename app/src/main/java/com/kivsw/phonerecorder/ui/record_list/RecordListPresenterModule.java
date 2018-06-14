package com.kivsw.phonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.tasks.RecordSender;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 3/27/18.
 */
@Module
public class RecordListPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public RecordListContract.IRecordListPresenter providePresenter(Context appContext, ISettings settings, DiskContainer disks, CloudCache cloudCache, IErrorProcessor errorProcessor, RecordSender recordSender)
    {
        return new RecordListPresenter(appContext, settings, disks, cloudCache,  errorProcessor, recordSender);
    }
}
