package com.kivsw.phonerecorder.ui.record_list.operations;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.record_list.BunchOfFiles;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * this class retrieves all records from a directory
 */

public class ReadRecordListOperation {
    private ISettings settings;
    private DiskContainer disks;

    ReadRecordListOperation(ISettings settings, DiskContainer disks)
    {
        this.settings = settings;
        this.disks = disks;
    }

    public Observable<BunchOfFiles> getCallRecordList(final String dirPath)
    {
        if(dirPath.equals(settings.getSavingUrlPath()))
        {
            final String internalDir = settings.getInternalTempPath();
            return
                    getDirectoryContent(internalDir, true)
                            .concatWith( getDirectoryContent(dirPath, false))
                            .toObservable();
        }
        else
            return getDirectoryContent(dirPath, false)
                            .toObservable();
    }
    protected Single<BunchOfFiles> getDirectoryContent(final String dirPath, final boolean isCache)
    {
        return
                disks.authorizeIfNecessary(dirPath)
                        .andThen(Single.just("") )
                        .flatMap(new Function<String, SingleSource<IDiskIO.ResourceInfo>>() {
                            @Override
                            public SingleSource<IDiskIO.ResourceInfo> apply(@NonNull String s) throws Exception {
                                return disks.getResourceInfo(dirPath);
                            }
                        })
                        .map(new Function<IDiskIO.ResourceInfo, BunchOfFiles>() {
                            @Override
                            public BunchOfFiles apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                                return new BunchOfFiles(dirPath, resourceInfo.content(), isCache);
                            }
                        });

    }
}
