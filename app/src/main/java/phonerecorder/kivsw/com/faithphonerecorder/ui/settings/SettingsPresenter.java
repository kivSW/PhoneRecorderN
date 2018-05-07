package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import javax.inject.Inject;

import io.reactivex.MaybeObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.os.LauncherIcon;

/**
 * Created by ivan on 3/1/18.
 */

public class SettingsPresenter implements SettingsContract.ISettingsPresenter {

    protected SettingsContract.ISettingsView view;
    protected ISettings settings;
    private Disposable settingsDisposable;
    protected DiskContainer disks;
    protected TaskExecutor taskExecutor;
    protected IErrorProcessor errorProcessor;

   /* public static SettingsPresenter createDialog(Context context, FragmentManager fragmentManager)
    {

        SettingsPresenter presenter = new SettingsPresenter(context);
        long id= presenter.getDialogPresenterId();

        Bundle arg=new Bundle();

        SettingsFragment settingsFragment = SettingsFragment.newInstance(id, context.getText(R.string.settings).toString());
        settingsFragment.show(fragmentManager, String.valueOf(id));

        return presenter;
    }*/

    @Inject
    public SettingsPresenter(ISettings settings, DiskContainer disks, TaskExecutor taskExecutor, IErrorProcessor errorProcessor)
    {
        super();
        //this.context = context;
        settingsDisposable=null;
        this.settings = settings;//Settings.getInstance(context);
        this.disks = disks;
        this.taskExecutor = taskExecutor;
        this.errorProcessor = errorProcessor;

    }
    @Override
    public Contract.IView getUI()
    {
        return view;
    };

    @Override
    public void setUI(@NonNull Contract.IView view)
    {
        if(view instanceof SettingsFragment) {
            this.view = (SettingsContract.ISettingsView) view;
            this.view.setSettings(settings);
            subscribeSettings();
        }
    }

    @Override
    public void removeUI() {
        view =null;
        unsubscribeSettings();
    }

    protected void subscribeSettings()
    {
        unsubscribeSettings();
        settingsDisposable =
                settings.getObservable()
                        .subscribe(new Consumer<ISettings>() {
                            @Override
                            public void accept(ISettings iSettings) throws Exception {
                                Context cnt = view.getContext();
                                boolean visible = ! iSettings.getHiddenMode();
                                if(visible != LauncherIcon.getVisibility(cnt)) {
                                    LauncherIcon.setVisibility(cnt, visible);
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


    private MvpRxSelectDirDialogPresenter selDirPresenter;
    @Override
    public void chooseDataDir()
    {
        //List<IDiskRepresenter> diskList = DiskRepresentativeModule.getDisks(view.getContext());
        selDirPresenter=MvpRxSelectDirDialogPresenter.createDialog(view.getContext(), view.getFragmentManager(), disks.getDiskList(), settings.getSavingUrlPath(), null);
        selDirPresenter.getMaybe()
                .subscribe(new MaybeObserver<String>() {
                    @Override  public void onSubscribe(Disposable d) {}
                    @Override public void onError(Throwable e) {
                        errorProcessor.onError(e);
                    }
                    @Override public void onComplete() {}

                    @Override
                    public void onSuccess(String s) {
                        settings.setSavingUrlPath(s);
                        view.updateSavePath();
                        taskExecutor.startFileSending();
                    }

                });
    };


}
