package phonerecorder.kivsw.com.faithphonerecorder.os;


import javax.inject.Inject;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.ui.notification.AntiTaskKillerNotification;

/**
 * Created by ivan on 3/20/18.
 */

public class MyApplication extends android.app.Application {
    protected static ApplicationComponent applicationComponent;
    public static ApplicationComponent getComponent()
    {
        return applicationComponent;
    };

    @Inject ISettings settings;
    @Inject AntiTaskKillerNotification  antiTaskKillerNotification;

    public MyApplication()
    {
        super();
        applicationComponent =DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        applicationComponent.inject(this);
        init();
    }

    protected void init()
    {
        if(settings.getAntiTaskKillerNotification().visible)
            antiTaskKillerNotification.show();
        else antiTaskKillerNotification.hide();
    }


}
