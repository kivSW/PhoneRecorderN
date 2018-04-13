package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloud.disk.StorageUtils;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.MaybeObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.FileNameData;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListPresenter
        implements RecordListContract.IRecordListPresenter
{
    private ISettings settings;
    private List<IDiskRepresenter> diskList;
    private RecordListContract.IRecordListView view;
    private List<IDiskIO.ResourceInfo> fileList;
    private Context appContext;
    private List<RecordListContract.RecordFileInfo> dirContent;
    private List<RecordListContract.RecordFileInfo> filteredDirContent;

    public RecordListPresenter(Context appContext, ISettings settings, List<IDiskRepresenter> diskList)
    {
        this.settings = settings;
        this.diskList = diskList;
        this.appContext = appContext;
    }

    @Override
    public Contract.IView getUI() {
        return null;
    }

    @Override
    public void setUI(Contract.IView view) {
        this.view = (RecordListContract.IRecordListView)view;
        updateViewProgressBarVisible();
        if(filteredDirContent!=null)  setFilteredDirContent(filteredDirContent);
        else updateDir();

    }

    @Override
    public void removeUI() {
        this.view = null;
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
        String path=settings.getCurrentPathView();
        MvpRxSelectDirDialogPresenter selDirPresenter = MvpRxSelectDirDialogPresenter
                .createDialog(view.getContext(), view.getFragmentManager(), diskList, settings.getSavingPath(), null);

        selDirPresenter.getMaybe().subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {  }

            @Override
            public void onSuccess(String filePath) {
                 settings.addToPathViewHistory(filePath);
                 updateDir();
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
    public void updateDir()
    {
            setProgressBarVisible(true);
            String filePath = settings.getCurrentPathView();

            final StorageUtils.CloudFile cloudFile
                    =StorageUtils.parseFileName(filePath, diskList);

            cloudFile.diskRepresenter.getDiskIo()
                  .authorizeIfNecessary()
                  .andThen(cloudFile.diskRepresenter.getDiskIo().getResourceInfo(cloudFile.getPath()))
                    .observeOn(Schedulers.io())
                    .map(new Function<IDiskIO.ResourceInfo, List<RecordListContract.RecordFileInfo>>(){

                        @Override
                        public List<RecordListContract.RecordFileInfo> apply(IDiskIO.ResourceInfo resourceInfo) throws Exception {
                            List<IDiskIO.ResourceInfo> fileList=resourceInfo.content();
                            List<RecordListContract.RecordFileInfo> res = new ArrayList<>(fileList.size());
                            Pattern p = Pattern.compile("^[0-9]{8}_[0-9]{6}_"); // this pattern filters the other app's files
                            for(IDiskIO.ResourceInfo file:fileList)
                            {
                                Matcher m = p.matcher(file.name());
                                if(!m.find()) continue;
                                res.add( getRecordInfo(file.name()) );
                            };
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
                                setDirContent( recordList);
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
        item.fileNameData=FileNameData.decipherFileName(fileName);
        item. callerName = "";
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
            view.setProgressBarVisible(progressBarVisible>0);
    }

    protected void setFilteredDirContent(List<RecordListContract.RecordFileInfo> aFilteredDirContent)
    {
        filteredDirContent = aFilteredDirContent;
        if(view!=null)
        {
            view.setRecordList(filteredDirContent);
        };
    }

    protected void setDirContent(List<RecordListContract.RecordFileInfo> recordList) {
       this.dirContent=recordList;
       filterContent();
    }

    private String filter;
    @Override
    public void setFilter(final String aFilter)
    {
        this.filter = aFilter;
        filterContent();
    }

    @Override
    public void setUndelitable(int pos, boolean selected) {

    }

    @Override
    public void playItem(int pos) {

    }

    @Override
    public void selectPlayerItem(int pos) {

    }

    @Override
    public void unselectAll() {

    }

    @Override
    public void selectAll() {

    }

    @Override
    public void selectItem(int pos, boolean selected) {

    }

    @Override
    public void deleteSelectedItems() {

    }

    ;

    protected void filterContent()
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
                setFilteredDirContent(recordList);
                setProgressBarVisible(false);
            }
       });
    }

    protected List<RecordListContract.RecordFileInfo> doFilterContent(List<RecordListContract.RecordFileInfo> dirContent, String filter)
    {

        if(filter==null || filter.length()==0)
            return dirContent;

        ArrayList<RecordListContract.RecordFileInfo> resList=new ArrayList(dirContent.size());
        for (RecordListContract.RecordFileInfo item : dirContent) {
            if ( checkFilter(item, filter) )
                resList.add(item);
        };
        return resList;
    }
    protected boolean checkFilter(RecordListContract.RecordFileInfo fileData, String filter)
    {
        if (fileData.fileNameData.phoneNumber.indexOf(filter) >= 0) return true;
        if (fileData.callerName.indexOf(filter) >= 0) return true;

        return false;

    }



}
