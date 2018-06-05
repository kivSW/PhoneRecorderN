package phonerecorder.kivsw.com.faithphonerecorder.model.utils;

/**
 * Created by ivan on 5/31/18.
 */

public class Utils {
    static public String durationToStr(int d)
    {
            if(d<=0)
                return "--:--";

            return timeToStr( d);
    }
    static public String timeToStr(int d)
    {
            int m=0,s=0;
            s=d%60;
            m=d/60;

            return String.format("%02d:%02d", m,s);
    }
}
