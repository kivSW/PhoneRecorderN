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
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 7/4/18.
 */

public class DeleteRecordsOperation {
    private ISettings settings;
    private DiskContainer disks;
    private IInternalFiles internalFiles;
    private Context appContext;

    DeleteRecordsOperation(Context context, ISettings settings, IInternalFiles internalFiles, DiskContainer disks)
    {
        this.settings = settings;
        this.disks = disks;
        this.internalFiles = internalFiles;
        this.appContext = context;
    };

    public Completable deleteRecords(List<RecordListContract.RecordFileInfo> files)
    {
        return
            Observable.fromIterable(files)
                .subscribeOn(Schedulers.io())
               // .observeOn(Schedulers.io())
                .flatMapCompletable(new Function<RecordListContract.RecordFileInfo, CompletableSource>() {
                    @Override
                    public CompletableSource apply(RecordListContract.RecordFileInfo recordFileInfo) throws Exception {
                        return doDelete(recordFileInfo);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected Completable doDelete(final RecordListContract.RecordFileInfo fileInfo)
    {

        if(fileInfo.fromInternalDir && internalFiles.isSent(fileInfo.recordFileNameData.origFileName))
        {
            if(fileInfo.cachedRecordFileInfo!=null)
                return Completable.error(new Exception(appContext.getText(R.string.retry_later).toString()));

            return
                doDelete(fileInfo.cachedRecordFileInfo)
                    .andThen(Single.just("") )
                    .flatMapCompletable(new Function<String, CompletableSource>() {
                        @Override
                        public CompletableSource apply(String s) throws Exception {
                            fileInfo.cachedRecordFileInfo=null;
                            return doDelete(fileInfo);
                        }
                    });
        }

        return disks.deleteFile(fileInfo.getFileFullPath());

    }
}
