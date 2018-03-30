package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import com.kivsw.cloud.disk.IDiskIO;
import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloud.disk.StorageUtils;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

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

    public RecordListPresenter(ISettings settings, List<IDiskRepresenter> diskList)
    {
        this.settings = settings;
        this.diskList = diskList;
    }

    @Override
    public Contract.IView getUI() {
        return null;
    }

    @Override
    public void setUI(Contract.IView view) {
        this.view = (RecordListContract.IRecordListView)view;
    }

    @Override
    public void removeUI() {
        this.view = null;
    }

    @Override
    public void chooseCurrentDir() {
        String path=settings.getCurrentPathView();
        MvpRxSelectDirDialogPresenter selDirPresenter = MvpRxSelectDirDialogPresenter
                .createDialog(view.getContext(), view.getFragmentManager(), diskList, settings.getSavingPath(), null);

        selDirPresenter.getMaybe()
                .flatMapSingle(new Function<String, Single<IDiskIO.ResourceInfo>>(){

                    @Override
                    public Single apply(String filePath) throws Exception {
                        settings.addToPathViewHistory(filePath);

                        final StorageUtils.CloudFile cloudFile
                                =StorageUtils.parseFileName(filePath, diskList);

                        return cloudFile.diskRepresenter.getDiskIo()
                                .authorizeIfNecessary()
                                .andThen(cloudFile.diskRepresenter.getDiskIo().getResourceInfo(cloudFile.getPath()));
                    }
                })
                .subscribe(new SingleObserver<IDiskIO.ResourceInfo>(){


                    @Override
                    public void onSubscribe(Disposable d) {
                        !!
                    }

                    @Override
                    public void onSuccess(IDiskIO.ResourceInfo resourceInfo) {
!!!
                    }

                    @Override
                    public void onError(Throwable e) {
!!
                    }
                });
    }

    @Override
    public void setCurrentDir() {

    }

    @Override
    public void setUndelitable(String file) {

    }

    @Override
    public void play(String file) {

    }
}
