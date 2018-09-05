package com.kivsw.phonerecorder.ui.record_list.operations;

/**
 * Created by ivan on 7/4/18.
 */

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.settings.ISettings;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

@Module
public class OperationsModule {
    @Provides
    @NonNull
    ReadRecordListOperation provideReadRecordFileListOperation(ISettings settings, DiskContainer disks, CloudCache cloudCache)
    {
        return new ReadRecordListOperation(settings, disks, cloudCache);
    }

    @Provides
    @NonNull
    DeleteRecordsOperation provideDeleteRecordsOperation(Context context, ISettings settings, IInternalFiles internalFiles, DiskContainer disks)
    {
        return new DeleteRecordsOperation(context,settings, internalFiles, disks);
    }

    @Provides
    @NonNull
    SetUndeletableFlagOperator provideSetUndeletableOperator(Context context, ISettings settings, IInternalFiles internalFiles, DiskContainer disks)
    {
        return new SetUndeletableFlagOperator(context,settings, internalFiles, disks);
    }
}
