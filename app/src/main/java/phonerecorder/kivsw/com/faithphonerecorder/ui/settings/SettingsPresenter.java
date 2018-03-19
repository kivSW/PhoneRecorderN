package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import android.content.Context;

import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxfiledialog.MvpRxSelectDirDialogPresenter;

import java.util.List;

import io.reactivex.MaybeObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import phonerecorder.kivsw.com.faithphonerecorder.model.DiskRepresentativeFactory;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.Settings;

/**
 * Created by ivan on 3/1/18.
 */

public class SettingsPresenter implements SettingsContract.ISettingsPresenter {
    private static SettingsPresenter singleton=null;
    public static synchronized SettingsPresenter getInstance(Context context)
    {
        if(singleton==null)
        {
            singleton = new SettingsPresenter(Settings.getInstance(context));
        };
        return singleton;
    }

    SettingsFragment view;
    ISettings settings;

   /* public static SettingsPresenter createDialog(Context context, FragmentManager fragmentManager)
    {

        SettingsPresenter presenter = new SettingsPresenter(context);
        long id= presenter.getDialogPresenterId();

        Bundle arg=new Bundle();

        SettingsFragment settingsFragment = SettingsFragment.newInstance(id, context.getText(R.string.settings).toString());
        settingsFragment.show(fragmentManager, String.valueOf(id));

        return presenter;
    }*/

    protected SettingsPresenter(ISettings settings)
    {
        super();
        //this.context = context;
        this.settings = settings;//Settings.getInstance(context);

        //registerDialogPresenter();
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
            this.view = (SettingsFragment) view;
            this.view.setSettings(settings);
        }
    }

    @Override
    public void removeUI() {
        view =null;
    }

    private MvpRxSelectDirDialogPresenter selDirPresenter;
    @Override
    public void selectDataDir()
    {
        List<IDiskRepresenter> diskList = DiskRepresentativeFactory.getDisks(view.getContext());
        selDirPresenter=MvpRxSelectDirDialogPresenter.createDialog(view.getContext(), view.getFragmentManager(), diskList, settings.getSavingPath(), null);
        selDirPresenter.getMaybe()
                .subscribe(new MaybeObserver<String>() {
                    @Override  public void onSubscribe(Disposable d) {}
                    @Override public void onError(Throwable e) {}
                    @Override public void onComplete() {}

                    @Override
                    public void onSuccess(String s) {
                        settings.setSavingPath(s);
                        view.updateSavePath();
                    }

                });
    };


}
