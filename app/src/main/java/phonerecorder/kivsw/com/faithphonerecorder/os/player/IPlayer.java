package phonerecorder.kivsw.com.faithphonerecorder.os.player;

import android.content.Context;

/**
 * Created by ivan on 4/19/18.
 */

public interface IPlayer {
    void play(Context activity, String filePath);
    void playItemWithChooser(Context activity,String filePath);
}
