package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxPresenter;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.Settings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.os.LauncherIcon;
import com.kivsw.phonerecorder.os.jobs.AppService;
import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

import javax.inject.Inject;

import io.reactivex.MaybeObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import phonerecorder.kivsw.com.phonerecorder.R;


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
        }
        subscribeSettings();
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
                if(!visible)  hideServiceNotifications();
                break;

            /*case Settings.ANTI_TASKKILLER_NOTOFICATION:
                    if (settings.getAntiTaskKillerNotification().visible)
                        antiTaskKillerNotification.show();
                    else
                        antiTaskKillerNotification.hide();
                    break;*/

            case Settings.ENABLE_SMS_RECORDING:
                    if(settings.getEnableSmsRecording())
                        taskExecutor.startSMSreading();
                    break;
        }
    }
    private void hideServiceNotifications() {
        if(AppService.Companion.mayApplicationHideNotification()) // application can hide notifications without its user
            return;
        MvpMessageBoxBuilder.newInstance()
                .setText(context.getText(R.string.confirmation), context.getText(R.string.ask_for_hiding_notification))
                .setOkButton(context.getText(R.string.yes))
                .setCancelButton(context.getText(R.string.no))
                .build(view.getFragmentManager())
                .getSingle()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer btnNum) throws Exception {
                        if (btnNum.intValue() == MvpMessageBoxPresenter.OK_BUTTON)
                        {
                            Intent i= new Intent();
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //9.0
                            {
                                i.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                i.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                            }
                            /*else
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // 8.0
                            {
                                i.setAction(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                                i.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                                //i.putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, ServiceNotification.CHANNEL_ID);
                            }*/
                            else
                            { // 7.0, 7.1
                                i.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                i.putExtra("app_package", context.getPackageName());
                                i.putExtra("app_uid", context.getApplicationInfo().uid);
                            }
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                        }

                    }
                });
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
                       /* taskExecutor.startFileSending();
                        taskExecutor.startSMSreading(); // FIXME*/
                    }

                });
    }

    @Override
    public void sendJournal() {
        settings.setAllowExportingJournal(true);
        taskExecutor.startFileSending();
    }

    ;

}
