package com.kivsw.phonerecorder.model.metrica;

import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 6/20/18.
 */

public interface IMetrica {
    void onSettingsCreate(ISettings settings);
    void notifyError(Throwable throwable);
}
