package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.cloudcache.data.CacheFileInfo;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxPresenter;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.IPlayer;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.Settings;
import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.RecordSender;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.SimpleFileReader;
import phonerecorder.kivsw.com.faithphonerecorder.os.MyApplication;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListPresenter
        implements RecordListContract.IRecordListPresenter
{
    private ISettings settings;
    private DiskContainer disks;
    private RecordListContract.IRecordListView view;
    private CloudCache cloudCache;
    private IErrorProcessor errorProcessor;

    private Context appContext;
    private RecListContainer recListContainer;
    //private List<RecordListContract.RecordFileInfo> visibleDirContent;

    private String lastUpdatedDir;
    private Disposable settingsDisposable;

    @Inject
    public RecordListPresenter(Context appContext, ISettings settings, DiskContainer disks, CloudCache cloudCache, IErrorProcessor errorProcessor, RecordSender recordSender)
    {
        this.settings = settings;
        this.disks = disks;
        this.appContext = appContext;
        this.cloudCache = cloudCache;
        this.errorProcessor = errorProcessor;

        settingsDisposable=null;
        lastUpdatedDir = "";

        recListContainer = new RecListContainer(appContext, errorProcessor);
        recListContainer.getContentReadyObservable()
                .sample(333, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RecListContainer>() {
                            @Override public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(RecListContainer recListContainer) {
                                setVisibleDirContent(recListContainer.getVisibleDirContent(), false);
                                updateViewProgressBarVisible();
                            }

                            @Override
                            public void onError(Throwable e) {
                                RecordListPresenter.this.errorProcessor.onError(e);
                            }

                            @Override public void onComplete() {}
                        });

        if(recordSender!=null)
            recordSender.getOnRecSentObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Object>() {
                @Override  public void onSubscribe(Disposable d) { }

                @Override  public void onNext(Object aBoolean) {
                    lazyUpdateDir();
                }

                @Override  public void onError(Throwable e) { }

                @Override public void onComplete() {}
            });
    };

    protected IPlayer providePlayerInstance()
    {
        if(settings.getUseInternalPlayer())
            return MyApplication.getComponent().getInnerPlayer();
        else
            return MyApplication.getComponent().getAndroidPlayer();
    }


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
        if(recListContainer.hasData())
                  setVisibleDirContent(recListContainer.getVisibleDirContent(), false);
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
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String id) throws Exception {
                                switch(id) {
                                    case Settings.PATH_HISTORY:
                                            if (!settings.getCurrentViewUrlPath().equals(lastUpdatedDir)) {
                                                updateDir(true);
                                            }
                                            break;
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
    public void chooseCurrentDir() {
        MvpRxSelectDirDialogPresenter selDirPresenter = MvpRxSelectDirDialogPresenter
                .createDialog(view.getContext(), view.getFragmentManager(), disks, settings.getSavingUrlPath(), null);

        selDirPresenter.getMaybe().subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {  }

            @Override
            public void onSuccess(String filePath) {
                setCurrentDir(filePath);
            };

            @Override
            public void onError(Throwable e) {
                errorProcessor.onError(e);
            };

            @Override
            public void onComplete() {};
        });

    }
    @Override
    public void setCurrentDir(String filePath)
    {
        if(settings.getCurrentViewUrlPath().equals( filePath)  )
            return;
         settings.addToViewUrlPathHistory(filePath);
         //updateDir(true); WE don't need updateDir due to subscribeSettings()
    };

    protected void lazyUpdateDir()
    {
        if(view==null)
            recListContainer.clean();
        else
            updateDir(false);
    };


    private Disposable updateDirDisposable=null;
    @Override
    public void updateDir(final boolean clearCurrentData)
    {
        setProgressBarVisible(true);

        final String dirPath = settings.getCurrentViewUrlPath();
        lastUpdatedDir = dirPath;

        if(clearCurrentData)
           setVisibleDirContent(Collections.<RecordListContract.RecordFileInfo>emptyList(), true);

        if(updateDirDisposable!=null)
        {
            updateDirDisposable.dispose();
            updateDirDisposable=null;
        };
        disks.authorizeIfNecessary(dirPath)
             .andThen(Single.just("") )
             .flatMap(new Function<String, SingleSource<IDiskIO.ResourceInfo>>() {
                @Override
                public SingleSource<IDiskIO.ResourceInfo> apply(@NonNull String s) throws Exception {
                    return disks.getResourceInfo(dirPath);
                }
             })
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new SingleObserver<IDiskIO.ResourceInfo>(){
                        @Override public void onSubscribe(Disposable d) {
                            updateDirDisposable = d;
                        }

                        @Override
                        public void onSuccess(IDiskIO.ResourceInfo dir) {
                           recListContainer.setFileList(dir.content());
                           setProgressBarVisible(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            errorProcessor.onError(e);
                            setProgressBarVisible(false);
                        }


              });
    };


    private boolean progressBarVisible=false;
    protected void setProgressBarVisible(boolean visible)
    {
        progressBarVisible = visible;

        updateViewProgressBarVisible();
    };
    protected void updateViewProgressBarVisible()
    {
        if(view!=null) {
            boolean v= (progressBarVisible) || (recListContainer.isProcessing());
            view.setRecListProgressBarVisible(v);
        }
    }

    protected void setVisibleDirContent(List<RecordListContract.RecordFileInfo> aFilteredDirContent, boolean scrollToBegin)
    {
        //visibleDirContent = aFilteredDirContent;
        int i=0;

        for(RecordListContract.RecordFileInfo item:aFilteredDirContent)
            item.visiblePosition=i++;

        if(view!=null)
        {
            view.setRecordList(aFilteredDirContent, scrollToBegin);
        };
    }


  /*  protected void addDirContent(List<RecordListContract.RecordFileInfo> recordList, boolean scrollToBegin) {

        if(needToClearDirContent) {
            this.dirContent = recordList;
            filterContent(scrollToBegin);
        }
        else {
            this.dirContent.addAll(recordList);
            addFilterContent(scrollToBegin);
        }


    }*/

    //private String filter;
    @Override
    public void setFilter(final String aFilter)
    {
        //this.filter = aFilter;
        recListContainer.setFilter(aFilter);
        //filterContent(true);
    }

    @Override
    public void setUndelitable(int pos, boolean isProtected) {
        try {
            final RecordListContract.RecordFileInfo recordFileInfo = recListContainer.getVisibleDirContent().get(pos);//visibleDirContent.get(pos);

            final String dirPath = settings.getCurrentViewUrlPath();
            String oldPath = dirPath + recordFileInfo.recordFileNameData.origFileName;
            recordFileInfo.recordFileNameData.isProtected = isProtected;
            final String newFileName = recordFileInfo.recordFileNameData.buildFileName();
            String newPath = dirPath + newFileName;
            notifyRecordChange(recordFileInfo);

            disks
                    .renameFile(oldPath, newPath)
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        ;

                        @Override
                        public void onComplete() {
                            notifyRecordChange(recordFileInfo);
                            recordFileInfo.recordFileNameData.origFileName = newFileName;
                        }

                        ;

                        @Override
                        public void onError(Throwable e) {
                            errorProcessor.onError(e);
                            recordFileInfo.recordFileNameData = RecordFileNameData.decipherFileName(recordFileInfo.recordFileNameData.origFileName);
                            notifyRecordChange(recordFileInfo);
                        }

                        ;
                    });
        }catch(Exception e)
        {
            errorProcessor.onError(e);
        }


    }

    @Override
    public void playItem(int pos) {
        try{

            final RecordListContract.RecordFileInfo recordFileInfo = recListContainer.getVisibleDirContent().get(pos);//visibleDirContent.get(pos);

            getCachedFile(recordFileInfo)
                    .subscribe(new SingleObserver<CacheFileInfo>() {
                        @Override public void onSubscribe(Disposable d) {}

                        @Override
                        public void onSuccess(CacheFileInfo cacheFileInfo) {
                                if(recordFileInfo.recordFileNameData.isSMS)
                                    showSMS(recordFileInfo, cacheFileInfo.localName);
                                else
                                    if(view!=null) {
                                        IPlayer player = providePlayerInstance();
                                        player.setUiParam(recordFileInfo.callerName, recordFileInfo.recordFileNameData);
                                        player.play(view.getContext(), cacheFileInfo.localName);
                                    }
                        }

                        @Override
                        public void onError(Throwable e) {}// error is processed in getCachedFile()
                    });
        }catch(Exception e)
        {
            errorProcessor.onError(e);
        }

    }

    @Override
    public void playItemWithPlayerChoosing(int pos) {
        try{

            final RecordListContract.RecordFileInfo recordFileInfo = recListContainer.getVisibleDirContent().get(pos);//visibleDirContent.get(pos);

            getCachedFile(recordFileInfo)
                    .subscribe(new SingleObserver<CacheFileInfo>() {
                        @Override public void onSubscribe(Disposable d) {}

                        @Override
                        public void onSuccess(CacheFileInfo cacheFileInfo) {
                            if(recordFileInfo.recordFileNameData.isSMS)
                                showSMS(recordFileInfo, cacheFileInfo.localName);
                            else
                                if(view!=null) {
                                    IPlayer player = providePlayerInstance();
                                    player.playItemWithChooser(view.getContext(), cacheFileInfo.localName);
                                }
                        }

                        @Override
                        public void onError(Throwable e) {}// error is processed in getCachedFile()
                    });
        }catch(Exception e)
        {
            errorProcessor.onError(e);
        }
    }

    protected Single<CacheFileInfo> getCachedFile(final RecordListContract.RecordFileInfo recordFileInfo)
    {
        String filePath = settings.getCurrentViewUrlPath() + recordFileInfo.recordFileNameData.origFileName;
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
                        errorProcessor.onError(t);
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
        List<RecordListContract.RecordFileInfo> visibleDirContent=recListContainer.getVisibleDirContent();
        if(visibleDirContent==null) return;

        for(RecordListContract.RecordFileInfo item: visibleDirContent)
            item.selected=false;

        if(view!=null) view.onRecordListChanged();
    }

    @Override
    public void selectAll() {
        List<RecordListContract.RecordFileInfo> visibleDirContent=recListContainer.getVisibleDirContent();
        if(visibleDirContent==null) return;

        for(RecordListContract.RecordFileInfo item: visibleDirContent)
            item.selected=true;
        if(view!=null) view.onRecordListChanged();
    }

    @Override
    public void selectItem(int pos, boolean selected) {
        List<RecordListContract.RecordFileInfo> visibleDirContent=recListContainer.getVisibleDirContent();
        if(visibleDirContent==null) return;

        RecordListContract.RecordFileInfo item=visibleDirContent.get(pos);
        item.selected = selected;
        notifyRecordChange(item);
    }

    @Override
    public boolean hasSelectedItem(boolean excludeProtected)
    {
        List<RecordListContract.RecordFileInfo> visibleDirContent=recListContainer.getVisibleDirContent();
        if(visibleDirContent==null) return false;

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
        List<RecordListContract.RecordFileInfo> visibleDirContent=recListContainer.getVisibleDirContent();
        if(visibleDirContent==null) return new ArrayList<>();

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
        /*StorageUtils.CloudFile cloudFile = getCloudFile();
        final String path=cloudFile.getPath();
        final IDiskIO diskIo=cloudFile.diskRepresenter.getDiskIo();*/

        final String dirPath = settings.getCurrentViewUrlPath();

        setProgressBarVisible(true);

        Observable.fromIterable(selectedFiles)
            .observeOn(Schedulers.io())
            .flatMapCompletable(new Function<RecordListContract.RecordFileInfo, CompletableSource>() {
                @Override
                public CompletableSource apply(RecordListContract.RecordFileInfo recordFileInfo) throws Exception {
                    return disks.deleteFile(dirPath+recordFileInfo.recordFileNameData.origFileName);
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
                    errorProcessor.onError(e);
                }
            });


    }


}
