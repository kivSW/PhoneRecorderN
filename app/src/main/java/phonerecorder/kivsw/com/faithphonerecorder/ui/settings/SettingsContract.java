package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 3/7/18.
 */

public class SettingsContract {
    //static abstract class ISettingsPresenter extends BaseMvpPresenter
    interface ISettingsPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
       void selectDataDir();
    };

    interface ISettingsView extends com.kivsw.mvprxdialog.Contract.IView
    {
       void setSettings(ISettings settings);
       void updateSavePath();
    }
}
