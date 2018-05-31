package phonerecorder.kivsw.com.faithphonerecorder.model.player;

import android.content.Context;

import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;

/**
 * Created by ivan on 4/19/18.
 */

public interface IPlayer {
    void setUiParam(String callerName, RecordFileNameData recordFileNameData);
    void play(Context activity, String filePath);
    void playItemWithChooser(Context activity,String filePath);
}
