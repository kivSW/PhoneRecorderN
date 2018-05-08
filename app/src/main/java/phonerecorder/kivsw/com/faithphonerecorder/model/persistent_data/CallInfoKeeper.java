package phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

/**
 * Class holds information about the current call
 */

public class CallInfoKeeper implements ICallInfoKeeper {
    private final static String PHONE_NUMBER="PHONE_NUMBER",
            IS_INCOME="IS_INCOME",
            CALL_TIME="CALL_TIME";
    private final static String NAME="call_info";

    SharedPreferences preferences;
    Context cnt;

    CallInfoKeeper(Context context)
    {
        cnt = context;
        preferences = cnt.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void setCallInfo(String number, boolean isIncome) {
        if(number==null)
            number="";
        preferences.edit()
                .putString(PHONE_NUMBER, number)
                .putBoolean(IS_INCOME, isIncome)
                .putLong(CALL_TIME, SystemClock.elapsedRealtime())
                .commit();
    }

    @Override
    public CallInfo getCallInfo() {
        CallInfo callInfo=new CallInfo();
        callInfo.number = preferences.getString(PHONE_NUMBER,"");
        callInfo.isIncome = preferences.getBoolean(IS_INCOME,false);
        long time=preferences.getLong(CALL_TIME,0);
        callInfo.elapsed_ms=SystemClock.elapsedRealtime() - time;

        return callInfo;
    }
}
