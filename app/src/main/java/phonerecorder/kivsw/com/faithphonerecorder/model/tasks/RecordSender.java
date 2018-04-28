package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;
import android.net.Uri;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloud.disk.StorageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

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
                File dir=new File(srcPath);
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
        })
        .subscribeOn(Schedulers.io())
        .flatMapObservable(new Function<String[], ObservableSource<String> >(){

            @Override
            public ObservableSource<String> apply(String[] fileList) throws Exception {
                return Observable.fromArray(fileList);
            }
        } )
        .flatMap(new Function<String, ObservableSource<Integer>>(){
            @Override
            public ObservableSource<Integer> apply(String file) throws Exception {
                final String source =srcPath + file;
                String destination = dstPath + file;
                return diskIO.uploadFile(destination, source)
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
