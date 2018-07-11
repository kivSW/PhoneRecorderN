package com.kivsw.phonerecorder.ui.record_list.operations;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.record_list.RecordListContract;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ivan on 7/4/18.
 */

public class DeleteRecordsOperation extends AbstractOperation{


    DeleteRecordsOperation(Context context, ISettings settings, IInternalFiles internalFiles, DiskContainer disks)
    {
        super(context,settings,internalFiles,disks);
    };

    public Completable deleteRecords(List<RecordListContract.RecordFileInfo> files, final boolean allDataLoaded)
    {
        return
            Observable.fromIterable(files)
                .subscribeOn(Schedulers.io())
               // .observeOn(Schedulers.io())
                .flatMapCompletable(new Function<RecordListContract.RecordFileInfo, CompletableSource>() {
                    @Override
                    public CompletableSource apply(RecordListContract.RecordFileInfo recordFileInfo) throws Exception {
                        return doDeleteRecord(recordFileInfo, allDataLoaded);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected Completable doDeleteRecord(final RecordListContract.RecordFileInfo fileInfo, boolean allDataLoaded)
    {
        if(!isConsistent(fileInfo, allDataLoaded))
            return getRetryLaterError();

            if(fileInfo.cachedRecordFileInfo==null)
                return doDeleteFile(fileInfo);
            else
            return
                    doDeleteFile(fileInfo.cachedRecordFileInfo)
                    .andThen(Single.just("") )
                    .flatMapCompletable(new Function<String, CompletableSource>() {
                        @Override
                        public CompletableSource apply(String s) throws Exception {
                            fileInfo.cachedRecordFileInfo=null;
                            return doDeleteFile(fileInfo);
                        }
                    });
    }

    protected Completable doDeleteFile(final RecordListContract.RecordFileInfo fileInfo)
    {
        return disks.deleteFile(fileInfo.getFileFullPath());
    }


}
