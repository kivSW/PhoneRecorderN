package com.kivsw.phonerecorder.ui.record_list;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * this class holds a dir's content
 */

class RecListContainer {

    private Context appContext;
    private IErrorProcessor errorProcessor;

    private List<RecordListContract.RecordFileInfo> dirContent,
                                                    cacheDirContent;
    private Map<RecordFileNameData, RecordListContract.RecordFileInfo> cacheDirContentMap;
    private List<RecordListContract.RecordFileInfo> visibleDirContent=null;
    private RecListFilter recListFilter;
    private Disposable filterDisposable;
    private boolean hasData;

    private Subject<RecListContainer> contentReadyObservable;
    private AtomicInteger processingCount=new AtomicInteger(0);

    public RecListContainer(Context appContext, IErrorProcessor errorProcessor)
    {
        this.appContext = appContext;
        this.errorProcessor = errorProcessor;
        dirContent = new ArrayList<>();
        cacheDirContent = new ArrayList<>();
        cacheDirContentMap = new HashMap<>();
        visibleDirContent = Collections.emptyList();

        hasData = false;
        contentReadyObservable = PublishSubject.create();

        createFilter();
    }

    protected void createFilter()
    {
        if(filterDisposable!=null)
            filterDisposable.dispose();
        filterDisposable=null;

        recListFilter=new RecListFilter(errorProcessor);
        recListFilter.getObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RecordListContract.RecordFileInfo>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        filterDisposable = d;
                    }

