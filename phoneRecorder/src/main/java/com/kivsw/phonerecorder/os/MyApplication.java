package com.kivsw.phonerecorder.os;


import com.kivsw.phonerecorder.model.metrica.IMetrica;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.Settings;
import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.os.jobs.AntiTaskkillerService;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ivan on 3/20/18.
 */

public class MyApplication extends android.app.Application {
    protected static ApplicationComponent applicationComponent;
    public static ApplicationComponent getComponent()
    {
        return applicationComponent;
    };

    @Inject
    ISettings settings;

    @Inject
    IMetrica metrica;

    public MyApplication()
    {
        super();
        applicationComponent = DaggerApplicationComponent.builder()
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
        settings.getObservable().subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(String s) {
                if(Settings.ANTI_TASKKILLER_NOTOFICATION.equals(s))
                    onAntitaskkillerChanged();
            }

            @Override
            public void onError(Throwable e) {  }

            @Override
            public void onComplete() {  }
        });

        try {
            onAntitaskkillerChanged();
        }catch(Throwable t)
        {}

    }

    protected void onAntitaskkillerChanged()
    {
        AntiTaskKillerNotificationParam param = settings.getAntiTaskKillerNotification();
        if(param.visible)
             AntiTaskkillerService.start(this, param.iconNum);
        else AntiTaskkillerService.stop(this);
    }


}
