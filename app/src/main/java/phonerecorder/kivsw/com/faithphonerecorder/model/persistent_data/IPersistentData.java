package phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data;

import android.content.Intent;

/**
 * Created by ivan on 4/26/18.
 */

public interface IPersistentData {
    class CallInfo
    {
        public String number;
        public boolean isIncome;
        public long elapsed_ms;
        public String toString(){return String.format("%s %s time:%d", number, isIncome?"in":"out", elapsed_ms/1000);};
    };

    void setCallInfo(String number, boolean isIncome);
    CallInfo getCallInfo();

    void journalAdd(String data);
    void journalAdd(Throwable throwable);
    void journalAdd(Intent intent);

}
