package com.kivsw.phonerecorder.model.metrica;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.Settings;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 *
 * https://tech.yandex.ru/appmetrica/doc/mobile-sdk-dg/concepts/android-initialize-docpage/
 * https://appmetrica.yandex.com
 */

public class YandexMetricaProxy implements IMetrica {

    SharedPreferences sharedPreferences;
    ISettings settings;

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

    @Override
    public void onSettingsCreate(ISettings settings)
    {
        this.settings = settings;
        settings.getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        switch(s) {
                            case Settings.HIDDEN_MODE:
                                        sendSettings();
                                        break;
                        }
                    };

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    };

    @Override
    public void notifyError(Throwable throwable)
    {
       /* if(throwable instanceof InsignificantException)
            return;*/

        String message = "SDK_INT="+Build.VERSION.SDK_INT+"  RELEASE="+Build.VERSION.RELEASE;
        YandexMetrica.reportError(message,throwable);
    };

    private void sendSettings()
    {
        Map<String, Object> map=new HashMap<>();
        map.put("HiddenMode", String.valueOf(settings.getHiddenMode()));
        Uri uri= Uri.parse(settings.getSavingUrlPath());
        map.put("SavingUrlPath", uri.getScheme());
        YandexMetrica.reportEvent("settings",map);
    }

}
