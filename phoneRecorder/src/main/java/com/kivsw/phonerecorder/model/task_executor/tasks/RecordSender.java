package com.kivsw.phonerecorder.model.task_executor.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.error_processor.InsignificantException;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.os.WatchdogTimerToSend;
import com.kivsw.phonerecorder.ui.notification.NotificationShower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Move records from the temp directory to the storage directory
 */

public class RecordSender implements ITask {
    private Context context;
    private ISettings settings;
    private IJournal journal;
    private IErrorProcessor errorProcessor;
    private DiskContainer disks;
    private ITaskExecutor taskExecutor;
    private NotificationShower notification;
    private IInternalFiles internalFiles;


    @Inject
    public RecordSender(Context context, ISettings settings, IJournal journal, DiskContainer disks, ITaskExecutor taskExecutor,
                        NotificationShower notification, IInternalFiles internalFiles, IErrorProcessor errorProcessor) {
        this.settings = settings;
        this.journal = journal;
        this.disks = disks;
        this.taskExecutor = taskExecutor;
        this.context = context;
        this.notification = notification;
        this.errorProcessor = errorProcessor;
        this.internalFiles = internalFiles;
    }

    class SendingParam {
        public String srcPath, dstPath;
        public String notificationText;
        public int totalFileQuantity=0, currentFileCount =0;


        public SendingParam( String notificationText)
        {
            this(null, null, notificationText);
        }
        public SendingParam(String srcPath, String dstPath, String notificationText)
        {
           this.srcPath=srcPath;
           this.dstPath=dstPath;
           this.notificationText=notificationText;
        };

        private long lastUpdateTime=0;
        final long MIN_UPDATE_INTERVAL=2000;

        //public int errorCount=0;

        void updateNotification()
        {
            long currentTime= SystemClock.elapsedRealtime();
            if(currentTime-lastUpdateTime < MIN_UPDATE_INTERVAL)
                return;
            lastUpdateTime=currentTime;

            int percent=-1;
            if(totalFileQuantity >0)
                percent = currentFileCount *100/ totalFileQuantity;
            String txt=String.format(Locale.US, notificationText, currentFileCount, totalFileQuantity);
            notification.show(txt, percent, true);
        };

        FileEmitter fileEmmiter;
    }


    class FileEmitter implements ObservableSource<String>
    {
        private Observer observer=null;
        private String[] files;
        private int count;


        FileEmitter(@NonNull String[] files) {
            this.files = files;
            count=0;
        }

        public void subscribe(@NonNull Observer observer)
        {
            this.observer=observer;
            emitNext();
        };

        public void emitNext()
        {
            if(count<files.length)
                observer.onNext(files[count++]);
            else
                observer.onComplete();
        }
    }

    private boolean isSending=false, tryToSendAgain=false;
    private int sentFileCount=0;

    public boolean startTask() {

        if(isSending) {
            tryToSendAgain=true;
            return false;
        }

        WatchdogTimerToSend.setTimer(context);

        final String srcPath=settings.getInternalTempPath();
        final String dstPath= settings.getSavingUrlPath();

        if(!checkSendCondition(dstPath))
            return false;

        journal.journalAdd("RecordSender.startTask()");

        final SendingParam sendingParam=new SendingParam(srcPath, dstPath, context.getText(R.string.rec_sending).toString());

       createCopyRecordsObservables(sendingParam)
           .concatWith(Observable.just("").flatMap(new Function<Object, ObservableSource<Integer>>() {
                        @Override
                        public ObservableSource<Integer> apply(Object v) throws Exception {
                            if(sendingParam.currentFileCount==0)
                                return Observable.empty();
                            return createDeleteOldFilesCompletable(dstPath).toObservable();
                        }
                    }))

            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Object>() {
                @Override
                public void onSubscribe(Disposable d) {}

                @Override
                public void onNext(Object v) {}

                @Override
                public void onError(Throwable e) {
                    isSending=false;
                    errorProcessor.onError(e, internalFiles.isOverflow());
                    taskExecutor.stopFileSending();
                    checkForStartAgain();

                }

                @Override
                public void onComplete() {
                    isSending=false;
                    taskExecutor.stopFileSending();
                    checkForStartAgain();

                }
            });

