package com.kivsw.phonerecorder.ui.record_list.operations;

import com.kivsw.cloud.DiskContainer;
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

public class DeleteRecordsOperation {
    private ISettings settings;
    private DiskContainer disks;

    DeleteRecordsOperation(ISettings settings, DiskContainer disks)
    {
        this.settings = settings;
        this.disks = disks;
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
        if(fileInfo.fromInternalDir && fileInfo.cachedRecordFileInfo!=null)
        {
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