                    @Override
                    public void onNext(List<RecordListContract.RecordFileInfo> recordFileInfos) {
                        visibleDirContent = recordFileInfos;
                        onChange();
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorProcessor.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    public Subject<RecListContainer> getContentReadyObservable()
    {
        return contentReadyObservable;
    };

    private void checkThread()
    {
        if(Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new RuntimeException("Must be main thread");
    };

    public void clear()
    {
        checkThread();
        dirContent.clear();
        cacheDirContent.clear();
        cacheDirContentMap.clear();

        visibleDirContent=Collections.emptyList();
        hasData=false;
        processingCount.set(0);
        recListFilter.clearData();

        initFileEmmiter();
        // i DO NOT invoke onChange here by intent

    }

    private Disposable fileEmmiterSubscription =null;
    private Subject<BunchOfFiles> fileEmmiter = null;
    private void initFileEmmiter()
    {
        if(fileEmmiterSubscription !=null && !fileEmmiterSubscription.isDisposed())
            fileEmmiterSubscription.dispose();
        fileEmmiterSubscription =null;

        fileEmmiter = PublishSubject.create();
        fileEmmiter
                .flatMap(new Function<BunchOfFiles, ObservableSource<List<RecordListContract.RecordFileInfo>> >(){

                    @Override
                    public ObservableSource<List<RecordListContract.RecordFileInfo>> apply(BunchOfFiles files) throws Exception {
                        return emitFilesAsRecordInfo(files)
                                .doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        processingCount.decrementAndGet();
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RecordListContract.RecordFileInfo>>() {
                    @Override public void onSubscribe(Disposable d) {
                        fileEmmiterSubscription =d;
                    }

                    @Override
                    public void onNext(List<RecordListContract.RecordFileInfo> recordFileInfo) {
                        doAddFileList(recordFileInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        processingCount.set(0);
                        errorProcessor.onError(e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void addFileList(BunchOfFiles bunchOfFiles)
    {
        checkThread();
        hasData = true;

        processingCount.incrementAndGet();

        fileEmmiter.onNext(bunchOfFiles);

    }
    protected void doAddFileList(List<RecordListContract.RecordFileInfo> recordFileInfo)
    {
        checkThread();
        if(recordFileInfo.size()>0) {
            if (recordFileInfo.get(0).fromInternalDir) {
                cacheDirContent.addAll(recordFileInfo);
                for (RecordListContract.RecordFileInfo item : recordFileInfo)
                    cacheDirContentMap.put(item.recordFileNameData, item);
            } else {
                List<RecordListContract.RecordFileInfo> newRecordFileInfo = new ArrayList<>(recordFileInfo.size());
                for (RecordListContract.RecordFileInfo item : recordFileInfo)
                    if (!bindWithCache(item))
                        newRecordFileInfo.add(item);
                recordFileInfo = newRecordFileInfo;
            }

            dirContent.addAll(recordFileInfo);
            recListFilter.addData(recordFileInfo);
        }

        onChange();
    };

    protected boolean bindWithCache(RecordListContract.RecordFileInfo item)
    {
        RecordListContract.RecordFileInfo cacheItem=cacheDirContentMap.get(item.recordFileNameData);
        if(cacheItem==null)
            return false;

        cacheItem.cachedRecordFileInfo = item;
        return true;
    }

    protected  void onChange()
    {
         contentReadyObservable.onNext(this);
    }

    public void setFilter(String filter)
    {
        checkThread();
        visibleDirContent=new ArrayList<>(dirContent.size());
        if(filter==null) filter="";

        recListFilter.clearData();
        recListFilter.setFilter(filter);

        List<RecordListContract.RecordFileInfo> tmpDirContent=new ArrayList(dirContent);
        recListFilter.addData(tmpDirContent);
    }

    public boolean hasData()
    {
        return hasData;
    }
    public List<RecordListContract.RecordFileInfo> getVisibleDirContent()
    {
        return visibleDirContent;
    }
     public boolean isProcessing()
     {
         return processingCount.get()>0 || recListFilter.isProcessing();
     }

    final static int BUNCH_SIZE =20;

    protected Observable<List<RecordListContract.RecordFileInfo>> emitFilesAsRecordInfo(final BunchOfFiles bunchOfFiles)
    {

        final List<IDiskIO.ResourceInfo> fileList = bunchOfFiles.content;
        Iterator<List<RecordListContract.RecordFileInfo>> iterator= new Iterator<List<RecordListContract.RecordFileInfo>>()
                {
                    private int count=0;
                    private Pattern p;
                    private boolean finished=false;

                    protected void init()
                    {
                        Collections.sort(fileList, new Comparator<IDiskIO.ResourceInfo>() {
                            @Override
                            public int compare(IDiskIO.ResourceInfo o1, IDiskIO.ResourceInfo o2) {
                                return o2.name().compareTo(o1.name());
                            }
                        });
                        p = Pattern.compile(RecordFileNameData.RECORD_PATTERN);//"^[0-9]{8}_[0-9]{6}_"); // this pattern filters the other app's files
                    }

                    @Override
                    public boolean hasNext() {
                        return !finished;
                    }

                    @Override
                    public List<RecordListContract.RecordFileInfo> next() {
                        if(count==0)
                            init();

                        List<RecordListContract.RecordFileInfo> res = new ArrayList<>(BUNCH_SIZE);
                        while(count<fileList.size() && res.size()< BUNCH_SIZE)
                        {
                            IDiskIO.ResourceInfo file = fileList.get(count++);
                            if(!file.isFile()) continue;
                            Matcher m = p.matcher(file.name());
                            if(!m.find()) continue;
                            RecordListContract.RecordFileInfo recInfo=getRecordInfo(file.name(),bunchOfFiles.path,bunchOfFiles.cache);
                            res.add(recInfo);
                        }
                        finished = (count >= fileList.size());
                        return res;
                    }

                };

      return Observable.fromIterable(itarableFromIterator(iterator))
         .subscribeOn(Schedulers.io());

    }

    static<T> Iterable<T> itarableFromIterator(final Iterator<T> i)
    {
        return new Iterable<T>() {
            @NonNull
            @Override
            public Iterator<T> iterator() {
                return i;
            }
        };
    }
    protected RecordListContract.RecordFileInfo getRecordInfo(String fileName, String parentDir, boolean fromInternalDir)
    {
        RecordListContract.RecordFileInfo item=new RecordListContract.RecordFileInfo();
        item.recordFileNameData = RecordFileNameData.decipherFileName(fileName);
        item. callerName = getNameFromNumber(item.recordFileNameData.phoneNumber);
        item.fromInternalDir =fromInternalDir;
        item.parentDir = parentDir;
        return item;
    }


    /** finds and returns name that corresponds phoneNumber
     * @return name or null
     * */
    public String getNameFromNumber(String phoneNumber)
    {
        String res = null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            ContentResolver resolver = appContext.getContentResolver();
            Cursor cur = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (cur != null) {
                if (cur.moveToFirst())
                    res = cur.getString(1);

                if (!cur.isClosed()) cur.close();
            }
        }catch(Exception e)
        {
            e.toString();
        }
        if (res == null) res = "";
        return res;
    }


}
