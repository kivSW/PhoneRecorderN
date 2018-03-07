package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import android.support.v4.app.FragmentManager;

import com.kivsw.mvprxdialog.BaseMvpPresenter;
import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.PresenterManager;

import io.reactivex.annotations.NonNull;

/**
 * Created by ivan on 3/1/18.
 */

public class SettingsPresenter extends BaseMvpPresenter {
    SettingsFragment ui;

    SettingsPresenter createPresenter(FragmentManager fragmentManager)
    {

        SettingsPresenter presenter = new SettingsPresenter();
        long id= PresenterManager.getInstance().addNewPresenter(presenter);

        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.show(fragmentManager, String.valueOf(id));

        return presenter;
    }

    @Override
    public Contract.IView getUI()
    {
        return ui;
    };

    @Override
    public void setUI(@NonNull Contract.IView view)
    {
        if(view instanceof SettingsFragment)
            ui= (SettingsFragment)view;
    };


}
