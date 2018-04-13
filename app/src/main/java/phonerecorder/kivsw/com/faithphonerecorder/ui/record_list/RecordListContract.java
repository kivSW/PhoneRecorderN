package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import java.util.List;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.FileNameData;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListContract {
    static class RecordFileInfo // class holds data of a record
    {
        FileNameData fileNameData;
        boolean selected;
        String callerName;
        int duration;
    }
    interface IRecordListPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
        void onError(String message);
        void chooseCurrentDir();
        void updateDir();
        void setFilter(String filter);
        void setUndelitable(int pos, boolean selected);
        void playItem(int pos);
        void selectPlayerItem(int pos);
        void unselectAll();
        void selectAll();
        void selectItem(int pos, boolean selected);
        void deleteSelectedItems();
    };

    interface IRecordListView extends com.kivsw.mvprxdialog.Contract.IView
    {
        void setSettings(ISettings settings);
        void setRecordList(List<RecordListContract.RecordFileInfo> recordListList);
        void setProgressBarVisible(boolean show);
        Context getContext();
        FragmentManager getFragmentManager();
    }
}
