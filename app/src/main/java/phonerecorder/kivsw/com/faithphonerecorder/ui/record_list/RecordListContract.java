package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import java.util.List;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListContract {
    static class RecordFileInfo // class holds data of a record to be visualized
    {
        RecordFileNameData recordFileNameData;
        boolean selected;
        String callerName;
        //int duration;
        boolean isDownloading;
        int percentage;
        int visiblePosition;
    }
    interface IRecordListPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
        void chooseCurrentDir();
        void setCurrentDir(String dir);

        void updateDir(boolean scrollToBegin);
        void setFilter(String filter);
        void setUndelitable(int pos, boolean isProtected);
        void playItem(int pos);
        void playItemWithPlayerChoosing(int pos);
        boolean hasSelectedItem(boolean excludeProtected);
        void unselectAll();
        void selectAll();
        void selectItem(int pos, boolean selected);
        void deleteSelectedItems();
    };

    interface IRecordListView extends com.kivsw.mvprxdialog.Contract.IView
    {
        void setSettings(ISettings settings);
        void setRecordList(List<RecordFileInfo> recordListList, boolean scrollToBegin);
        void setRecListProgressBarVisible(boolean show);
        void onRecordListChanged();
        void onRecordChanged(int index);
        Context getContext();
        FragmentManager getFragmentManager();

    }
}
