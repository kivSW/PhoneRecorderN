package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloud.disk.IDiskIO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.os.NotificationShower;
import phonerecorder.kivsw.com.faithphonerecorder.os.WatchdogTimer;

/**
 * Move records from the temp directory to the storage directory
 */

public class RecordSender implements ITask {
    private Context context;
    private ISettings settings;
    private IPersistentData persistentData;
    private IErrorProcessor errorProcessor;
    private DiskContainer disks;
    private TaskExecutor taskExecutor;
    private NotificationShower notification;


    @Inject
    public RecordSender(Context context, ISettings settings, IPersistentData persistentData, DiskContainer disks, TaskExecutor taskExecutor,
                        NotificationShower notification, IErrorProcessor errorProcessor) {
        this.settings = settings;
        this.persistentData = persistentData;
        this.disks = disks;
        this.taskExecutor = taskExecutor;
        this.context = context;
        this.notification = notification;
        this.errorProcessor = errorProcessor;
    }

    class NotificationInfo {
        String name;
        int totalFileCount=0, currentFileNumber=0;

        void updateNotification()
        {
            int percent=-1;
            if(totalFileCount>0)
                percent = currentFileNumber*100/totalFileCount;
            String txt=String.format(Locale.US, name, totalFileCount, currentFileNumber);
            notification.show(txt, percent);
        };
    }

    @Override
    public void startTask() {

        WatchdogTimer.setTimer(context); // TODO check for reentrance

        final NotificationInfo notificationInfo=new NotificationInfo();
        notificationInfo.name = context.getText(R.string.rec_sending).toString();

        final String srcPath=settings.getInternalTempPath();
        final String dstPath= settings.getSavingUrlPath();

        Single.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] fileList = getRecordFileList(srcPath);
                notificationInfo.totalFileCount = fileList.length;
                notificationInfo.currentFileNumber=0;
                notificationInfo.updateNotification();
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
                notificationInfo.currentFileNumber++;
                notificationInfo.updateNotification();

                final String source =srcPath + file;
                String destination = dstPath + file;
                return createUploadObservable(source, destination);
            };
        })
        .concatMap(new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer integer) throws Exception {
                return createDeleteOldFilesCompletable(dstPath).toObservable();
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
                errorProcessor.onError(e);
                taskExecutor.stopFileSending();

            }

            @Override
            public void onComplete() {
                taskExecutor.stopFileSending();
                WatchdogTimer.cancelTimer(context);
            }
        });


    }

    @Override
    public void stopTask() {
      // do nothing because RecordSender stops itself
        notification.hide();
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

    protected Observable<Integer> createUploadObservable(final String source, String destination)
    {
        return
            disks.uploadFile(destination, source)
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
                            errorProcessor.onError(throwable);
                            return Observable.empty();
                        }
                    });
    };

    protected Completable createDeleteOldFilesCompletable(final String dstPath)
    {
        final NotificationInfo notificationInfo=new NotificationInfo();
        notificationInfo.name = context.getText(R.string.rec_deleting).toString();

        return
        disks.getResourceInfo(dstPath)
        .observeOn(Schedulers.io())
        .flatMapObservable(new Function<IDiskIO.ResourceInfo, Observable<String>>(){

                @Override
                public Observable<String> apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                    List<IDiskIO.ResourceInfo> recordList=filterRecordFileList(resourceInfo);
                    List<String> deletableFileList = getDeletableList(recordList);
                    notificationInfo.totalFileCount = deletableFileList.size();

                    return Observable.fromIterable(deletableFileList);
            }
            })
            .flatMapCompletable(new Function<String, CompletableSource>() {
                @Override
                public CompletableSource apply(String fileName) throws Exception {
                    notificationInfo.currentFileNumber++;
                    notificationInfo.updateNotification();

                    return disks.deleteFile(dstPath+fileName);
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

   /* protected StorageUtils.CloudFile getCloudFile()
    {
        String filePath = settings.getSavingUrlPath();
        StorageUtils.CloudFile cloudFile
                =StorageUtils.parseFileName(filePath, diskList);
        return cloudFile;
    }
    protected IDiskIO getDiskIO()
    {
        return getCloudFile().diskRepresenter.getDiskIo();
    }*/
}
