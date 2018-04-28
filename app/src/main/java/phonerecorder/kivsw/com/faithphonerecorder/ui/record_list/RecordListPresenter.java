package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloud.disk.StorageUtils;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.cloudcache.data.CacheFileInfo;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxPresenter;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.IPlayer;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.SimpleFileReader;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListPresenter
        implements RecordListContract.IRecordListPresenter
{
    private ISettings settings;
    private IPlayer player;
    private List<IDiskRepresenter> diskList;
    private RecordListContract.IRecordListView view;
    private CloudCache cloudCache;
    private Context appContext;
    private List<RecordListContract.RecordFileInfo> dirContent;
    private List<RecordListContract.RecordFileInfo> visibleDirContent;

    private String lastUpdatedDir;
    private Disposable settingsDisposable;

    @Inject
    public RecordListPresenter(Context appContext, ISettings settings, IPlayer player, List<IDiskRepresenter> diskList,CloudCache cloudCache )
    {
        this.settings = settings;
        this.player = player;
        this.diskList = diskList;
        this.appContext = appContext;
        this.cloudCache = cloudCache;
        settingsDisposable=null;
        lastUpdatedDir = "";
    };

    @Override
    public Contract.IView getUI() {
        return view;
    }

    @Override
    public void setUI(Contract.IView view) {
        this.view = (RecordListContract.IRecordListView)view;
        this.view.setSettings(settings);
        subscribeSettings();
        updateViewProgressBarVisible();
        if(visibleDirContent !=null)  setVisibleDirContent(visibleDirContent, false);
        else updateDir(true);
    }

    @Override
    public void removeUI() {
        unsubscribeSettings();
        this.view = null;
    }

    protected void subscribeSettings()
    {
        unsubscribeSettings();
        settingsDisposable =
                settings.getObservable()
                        .subscribe(new Consumer<ISettings>() {
                            @Override
                            public void accept(ISettings iSettings) throws Exception {
                                if(!iSettings.getCurrentPathView().equals(lastUpdatedDir)) {
                                    updateDir(true);
                                }
                            }
                        });
    }
    protected void unsubscribeSettings()
    {
        if(settingsDisposable!=null)
            settingsDisposable.dispose();
        settingsDisposable=null;
    }

    @Override
    public void onError(String message)
    {
        MvpMessageBoxBuilder builder=new MvpMessageBoxBuilder();

        String error = appContext.getText(R.string.error).toString();
        builder.setText(error, message)
                .build(view.getFragmentManager());

    };

    @Override
    public void chooseCurrentDir() {
        MvpRxSelectDirDialogPresenter selDirPresenter = MvpRxSelectDirDialogPresenter
                .createDialog(view.getContext(), view.getFragmentManager(), diskList, settings.getSavingPath(), null);

        selDirPresenter.getMaybe().subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {  }

            @Override
            public void onSuccess(String filePath) {
                setCurrentDir(filePath);
            };

            @Override
            public void onError(Throwable e) {
                RecordListPresenter.this.onError(e.toString());
            };

            @Override
            public void onComplete() {};
        });

    }
    @Override
    public void setCurrentDir(String filePath)
    {
        if(settings.getCurrentPathView().equals( filePath)  )
            return;
         settings.addToPathViewHistory(filePath);
         updateDir(true);
    };

    protected StorageUtils.CloudFile getCloudFile()
    {
        String filePath = settings.getCurrentPathView();
        StorageUtils.CloudFile cloudFile
                =StorageUtils.parseFileName(filePath, diskList);
        return cloudFile;
    }
    protected IDiskIO getCurrectDiskIO()
    {
        return getCloudFile().diskRepresenter.getDiskIo();
    }

    @Override
    public void updateDir(final boolean scrollToBegin)
    {
        setProgressBarVisible(true);

        getCurrectDiskIO()
                  .authorizeIfNecessary()
                  .andThen(getCurrectDiskIO().getResourceInfo(getCloudFile().getPath()))
                  .observeOn(Schedulers.io())
                  .map(new Function<IDiskIO.ResourceInfo, List<RecordListContract.RecordFileInfo>>(){

                        @Override
                        public List<RecordListContract.RecordFileInfo> apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                            List<IDiskIO.ResourceInfo> fileList=resourceInfo.content();
                            List<RecordListContract.RecordFileInfo> res = new ArrayList<>(fileList.size());
                            Pattern p = Pattern.compile(RecordFileNameData.PATTERN);//"^[0-9]{8}_[0-9]{6}_"); // this pattern filters the other app's files
                            for(IDiskIO.ResourceInfo file:fileList)
                            {
                                if(!file.isFile()) continue;
                                Matcher m = p.matcher(file.name());
                                if(!m.find()) continue;
                                res.add( getRecordInfo(file.name()) );
                            };
                            Collections.sort(res, new Comparator<RecordListContract.RecordFileInfo>(){
                                @Override
                                public int compare(RecordListContract.RecordFileInfo o1, RecordListContract.RecordFileInfo o2) {
                                    return o2.recordFileNameData.origFileName.compareTo(o1.recordFileNameData.origFileName);
                                }
                            });
                            return res;
                        }
                    })

                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new SingleObserver<List<RecordListContract.RecordFileInfo>>(){
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(List<RecordListContract.RecordFileInfo> recordList) {
                                setDirContent(recordList, scrollToBegin);
                                setProgressBarVisible(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                RecordListPresenter.this.onError(e.toString());
                                setProgressBarVisible(false);
                            }
                        });
    };
    protected RecordListContract.RecordFileInfo getRecordInfo(String fileName)
    {
        RecordListContract.RecordFileInfo item=new RecordListContract.RecordFileInfo();
        item.recordFileNameData = RecordFileNameData.decipherFileName(fileName);
        item. callerName = getNameFromNumber(item.recordFileNameData.phoneNumber);
        return item;
    }

    private int progressBarVisible=0;
    protected void setProgressBarVisible(boolean visible)
    {
        if(visible)  progressBarVisible++;
        else  progressBarVisible--;

        updateViewProgressBarVisible();
    };
    protected void updateViewProgressBarVisible()
    {
        if(view!=null)
            view.setRecListProgressBarVisible(progressBarVisible>0);
    }

    protected void setVisibleDirContent(List<RecordListContract.RecordFileInfo> aFilteredDirContent, boolean scrollToBegin)
    {
        visibleDirContent = aFilteredDirContent;
        int i=0;

        for(RecordListContract.RecordFileInfo item:aFilteredDirContent)
            item.visiblePosition=i++;

        if(view!=null)
        {
            view.setRecordList(visibleDirContent, scrollToBegin);
        };
    }

    protected void setDirContent(List<RecordListContract.RecordFileInfo> recordList, boolean scrollToBegin) {
       this.dirContent=recordList;
       filterContent(scrollToBegin);
    }

    private String filter;
    @Override
    public void setFilter(final String aFilter)
    {
        this.filter = aFilter;
        filterContent(true);
    }

    @Override
    public void setUndelitable(int pos, boolean isProtected) {

       final RecordListContract.RecordFileInfo recordFileInfo=visibleDirContent.get(pos);

       String oldPath = getCloudFile().getPath() + recordFileInfo.recordFileNameData.origFileName;
       recordFileInfo.recordFileNameData.isProtected = isProtected;
       final String newFileName=recordFileInfo.recordFileNameData.buildFileName();
       String newPath = getCloudFile().getPath() + newFileName;
       notifyRecordChange(recordFileInfo);

       getCurrectDiskIO()
       .renameFile(oldPath, newPath)
       .subscribe(new CompletableObserver() {
                   @Override
                   public void onSubscribe(Disposable d) {

                   };

                   @Override
                   public void onComplete() {
                       notifyRecordChange(recordFileInfo);
                       recordFileInfo.recordFileNameData.origFileName = newFileName;
                   };

                   @Override
                   public void onError(Throwable e) {
                       RecordListPresenter.this.onError(e.toString());
                       notifyRecordChange(recordFileInfo);
                   };
               });
    }

    @Override
    public void playItem(int pos) {
        final RecordListContract.RecordFileInfo recordFileInfo = visibleDirContent.get(pos);
        getCachedFile(recordFileInfo)
                .subscribe(new SingleObserver<CacheFileInfo>() {
                    @Override public void onSubscribe(Disposable d) {}

                    @Override
                    public void onSuccess(CacheFileInfo cacheFileInfo) {
                            if(recordFileInfo.recordFileNameData.isSMS)
                                showSMS(recordFileInfo, cacheFileInfo.localName);
                            else
                                if(view!=null)
                                   player.play(view.getContext(), cacheFileInfo.localName);
                    }

                    @Override
                    public void onError(Throwable e) {}// error is processed in getCachedFile()
                });

    }

    @Override
    public void playItemWithPlayerChoosing(int pos) {
        final RecordListContract.RecordFileInfo recordFileInfo = visibleDirContent.get(pos);
        getCachedFile(recordFileInfo)
                .subscribe(new SingleObserver<CacheFileInfo>() {
                    @Override public void onSubscribe(Disposable d) {}

                    @Override
                    public void onSuccess(CacheFileInfo cacheFileInfo) {
                        if(recordFileInfo.recordFileNameData.isSMS)
                            showSMS(recordFileInfo, cacheFileInfo.localName);
                        else
                            if(view!=null)
                                player.playItemWithChooser(view.getContext(),cacheFileInfo.localName);
                    }

                    @Override
                    public void onError(Throwable e) {}// error is processed in getCachedFile()
                });
    }

    protected Single<CacheFileInfo> getCachedFile(final RecordListContract.RecordFileInfo recordFileInfo)
    {
        String filePath = settings.getCurrentPathView() + recordFileInfo.recordFileNameData.origFileName;
        recordFileInfo.percentage=0;
        recordFileInfo.isDownloading=true;
        notifyRecordChange(recordFileInfo);

        Observable res= cloudCache.getFileFromCache(filePath)
                .filter(new Predicate() {
                    @Override
                    public boolean test(Object event) throws Exception {
                        if(event instanceof Integer) {
                            recordFileInfo.percentage=(((Integer) event).intValue());
                            notifyRecordChange(recordFileInfo);
                        }
                        return event instanceof CacheFileInfo;
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        recordFileInfo.isDownloading=false;
                        notifyRecordChange(recordFileInfo);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    public void accept(@NonNull Throwable t)
                    {
                        recordFileInfo.isDownloading=false;
                        notifyRecordChange(recordFileInfo);
                        RecordListPresenter.this.onError(t.toString());
                    }
                });

        return Single.fromObservable(res);

    }
    protected void notifyRecordChange(RecordListContract.RecordFileInfo recordFileInfo)
    {
        if (view != null)
                            view.onRecordChanged(recordFileInfo.visiblePosition);
    }

    private void showSMS(RecordListContract.RecordFileInfo recordFileInfo, String localFileName)
    {
        if(view==null)
            return;

        String title, text;
        if(recordFileInfo.recordFileNameData.income)
            title = appContext.getText(R.string.from) + recordFileInfo.recordFileNameData.phoneNumber + " "+recordFileInfo.callerName;
        else
            title = appContext.getText(R.string.to) + recordFileInfo.recordFileNameData.phoneNumber + " "+recordFileInfo.callerName;
        text = SimpleFileReader.readFile(localFileName);


        MvpMessageBoxBuilder.newInstance()
                .setText(title, text)
                .build(view.getFragmentManager());

    }

    @Override
    public void unselectAll() {
        for(RecordListContract.RecordFileInfo item: visibleDirContent)
            item.selected=false;

        if(view!=null) view.onRecordListChanged();
    }

    @Override
    public void selectAll() {
        for(RecordListContract.RecordFileInfo item: visibleDirContent)
            item.selected=true;
        if(view!=null) view.onRecordListChanged();
    }

    @Override
    public void selectItem(int pos, boolean selected) {
        RecordListContract.RecordFileInfo item=visibleDirContent.get(pos);
        item.selected = selected;
        notifyRecordChange(item);
    }

    @Override
    public boolean hasSelectedItem(boolean excludeProtected)
    {
        for(RecordListContract.RecordFileInfo item: visibleDirContent)
        {
            if(excludeProtected && item.recordFileNameData.isProtected)
                continue;
            if(item.selected)
                return true;
        };
        return false;
    };

    List<RecordListContract.RecordFileInfo> getSelectedItems(boolean excludeProtected)
    {
        ArrayList<RecordListContract.RecordFileInfo> res = new ArrayList<>(visibleDirContent.size());
        for(RecordListContract.RecordFileInfo item: visibleDirContent)
        {
            if(excludeProtected && item.recordFileNameData.isProtected)
                continue;
            if(item.selected)
                res.add(item);
        }
        return res;
    }

    @Override
    public void deleteSelectedItems() {

        final List<RecordListContract.RecordFileInfo> selectedFiles=getSelectedItems(true);
        if(selectedFiles.size()==0)
            return;

        MvpMessageBoxBuilder.newInstance()
                .setText(appContext.getText(R.string.confirmation),
                         String.format(Locale.US, appContext.getText(R.string.deleteConfirmation).toString(), selectedFiles.size()) )
                .setOkButton(appContext.getText(R.string.yes))
                .setCancelButton(appContext.getText(R.string.no))
                .build(view.getFragmentManager())
                .getSingle()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer btnNum) throws Exception {
                        if(btnNum.intValue()==MvpMessageBoxPresenter.OK_BUTTON)
                            doDelete(selectedFiles);
                    }
                });
    };

    protected void  doDelete(List<RecordListContract.RecordFileInfo> selectedFiles)
    {
        StorageUtils.CloudFile cloudFile = getCloudFile();
        final String path=cloudFile.getPath();
        final IDiskIO diskIo=cloudFile.diskRepresenter.getDiskIo();
//        AsyncOperationCounter fileCounter = new AsyncOperationCounter();
        setProgressBarVisible(true);

        Observable.fromIterable(selectedFiles)
                .observeOn(Schedulers.io())
                .flatMapCompletable(new Function<RecordListContract.RecordFileInfo, CompletableSource>() {
                    @Override
                    public CompletableSource apply(RecordListContract.RecordFileInfo recordFileInfo) throws Exception {
                        return diskIo.deleteFile(path+recordFileInfo.recordFileNameData.origFileName);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onComplete() {
                        updateDir(false);
                        setProgressBarVisible(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        setProgressBarVisible(false);
                        RecordListPresenter.this.onError(e.toString());
                    }
                });


    }

    protected void filterContent(final boolean scrollToBegin)
    {
        final List<RecordListContract.RecordFileInfo> tmpDirContent = this.dirContent;
        final String tmpFilter = filter;

        setProgressBarVisible(true);

        Single.fromCallable(new Callable<List<RecordListContract.RecordFileInfo>>() {
            @Override
            public List<RecordListContract.RecordFileInfo> call() throws Exception {
                return doFilterContent(tmpDirContent, tmpFilter);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<RecordListContract.RecordFileInfo>>() {
            @Override
            public void accept(List<RecordListContract.RecordFileInfo> recordList) throws Exception {
                setVisibleDirContent(recordList,scrollToBegin);
                setProgressBarVisible(false);
            }
       });
    }

    protected List<RecordListContract.RecordFileInfo> doFilterContent(List<RecordListContract.RecordFileInfo> dirContent, String filter)
    {

        if(filter==null || filter.length()==0)
            return dirContent;

        filter = filter.toLowerCase();

        ArrayList<RecordListContract.RecordFileInfo> resList=new ArrayList(dirContent.size());
        for (RecordListContract.RecordFileInfo item : dirContent) {
            if ( checkFilter(item, filter) )
                resList.add(item);
        };
        return resList;
    }
    protected boolean checkFilter(RecordListContract.RecordFileInfo fileData, String filter)
    {
        if (fileData.recordFileNameData.phoneNumber.indexOf(filter) >= 0) return true;
        if (fileData.callerName.toLowerCase().indexOf(filter) >= 0) return true;

        return false;
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
        {}
        if (res == null) res = "";
        return res;
    };


}
