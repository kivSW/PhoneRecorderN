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

import io.reactivex.MaybeObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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
    private List<IDiskIO.ResourceInfo> dirContent;
    private List<FileNameData> filteredDirContent;

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
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new SingleObserver<IDiskIO.ResourceInfo>(){
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(IDiskIO.ResourceInfo resourceInfo) {
                                setDirContent(resourceInfo.content());
                                setProgressBarVisible(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                RecordListPresenter.this.onError(e.toString());
                                setProgressBarVisible(false);
                            }
                        });
    };

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

    protected void setFilteredDirContent(List<FileNameData> aFilteredDirContent)
    {
        filteredDirContent = aFilteredDirContent;
        if(view!=null)
        {
            view.setRecordList(filteredDirContent);
        };
    }

    protected void setDirContent(List<IDiskIO.ResourceInfo> dirContent) {
       this.dirContent=dirContent;
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
        final List<IDiskIO.ResourceInfo> tmpDirContent = this.dirContent;
        final String tmpFilter = filter;

        setProgressBarVisible(true);

        Single.fromCallable(new Callable<List<FileNameData>>() {
            @Override
            public List<FileNameData> call() throws Exception {
                return doFilterContent(tmpDirContent, tmpFilter);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<FileNameData>>() {
            @Override
            public void accept(List<FileNameData> resourceInfos) throws Exception {
                setFilteredDirContent(resourceInfos);
                setProgressBarVisible(false);
            }
       });
    }

    protected List<FileNameData> doFilterContent(List<IDiskIO.ResourceInfo> dirContent, String filter)
    {
        ArrayList<FileNameData> resList=new ArrayList(dirContent.size());
        if(filter==null || filter.length()==0)
        {
            for(IDiskIO.ResourceInfo item:dirContent)
                    resList.add( FileNameData.decipherFileName(item.name()) );
        }
        else {
            for (IDiskIO.ResourceInfo item : dirContent) {
                FileNameData fileData = FileNameData.decipherFileName(item.name());
                if ( (fileData.phoneNumber.indexOf(filter) >= 0) )
                    resList.add(fileData);
            }
        }
        return resList;
    }



}
