package com.kivsw.phonerecorder.os;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

/**
 * Created by ivan on 3/21/18.
 */
@Module
public class ApplicationModule {
    private Context context;
    private Application application;
    public ApplicationModule(@NonNull Application application)
    {
        this.context = context;
        this.application = application;
    };

    @Provides
    @Singleton
    Context provideContext()
    {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    Application provideApplication()
    {
        return application;
    }
}
