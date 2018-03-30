package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListContract {
    interface IRecordListPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
        void chooseCurrentDir();
        void setCurrentDir();
        void setUndelitable(String file);
        void play(String file);
    };

    interface IRecordListView extends com.kivsw.mvprxdialog.Contract.IView
    {
        void setSettings(ISettings settings);
        void setRecordList();
        void setProgressBarVisible(boolean show);
        Context getContext();
        FragmentManager getFragmentManager();
    }
}
