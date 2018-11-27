package com.kivsw.phonerecorder.model.task_executor.tasks;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.addrbook.PhoneAddrBook;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.model.persistent_data.IPersistentDataKeeper;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.ui.notification.NotificationShower;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class TaskModule {
    @Provides
    @Singleton
    CallRecorder provideCallRecorder(Context context, ISettings settings, IPersistentDataKeeper callInfoKeeper,
                                     ITaskExecutor taskExecutor, IInternalFiles internalFiles,
                                     NotificationShower notification, PhoneAddrBook localPhoneAddrBook,
                                     IJournal journal, IErrorProcessor errorProcessor)
    {
        return new CallRecorder(context,settings, callInfoKeeper, taskExecutor, internalFiles, notification, localPhoneAddrBook, journal, errorProcessor);
    };


    @Provides
    @Singleton
    RecordSender provideRecordSender(Context context, ISettings settings, IJournal persistentData, DiskContainer disks, ITaskExecutor taskExecutor, NotificationShower notification, IInternalFiles internalFiles, IErrorProcessor errorProcessor)
    {
        return new RecordSender(context,settings,persistentData,disks, taskExecutor, notification, internalFiles, errorProcessor);
    };

    @Provides
    @Singleton
    SmsReader provideSmsReader(Context context, ISettings settings, IJournal journal, IPersistentDataKeeper persistentData,
                               ITaskExecutor taskExecutor, NotificationShower notification, PhoneAddrBook localPhoneAddrBook,
                               IInternalFiles internalFiles, IErrorProcessor errorProcessor)
    {
        return new SmsReader(context,settings,journal, persistentData, taskExecutor, notification, localPhoneAddrBook, internalFiles, errorProcessor);
    };

    @Provides
    @Singleton
    AddrBookReader provideAddrBookReader(Context context, ISettings settings, IPersistentDataKeeper persistentData, ITaskExecutor taskExecutor,
                                         NotificationShower notification, IInternalFiles internalFiles, IErrorProcessor errorProcessor)
    {
        return new AddrBookReader(context,settings, persistentData, taskExecutor, notification, internalFiles, errorProcessor);
    };

    @Provides
    @Singleton
    ITaskProvider provideTaskProvider(final SmsReader smsReader, final RecordSender recordSender,
                                      final CallRecorder callRecorder, final AddrBookReader addrBookReader)
    {
        return new ITaskProvider() {
         /*   @Override
            public int getTaskId(String taskId) {
                ITask task= getTask(taskId);
                if(task==null) return -1;
                return task.getTaskId();
            }*/

            @Override
            public ITask getTask(String taskId) {
                switch(taskId)
                {
                    case TASK_CALL_RECORDING: return callRecorder;
                    case TASK_SEND_FILES:     return recordSender;
                    case TASK_SMS_READING:    return  smsReader;
                    case TASK_ADDRBOOK_READING: return addrBookReader;
                };
                return null;
            }
        };
    }


}


