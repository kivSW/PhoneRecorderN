package com.kivsw.phonerecorder.ui.notification;

import android.content.Context;

import com.kivsw.phonerecorder.model.settings.ISettings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 5/4/18.
 */
@Module
public class NotificationShowerModule {
    private static int dynamic_id =1000;
    private static final int ANTI_TASK_KILLER_NOTIFICATION_ID =1;
    public static final int AUXIALIARY_NOTIFICATION_ID =2;

    @Provides
    static public NotificationShower provideNotification(Context context, ISettings settings)
    {
        return new NotificationShower(context, settings, dynamic_id++);
    };

    @Provides
    @Singleton
    static public AntiTaskKillerNotification provideAntiTaskKillerNotification(Context context, ISettings settings)
    {
        return new AntiTaskKillerNotification(context, settings, ANTI_TASK_KILLER_NOTIFICATION_ID);
    };
}
