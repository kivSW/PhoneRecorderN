package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;
import android.net.Uri;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloud.disk.StorageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;

/**
 * Move records from the temp directory to the storage directory
 */

public class RecordSender implements ITask {
    Context context;
    ISettings settings;
    IPersistentData persistentData;
    List<IDiskRepresenter> diskList;
    TaskExecutor taskExecutor;


    @Inject
    public RecordSender(Context context, ISettings settings, IPersistentData persistentData, List<IDiskRepresenter> diskList, TaskExecutor taskExecutor) {
        this.settings = settings;
        this.persistentData = persistentData;
        this.diskList = diskList;
        this.taskExecutor = taskExecutor;
        this.context = context;
    }

    @Override
    public void startTask() {
        final String srcPath=settings.getInternalTempPath();
        final String dstPath= Uri.parse(settings.getSavingPath()).getPath();
        final IDiskIO diskIO = getDiskIO();

        Single.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] fileList = getRecordFileList(srcPath);
                return fileList;
            }
        })
        .subscribeOn(Schedulers.io())
        .flatMapObservable(new Function<String[], ObservableSource<String> >(){
            @Override
            public ObservableSource<String> apply(String[] fileList) throws Exception {
                return Observable.fromArray(fileList);
            }
        })
        .flatMap(new Function<String, ObservableSource<Integer>>(){
            @Override
            public ObservableSource<Integer> apply(String file) throws Exception {
                final String source =srcPath + file;
                String destination = dstPath + file;
                return createUploadObservable(diskIO, source, destination);
            };
        })
        .concatMap(new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer integer) throws Exception {
                return createDeleteOldFilesCompletable(diskIO, dstPath).toObservable();
            }
        })
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(Integer integer) {}

            @Override
            public void onError(Throwable e) {
                persistentData.journalAdd(e);
                taskExecutor.stopFileSending();
            }

            @Override
            public void onComplete() {
                taskExecutor.stopFileSending();
            }
        });


    }

    @Override
    public void stopTask() {
      // do nothing because RecordSender stops itself
    }

    protected String[] getRecordFileList(String LocalDir)
    {
        File dir=new File(LocalDir);
        final Pattern p = Pattern.compile(RecordFileNameData.PATTERN);
        String[] fileList = dir.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                Matcher m = p.matcher(name);
                return m.find();
            }
        });
        if(fileList==null)
            fileList = new String[0];
        return fileList;
    }

    protected Observable<Integer> createUploadObservable(IDiskIO diskIO, final String source, String destination)
    {
        return
            diskIO.uploadFile(destination, source)
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            File file = new File(source);
                            file.delete();
                        }
                    })
                    .onErrorResumeNext(new Function<Throwable, Observable<Integer>>(){
                        @Override
                        public Observable<Integer> apply(Throwable throwable) throws Exception {
                            persistentData.journalAdd(throwable);
                            return Observable.empty();
                        }
                    });
    };

    protected Completable createDeleteOldFilesCompletable(final IDiskIO diskIO, final String dstPath)
    {
        return
        diskIO.getResourceInfo(dstPath)
        .observeOn(Schedulers.io())
        .flatMapObservable(new Function<IDiskIO.ResourceInfo, Observable<String>>(){

                @Override
                public Observable<String> apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                    List<IDiskIO.ResourceInfo> recordList=filterRecordFileList(resourceInfo);
                    List<String> deletableFileList = getDeletableList(recordList);
                    return Observable.fromIterable(deletableFileList);
            }
            })
            .flatMapCompletable(new Function<String, CompletableSource>() {
                @Override
                public CompletableSource apply(String fileName) throws Exception {
                    return diskIO.deleteFile(dstPath+fileName);
                }
            })
            .observeOn(AndroidSchedulers.mainThread());

    };

    List<IDiskIO.ResourceInfo> filterRecordFileList(IDiskIO.ResourceInfo resourceInfo)
    {
        List<IDiskIO.ResourceInfo>
                fileList=resourceInfo.content(),
                res = new ArrayList<>(fileList.size());

        Pattern p = Pattern.compile(RecordFileNameData.PATTERN);//"^[0-9]{8}_[0-9]{6}_"); // this pattern filters the other app's files
        for(IDiskIO.ResourceInfo file:fileList)
        {
            if(!file.isFile()) continue;
            Matcher m = p.matcher(file.name());
            if(!m.find()) continue;
            res.add( file );
        };
        Collections.sort(res, new Comparator<IDiskIO.ResourceInfo>(){
            @Override
            public int compare(IDiskIO.ResourceInfo o1, IDiskIO.ResourceInfo o2) {
                return o2.name().compareTo(o1.name());
            }
        });
        return res;
    };

    List<String> getDeletableList(List<IDiskIO.ResourceInfo> fileList)
    {
        int maxFileCount;
        boolean hasDataSizeLimit = settings.getDataSizeLimitation();
        boolean hasFileAmountLimit = settings.getFileAmountLimitation();

        if(hasFileAmountLimit)      maxFileCount= settings.getKeptFileAmount();
        else maxFileCount= settings.maxKeptFileAmount();


        int limit = Math.min(fileList.size(), maxFileCount);
        if(hasDataSizeLimit)
        {
            long dataSize=0;
            long maxDataSize;
            maxDataSize=settings.maxFileDataSize();

            for(int i=0; i<limit; i++)
            {
                dataSize += fileList.get(i).size();
                if(dataSize>maxDataSize)
                    limit=i;
            };
        };

        ArrayList<String> res = new ArrayList<>(fileList.size()-limit);
        int s=fileList.size();
        for(int i=limit; i<s; i++)
        {
            String fn=fileList.get(i).name();
            RecordFileNameData rfd = RecordFileNameData.decipherFileName(fn);
            if(rfd!=null && !rfd.isProtected)
                res.add(fn);
        };

        return res;

    };

    protected StorageUtils.CloudFile getCloudFile()
    {
        String filePath = settings.getSavingPath();
        StorageUtils.CloudFile cloudFile
                =StorageUtils.parseFileName(filePath, diskList);
        return cloudFile;
    }
    protected IDiskIO getDiskIO()
    {
        return getCloudFile().diskRepresenter.getDiskIo();
    }
}
