package com.kivsw.phonerecorder.model.metrica;

import android.app.Application;

import com.kivsw.phonerecorder.model.keys.keys;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 6/20/18.
 */
@Module
public class MetricaModule {

    @Provides
    @Singleton
    IMetrica provideMetrica(Application app)
    {
        return new YandexMetricaProxy(app, keys.getYandexMetricKey());
    }
}
