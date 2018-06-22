package com.kivsw.phonerecorder.model.metrica;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

/**
 *
 * https://tech.yandex.ru/appmetrica/doc/mobile-sdk-dg/concepts/android-initialize-docpage/
 */

public class YandexMetricaProxy implements IMetrica {

    SharedPreferences sharedPreferences;

    YandexMetricaProxy(Application app, String API_key)
    {
        sharedPreferences=app.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE);

        // Инициализация AppMetrica SDK
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(API_key);

        if (!isFirstApplicationLaunch()) {
            // Передайте значение true, если не хотите, чтобы данный пользователь засчитывался как новый
            configBuilder.handleFirstActivationAsUpdate(true);
        }
        YandexMetricaConfig extendedConfig = configBuilder.build();
        YandexMetrica.activate(app.getApplicationContext(), extendedConfig);
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(app);
    }

    final private static String IS_FIRST="IS_FIRST_RUN";
    private boolean isFirstApplicationLaunch()
    {
        boolean res=sharedPreferences.getBoolean(IS_FIRST, true);
        sharedPreferences.edit()
        .putBoolean(IS_FIRST, false)
        .apply();

        return  res;
    }
}
