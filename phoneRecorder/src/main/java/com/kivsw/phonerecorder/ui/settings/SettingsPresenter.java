package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.Settings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.os.LauncherIcon;
import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

import javax.inject.Inject;

import io.reactivex.MaybeObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by ivan on 3/1/18.
 */

public class SettingsPresenter implements SettingsContract.ISettingsPresenter {

    protected Context  context;
    protected SettingsContract.ISettingsView view;
    protected ISettings settings;
    private Disposable settingsDisposable;
    protected DiskContainer disks;
    protected ITaskExecutor taskExecutor;
    protected IErrorProcessor errorProcessor;
    protected AntiTaskKillerNotification antiTaskKillerNotification;

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
    public SettingsPresenter(Context  context, ISettings settings, DiskContainer disks, ITaskExecutor taskExecutor, IErrorProcessor errorProcessor, AntiTaskKillerNotification antiTaskKillerNotification)
    {
        super();
        this.context = context;
        settingsDisposable=null;
        this.settings = settings;//Settings.getInstance(context);
        this.disks = disks;
        this.taskExecutor = taskExecutor;
        this.errorProcessor = errorProcessor;
        this.antiTaskKillerNotification=antiTaskKillerNotification;

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
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String id) throws Exception {
                                onChangeSettings(id);
                            }
                        });
    }
    protected void unsubscribeSettings()
    {
        if(settingsDisposable!=null)
            settingsDisposable.dispose();
        settingsDisposable=null;
    }

    protected void onChangeSettings(String id)
    {
        switch(id) {
            case Settings.HIDDEN_MODE:
                boolean visible = !settings.getHiddenMode();
                LauncherIcon.setVisibility(context, visible);
                break;

            case Settings.ANTI_TASKKILLER_NOTOFICATION:
                    if (settings.getAntiTaskKillerNotification().visible)
                        antiTaskKillerNotification.show();
                    else
                        antiTaskKillerNotification.hide();
                    break;

            case Settings.ENABLE_SMS_RECORDING:
                    if(settings.getEnableSmsRecording())
                        taskExecutor.startSMSreading();
                    break;
        }
    }

    private MvpRxSelectDirDialogPresenter selDirPresenter;
    @Override
    public void chooseDataDir()
    {
        //List<IDiskRepresenter> diskList = DiskRepresentativeModule.getDisks(view.getContext());
        selDirPresenter=MvpRxSelectDirDialogPresenter.createDialog(view.getContext(), view.getFragmentManager(), disks, settings.getSavingUrlPath(), null);
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
                        taskExecutor.startSMSreading(); // FIXME
                    }

                });
    };

}