        return true;
    }


    private void checkForStartAgain()
    {
        if(tryToSendAgain)
            taskExecutor.startFileSending();
        tryToSendAgain=false;
    }

    @Override
    public void stopTask() {
      // do nothing because RecordSender stops itself
        notification.hide();
        if(sentFileCount>0)
           onCopyObservable.onNext("");
    }

    protected Observable<Integer> createUploadObservable(final String source, String destination)
    {
        if(!checkSendCondition(destination))
            return Observable.error(new InsignificantException("No allowed connection to send"));

        return
            disks.uploadFile(destination, source)
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            internalFiles.markFileAsSent(source);
                        }
                    });

    };
    protected Observable<Integer> createCopyRecordsObservables(final SendingParam sendingParam/*, final String srcPath, final String dstPath*/)
    {
        isSending=true;
        sentFileCount=0;

        return
        Single.fromCallable( ()-> {
                internalFiles.deleteOldFiles();
                String[] fileList = internalFiles.getFileListToSend(settings.getAllowExportingJournal());//getRecordFileList(srcPath);
                settings.setAllowExportingJournal(false);

                sendingParam.totalFileQuantity = fileList.length;
                sendingParam.currentFileCount =0;
                sendingParam.updateNotification();
                return fileList;
        })
        .subscribeOn(Schedulers.io())
        .flatMapObservable(new Function<String[], ObservableSource<String> >(){
            @Override
            public ObservableSource<String> apply(String[] fileList) throws Exception {
                sendingParam.fileEmmiter = new FileEmitter(fileList);
                return sendingParam.fileEmmiter;
            }
        })

        .flatMap(new Function<String, ObservableSource<Integer>>(){
            @Override
            public ObservableSource<Integer> apply(String file) throws Exception {
                sendingParam.currentFileCount++;
                sendingParam.updateNotification();

                final String source =sendingParam.srcPath + file;
                String destination = sendingParam.dstPath + file;
                return createUploadObservable(source, destination)
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                sentFileCount++;
                                sendingParam.fileEmmiter.emitNext();
                            }
                        });
            };
        });
    }
    protected Completable createDeleteOldFilesCompletable(final String dstPath)
    {
        boolean hasDataSizeLimit = settings.getDataSizeLimitation();
        boolean hasFileAmountLimit = settings.getFileAmountLimitation();
        if(!hasDataSizeLimit && !hasFileAmountLimit)
            return Completable.complete();

        final SendingParam notificationInfo=new SendingParam( context.getText(R.string.rec_deleting).toString());

        notification.show(context.getText(R.string.prepare_rec_deleting).toString(), true);

        return
            disks.getResourceInfo(dstPath)
            .observeOn(Schedulers.io())
                    .map(new Function<IDiskIO.ResourceInfo, List<IDiskIO.ResourceInfo>>() {

                        @Override
                        public List<IDiskIO.ResourceInfo> apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                            return filterRecordFileList(resourceInfo.content());
                        }
                    })
                    .reduce(new ArrayList<IDiskIO.ResourceInfo>(),
                            new BiFunction<ArrayList<IDiskIO.ResourceInfo>, List<IDiskIO.ResourceInfo>, ArrayList<IDiskIO.ResourceInfo>>() {

                                @Override
                                public ArrayList<IDiskIO.ResourceInfo> apply(ArrayList<IDiskIO.ResourceInfo> accumulator, List<IDiskIO.ResourceInfo> data) throws Exception {
                                    accumulator.addAll(data);
                                    return accumulator;
                                }
                            })
                            .flatMapObservable(new Function<ArrayList<IDiskIO.ResourceInfo>, Observable<String>>(){

                    @Override
                    public Observable<String> apply(ArrayList<IDiskIO.ResourceInfo> recordList) throws Exception {
                        //List<IDiskIO.ResourceInfo> recordList=filterRecordFileList(resourceInfo.content());
                        List<String> deletableFileList = getDeletableList(recordList);
                        notificationInfo.totalFileQuantity = deletableFileList.size();

                        return Observable.fromIterable(deletableFileList);
                    }
                })
                .flatMapCompletable(new Function<String, CompletableSource>() {
                    @Override
                    public CompletableSource apply(String fileName) throws Exception {
                        notificationInfo.currentFileCount++;
                        notificationInfo.updateNotification();

                        return disks.deleteFile(dstPath+fileName);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

    };

    List<IDiskIO.ResourceInfo> filterRecordFileList(List<IDiskIO.ResourceInfo> fileList)
    {
        List<IDiskIO.ResourceInfo>  res = new ArrayList<>(fileList.size());

        Pattern p = Pattern.compile(RecordFileNameData.RECORD_PATTERN);//"^[0-9]{8}_[0-9]{6}_"); // this pattern filters the other app's files
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

    protected boolean checkSendCondition(String dstPath)
    {
        NetworkInfo ni;
        try {
            if (disks.isLocalStorage(dstPath)) return true;
            ConnectivityManager cm=  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            ni=cm.getActiveNetworkInfo();
        }
        catch(Exception e)
        {
            errorProcessor.onError(e);
            return false;
        };

        if(ni==null || !ni.isConnected()) return false;

        if(ni.getType()==ConnectivityManager.TYPE_MOBILE) {
            if(!settings.getUsingMobileInternet())
                return false;
            if (ni.isRoaming() && !settings.getAllowSendingInRoaming())
                return false;
        }

        return true;

    }

    Subject<Object> onCopyObservable = PublishSubject.create();
    /**
     * emitts rocords were copied from the internal storage
     * @return
     */
    public Observable<Object> getOnRecSentObservable()
    {
        return onCopyObservable;
    }
}
