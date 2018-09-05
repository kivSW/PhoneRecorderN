package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;
import android.support.v4.app.FragmentManager;

/**
 * Created by ivan on 3/7/18.
 */

public class SettingsContract {

    interface ISettingsPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
       void chooseDataDir();
       void sendJournal();
    };

    interface ISettingsView extends com.kivsw.mvprxdialog.Contract.IView
    {
       void updateSavePath();
       Context getContext();
       FragmentManager getFragmentManager();
    }
}
