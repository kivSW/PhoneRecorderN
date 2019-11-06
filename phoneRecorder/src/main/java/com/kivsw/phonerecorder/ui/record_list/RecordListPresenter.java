package com.kivsw.phonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;
import com.kivsw.cloudcache.data.CacheFileInfo;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxPresenter;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;
import com.kivsw.phonerecorder.model.addrbook.IAddrBook;
import com.kivsw.phonerecorder.model.addrbook.PhoneAddrBook;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.player.IPlayer;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.Settings;
import com.kivsw.phonerecorder.model.task_executor.tasks.CallRecorder;
import com.kivsw.phonerecorder.model.task_executor.tasks.RecordSender;
import com.kivsw.phonerecorder.model.task_executor.tasks.SmsReader;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.model.utils.SimpleFileIO;
import com.kivsw.phonerecorder.os.MyApplication;
import com.kivsw.phonerecorder.ui.record_list.operations.DeleteRecordsOperation;
import com.kivsw.phonerecorder.ui.record_list.operations.ReadRecordListOperation;
import com.kivsw.phonerecorder.ui.record_list.operations.SetUndeletableFlagOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 *
 */

public class RecordListPresenter
        implements RecordListContract.IRecordListPresenter
{
    private ISettings settings;
    private DiskContainer disks;
    private ReadRecordListOperation readRecordListOperation;
    private DeleteRecordsOperation deleteRecordsOperation;
    private SetUndeletableFlagOperator setUndeletableFlagOperator;
    private RecordListContract.IRecordListView view;
    private CloudCache cloudCache;
    private IErrorProcessor errorProcessor;

    private Context appContext;
    private RecListContainer recListContainer;
    //private List<RecordListContract.RecordFileInfo> visibleDirContent;

    private String lastUpdatedDir;
    private Disposable settingsDisposable;


    @Inject
    RecordListPresenter(Context appContext, ISettings settings, DiskContainer disks, CloudCache cloudCache,
                        ReadRecordListOperation readRecordListOperation, DeleteRecordsOperation deleteRecordsOperation, SetUndeletableFlagOperator setUndeletableFlagOperator,
                        IErrorProcessor errorProcessor, RecordSender recordSender, CallRecorder callRecorder, SmsReader smsReader, PhoneAddrBook phoneAddrBook)
    {
        this.settings = settings;
        this.disks = disks;
        this.appContext = appContext;
        this.cloudCache = cloudCache;
        this.readRecordListOperation = readRecordListOperation;
        this.deleteRecordsOperation = deleteRecordsOperation;
        this.setUndeletableFlagOperator = setUndeletableFlagOperator;
        this.errorProcessor = errorProcessor;

        settingsDisposable=null;
        lastUpdatedDir = "";

        recListContainer = new RecListContainer(appContext, errorProcessor, phoneAddrBook);
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
                                updateViewProgressBarVisible();
                            }

                            @Override public void onComplete() {
                                updateViewProgressBarVisible();
                            }
                        });

        Observer updateObserver=new Observer<Object>() {
            @Override  public void onSubscribe(Disposable d) { }

            @Override  public void onNext(Object aBoolean) {
                lazyUpdateDir();
            }

            @Override  public void onError(Throwable e) { }

            @Override public void onComplete() {}
        };

        if(recordSender!=null)
            recordSender.getOnRecSentObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateObserver);
        if(callRecorder!=null)
            callRecorder.getOnRecordObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateObserver);
        if(smsReader!=null)
            smsReader.getOnNewSmsReadObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateObserver);
    }

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
            }

            @Override
            public void onError(Throwable e) {
                errorProcessor.onError(e);
            }

            @Override
            public void onComplete() {}
        });

    }
    @Override
    public void setCurrentDir(String filePath)
    {
        if(settings.getCurrentViewUrlPath().equals( filePath)  )
            return;
         settings.addToViewUrlPathHistory(filePath);
         //updateDir(true); WE don't need updateDir due to subscribeSettings()
    }

    protected void lazyUpdateDir()
    {
        if(view==null)
            recListContainer.clear();
        else
            updateDir(false);
    }


    private Disposable updateDirDisposable=null;
    @Override
    public void updateDir(final boolean clearCurrentData)
    {
        setProgressBarVisible(true);
        isFileListLoading=true;

        lastUpdatedDir = settings.getCurrentViewUrlPath();

        if(clearCurrentData)
           setVisibleDirContent(Collections.<RecordListContract.RecordFileInfo>emptyList(), true);

        if(updateDirDisposable!=null)
        {
            updateDirDisposable.dispose();
            updateDirDisposable=null;
        }

        recListContainer.clear();
        readRecordListOperation.getAllDirectoryContent(lastUpdatedDir)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<Object>(){
                        @Override public void onSubscribe(Disposable d) {
                            updateDirDisposable = d;
                        }

                        @Override
                        public void onNext(Object data) {
                            if(data instanceof IAddrBook)
                                recListContainer.setAddrBook((IAddrBook)data);
                            if(data instanceof BunchOfFiles)
                                recListContainer.addFileList((BunchOfFiles)data);
                        }

                        @Override
                        public void onError(Throwable e) {
                            recListContainer.addFileList(BunchOfFiles.getEmptyInstance());
                            errorProcessor.onSmallError(e);
                            setProgressBarVisible(false);
                            isFileListLoading=false;
                        }

                        @Override
                        public void onComplete() {
                            setProgressBarVisible(false);
                            isFileListLoading=false;
                        }


              });
    }


    private boolean isFileListLoading=false;
    protected boolean isFileListLoading()
    {
        return isFileListLoading || recListContainer.isProcessing();
    }

    private boolean progressBarVisible=false;
    protected void setProgressBarVisible(boolean visible)
    {
        progressBarVisible = visible;
        updateViewProgressBarVisible();
    }

    protected void updateViewProgressBarVisible()
    {
        if(view!=null) {
            boolean v= (progressBarVisible) || isFileListLoading();
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
        }
    }

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

            recordFileInfo.recordFileNameData.isProtected = isProtected; // updates UI state
            notifyRecordChange(recordFileInfo);

            setUndeletableFlagOperator.setUndeletableFlag(recordFileInfo, isProtected, !isFileListLoading())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onComplete() {
                            notifyRecordChange(recordFileInfo);
                        }

                        @Override
                        public void onError(Throwable e) {
                            errorProcessor.onError(e);
                            recordFileInfo.recordFileNameData = RecordFileNameData.decipherFileName(recordFileInfo.recordFileNameData.origFileName);
                            notifyRecordChange(recordFileInfo);
                        }

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
                                        player.setUiLabels(recordFileInfo.callerName, recordFileInfo.recordFileNameData);
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
        String filePath = recordFileInfo.getFileFullPath();//settings.getCurrentViewUrlPath() + recordFileInfo.recordFileNameData.origFileName;
        recordFileInfo.percentage=0;
        recordFileInfo.isDownloading=true;
        notifyRecordChange(recordFileInfo);

        Observable<CacheFileInfo> res= cloudCache.getFileFromCacheOrDownload(filePath)
                .filter(new Predicate<CacheFileInfo>() {
                    @Override
                    public boolean test(CacheFileInfo cacheFileInfo) throws Exception {
                        if(cacheFileInfo.localName==null) {
                            recordFileInfo.percentage=cacheFileInfo.progress;
                            //notifyRecordChange(recordFileInfo);
                        }
                        return cacheFileInfo.localName!=null;
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

        String title, lbl;
        StringBuilder text = new StringBuilder();

        if(recordFileInfo.recordFileNameData.income)
            lbl = appContext.getText(R.string.from).toString();
        else
            lbl = appContext.getText(R.string.to).toString();

        title = "SMS";

        text.append("<font face=\"monospace\" >");
        text.append("<b>");    text.append(lbl);    text.append(" ");  text.append("</b>");

        if(recordFileInfo.callerName.isEmpty()) {
            text.append("<big>");
            text.append(recordFileInfo.recordFileNameData.phoneNumber);
            text.append("</big>");
        }
        else {
            text.append("<big>");
            text.append(recordFileInfo.callerName);
            text.append("</big><br>");

            text.append("<b>");
            for (int i=-1;i<lbl.length();i++)
                text.append("&nbsp");
            text.append("</b>");

            text.append("<small>");
            text.append(recordFileInfo.recordFileNameData.phoneNumber);
            text.append("</small>");
        }
        text.append("</font>");

        text.append("<br><br><i>");
        text.append( SimpleFileIO.readFile(localFileName) );
        text.append("</i>");

        MvpMessageBoxBuilder.newInstance()
                .setText(title, text.toString())
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
    }

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
    }

    protected void  doDelete(List<RecordListContract.RecordFileInfo> selectedFiles)
    {
        setProgressBarVisible(true);

        deleteRecordsOperation.deleteRecords(selectedFiles, !isFileListLoading())
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
