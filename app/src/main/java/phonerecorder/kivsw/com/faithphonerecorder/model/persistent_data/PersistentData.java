package phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import javax.inject.Inject;

/**
 * Created by ivan on 4/26/18.
 */

public class PersistentData implements IPersistentData{

    SharedPreferences preferences;
    Context cnt;

    private final static String NAME="call_info";

    @Inject
    public PersistentData(Context context) {
        cnt = context;
        preferences = cnt.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    final String PHONE_NUMBER="PHONE_NUMBER",
                 IS_INCOME="IS_INCOME",
                 CALL_TIME="CALL_TIME";
    @Override
    public void setCallInfo(String number, boolean isIncome) {
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

    @Override
    public void journalAdd(String data)
    {

    };

    @Override
    public void journalAdd(Throwable throwable)
    {

    };

    @Override
    public void journalAdd(Intent intent)
    {

    };
}
