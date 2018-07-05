package com.kivsw.phonerecorder.ui.record_list.operations;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.record_list.RecordListContract;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * Created by ivan on 7/4/18.
 */

public class SetUndeletableFlagOperator {
    private ISettings settings;
    private DiskContainer disks;

    SetUndeletableFlagOperator(ISettings settings, DiskContainer disks)
    {
        this.settings = settings;
        this.disks = disks;
    }

    public Completable setUndeletableFlag(final RecordListContract.RecordFileInfo fileInfo, final boolean isProtected)
    {
        if(fileInfo.fromInternalDir && fileInfo.cachedRecordFileInfo!=null)
        {
            return
                doSetUndeletableFlag(fileInfo.cachedRecordFileInfo, isProtected)
                .andThen(Single.just("") )
                        .flatMapCompletable(new Function<String, CompletableSource>() {
                            @Override
                            public CompletableSource apply(String s) throws Exception {
                                return doSetUndeletableFlag(fileInfo, isProtected);
                            }
                        });
        }
        else
            return doSetUndeletableFlag(fileInfo, isProtected);
    }

    protected Completable doSetUndeletableFlag(final RecordListContract.RecordFileInfo recordFileInfo, boolean isProtected)
    {
        final String dirPath = recordFileInfo.parentDir;

        String oldFileName = recordFileInfo.recordFileNameData.origFileName;

        recordFileInfo.recordFileNameData.isProtected = isProtected;
        final String newFileName = recordFileInfo.recordFileNameData.buildFileName();

        return disks.renameFile(dirPath + oldFileName, dirPath + newFileName)
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        recordFileInfo.recordFileNameData.origFileName = newFileName;
                    }
                });
    }
}
