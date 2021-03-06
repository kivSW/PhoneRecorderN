package com.kivsw.phonerecorder.ui.record_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;

import java.util.List;

/**
 * Created by ivan on 3/27/18.
 */

public class RecordListContract {
    static public class RecordFileInfo implements Comparable// class holds data of a record to be visualized
    {
        public RecordFileNameData recordFileNameData;
        public boolean fromInternalDir;
        public RecordFileInfo cachedRecordFileInfo;

        public String parentDir;
        public boolean selected;
        public boolean isCallerNameFromLocalAddrBook=false;
        public String callerName;
        //int duration;
        public boolean isDownloading;
        public int percentage;
        public int visiblePosition;

        public String getFileFullPath()
        {
            return parentDir+recordFileNameData.origFileName;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            RecordFileInfo other=(RecordFileInfo)o;
            return recordFileNameData.origFileName.compareTo(other.recordFileNameData.origFileName);
        }
        @Override
        public boolean equals(Object o)
        {
            return 0==compareTo(o);
        }
    }
    interface IRecordListPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
        void chooseCurrentDir();
        void setCurrentDir(String dir);

        void updateDir(boolean scrollToBegin);
        void setFilter(String filter);
        void setUndeletable(int pos, boolean isProtected);
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
