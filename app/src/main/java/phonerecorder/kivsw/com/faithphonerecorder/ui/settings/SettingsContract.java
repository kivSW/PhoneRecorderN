package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import com.kivsw.mvprxdialog.BaseMvpPresenter;

import phonerecorder.kivsw.com.faithphonerecorder.ui.model.Settings;

/**
 * Created by ivan on 3/7/18.
 */

public class SettingsContract {
    abstract class ISettingsPresenter extends BaseMvpPresenter
    {

    }
    interface ISettingsView extends com.kivsw.mvprxdialog.Contract.IView
    {
       void setSettings(Settings settings);
    }
}
