package com.kivsw.phonerecorder.ui.record_list.operations;

/**
 * Created by ivan on 7/4/18.
 */

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.settings.ISettings;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

@Module
public class OperationsModule {
    @Provides
    @NonNull
    ReadRecordListOperation provideReadRecordFileListOperation(ISettings settings, DiskContainer disks)
    {
        return new ReadRecordListOperation(settings, disks);
    }

    @Provides
    @NonNull
    DeleteRecordsOperation provideDeleteRecordsOperation(ISettings settings, DiskContainer disks)
    {
        return new DeleteRecordsOperation(settings, disks);
    }

    @Provides
    @NonNull
    SetUndeletableFlagOperator provideSetUndeletableOperator(ISettings settings, DiskContainer disks)
    {
        return new SetUndeletableFlagOperator(settings, disks);
    }
}
