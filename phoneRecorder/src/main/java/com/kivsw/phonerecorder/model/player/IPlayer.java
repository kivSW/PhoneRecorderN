package com.kivsw.phonerecorder.model.player;

import android.content.Context;

import com.kivsw.phonerecorder.model.utils.RecordFileNameData;

/**
 * Created by ivan on 4/19/18.
 */

public interface IPlayer {
    void setUiParam(String callerName, RecordFileNameData recordFileNameData);
    void play(Context activity, String filePath);
    void playItemWithChooser(Context activity,String filePath);
}
