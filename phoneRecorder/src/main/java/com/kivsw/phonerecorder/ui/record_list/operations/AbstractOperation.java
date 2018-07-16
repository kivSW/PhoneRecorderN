package com.kivsw.phonerecorder.ui.record_list.operations;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.phonerecorder.model.error_processor.InsignificantException;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.record_list.RecordListContract;

import io.reactivex.Completable;
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 7/6/18.
 */

public abstract class AbstractOperation {
    protected ISettings settings;
    protected DiskContainer disks;
    protected IInternalFiles internalFiles;
    protected Context appContext;

    AbstractOperation(Context context, ISettings settings, IInternalFiles internalFiles, DiskContainer disks)
    {
        this.settings = settings;
        this.disks = disks;
        this.internalFiles = internalFiles;
        this.appContext = context;
    };

    /*
    * return true if fileInfo is consistent.
    * Check that this fileInfo has a reference to its main RecordFileInfo, in case fileInfo should has the reference
    * */
    protected boolean isConsistent(RecordListContract.RecordFileInfo fileInfo)
    {

        if(fileInfo.fromInternalDir && internalFiles.isSent(fileInfo.recordFileNameData.origFileName))
        {

            return fileInfo.cachedRecordFileInfo!=null;
        }
        return true;
    };

    protected Completable getRetryLaterError()
    {
        return Completable.error(new InsignificantException(appContext.getText(R.string.retry_later).toString()));
    }
}
