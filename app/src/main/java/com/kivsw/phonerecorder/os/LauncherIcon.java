package com.kivsw.phonerecorder.os;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * This class shows or hide launcher icon
 */

public class LauncherIcon {
    private final static String ALIASE_NAME="com.kivSW.phonerecorder.MainActivity";//"phonerecorder.kivsw.com.faithphonerecorder.MainActivityAlias";
    static public void setVisibility(Context context, boolean isVisible)
    {
        PackageManager p = context.getPackageManager();
        int st;
        if(isVisible) st=PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        else  st=PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        p.setComponentEnabledSetting(new ComponentName(context,ALIASE_NAME), st	, PackageManager.DONT_KILL_APP);

    };
    static public boolean getVisibility(Context context)
    {
        PackageManager p = context.getPackageManager();
        int r=p.getComponentEnabledSetting(new ComponentName(context,ALIASE_NAME));

        return r!=PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    };
}
