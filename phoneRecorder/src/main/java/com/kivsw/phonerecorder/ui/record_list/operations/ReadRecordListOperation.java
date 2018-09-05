package com.kivsw.phonerecorder.ui.record_list.operations;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.cloudcache.data.CacheFileInfo;
import com.kivsw.phonerecorder.model.addrbook.FileAddrBook;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.ui.record_list.BunchOfFiles;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * this class retrieves all records from a directory
 */

public class ReadRecordListOperation {
    private ISettings settings;
    private DiskContainer disks;
    private CloudCache cloudCache;

    //private FileAddrBook fileAddrBook;

    ReadRecordListOperation(ISettings settings, DiskContainer disks, CloudCache cloudCache)
    {
        this.settings = settings;
        this.disks = disks;
        this.cloudCache = cloudCache;
    }
    /**
     * read directory content and emmits FileAddrBook and BunchOfFiles
     *
     **/
    public Observable<Object> getAllDirectoryContent(final String dirPath)
    {
        if(dirPath.equals(settings.getSavingUrlPath()))
        {
            final String internalDir = settings.getInternalTempPath();
            return
                    getDirectoryContent(internalDir, true)
                            .concatWith( getDirectoryContent(dirPath, false));
        }
        else
            return getDirectoryContent(dirPath, false);
    }

    /**
     * read directory content and emmits FileAddrBook and BunchOfFiles
     * @param dirPath
     * @param isCache
     * @return
     */
    protected Observable<Object> getDirectoryContent(final String dirPath, final boolean isCache)
    {
        return
                disks.authorizeIfNecessary(dirPath)
                    .andThen(Single.just(""))
                    .flatMapObservable(new Function<String, Observable<Object>>(){

                        @Override
                        public Observable apply(String s) throws Exception {
                            return ((Observable)loadAddrBook(dirPath))
                                    .concatWith(getFileList(dirPath, isCache));
                        }
                    });


    }

    protected Observable<FileAddrBook> loadAddrBook(final String dirPath)
    {
        return
            cloudCache.getFileFromCache(dirPath + FileAddrBook.DEFAULT_FILE_NAME)
                    .filter((value) -> {
                        return value instanceof CacheFileInfo;
                    })
                    .observeOn(Schedulers.io())
                    .map((Object v) -> {
                        return new FileAddrBook(((CacheFileInfo) v).localName, null);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext((Object v) -> {
                        return Observable.empty();
                    });


    };
    protected Observable<BunchOfFiles> getFileList(final String dirPath, final boolean isCache)
    {

        return
            disks.getResourceInfo(dirPath)
            .map(new Function<IDiskIO.ResourceInfo, BunchOfFiles>() {
                    @Override
                    public BunchOfFiles apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                        return new BunchOfFiles(dirPath, resourceInfo.content(), isCache);
                    }
                })
            .toObservable();

    }
}
