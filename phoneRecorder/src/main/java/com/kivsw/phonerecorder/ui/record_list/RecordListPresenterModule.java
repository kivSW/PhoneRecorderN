package com.kivsw.phonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.tasks.CallRecorder;
import com.kivsw.phonerecorder.model.task_executor.tasks.RecordSender;
import com.kivsw.phonerecorder.model.task_executor.tasks.SmsReader;
import com.kivsw.phonerecorder.ui.record_list.operations.DeleteRecordsOperation;
import com.kivsw.phonerecorder.ui.record_list.operations.ReadRecordListOperation;
import com.kivsw.phonerecorder.ui.record_list.operations.SetUndeletableFlagOperator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

/**
 * Created by ivan on 3/27/18.
 */
@Module
public class RecordListPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public RecordListContract.IRecordListPresenter providePresenter(Context appContext, ISettings settings, DiskContainer disks, CloudCache cloudCache,
                                                                    ReadRecordListOperation readRecordListOperation, DeleteRecordsOperation deleteRecordsOperation, SetUndeletableFlagOperator setUndeletableFlagOperator,
                                                                    IErrorProcessor errorProcessor, RecordSender recordSender, CallRecorder callRecorder, SmsReader smsReader)
    {
        return new RecordListPresenter(appContext, settings, disks, cloudCache,
                readRecordListOperation, deleteRecordsOperation, setUndeletableFlagOperator,
                errorProcessor, recordSender, callRecorder,smsReader);
    }
}
