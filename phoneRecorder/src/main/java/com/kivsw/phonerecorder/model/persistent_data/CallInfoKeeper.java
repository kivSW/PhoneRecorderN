package com.kivsw.phonerecorder.model.persistent_data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

/**
 * Class holds information about the current call
 */

public class CallInfoKeeper implements IPersistentDataKeeper {
    private final static String PHONE_NUMBER="PHONE_NUMBER",
            IS_INCOME="IS_INCOME",
            CALL_TIME="CALL_TIME";
    private final static String NAME="call_info";

    private SharedPreferences preferences;
    private Context cnt;

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
                .apply();
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

    private long getDefaultTime()
    {
        /*if(BuildConfig.DEBUG)
            return 0;;*/
        final long _24HOURS=24*60*60000;
        return System.currentTimeMillis() - _24HOURS*1;
    };

    private final static String LAST_INCOME_SMS="LAST_INCOME_SMS";
    @Override
    public long getLastIncomeSms()
    {
        return preferences.getLong(LAST_INCOME_SMS, getDefaultTime());
    };
    @Override
    public void setLastIncomeSms(long v)
    {
        preferences.edit()
                .putLong(LAST_INCOME_SMS, v)
                .apply();
    }

    private final static String LAST_OUTGOING_SMS="LAST_OUTGOING_SMS";
    @Override
    public long getLastOutgoingSms()
    {
        return preferences.getLong(LAST_OUTGOING_SMS, getDefaultTime());
    }
    @Override
    public void setLastOutgoingSms(long v)
    {
        preferences.edit()
                .putLong(LAST_OUTGOING_SMS, v)
                .apply();
    }
}
