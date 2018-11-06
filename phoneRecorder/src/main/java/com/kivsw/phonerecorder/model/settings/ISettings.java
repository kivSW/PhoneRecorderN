package com.kivsw.phonerecorder.model.settings;

import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.model.settings.types.DataSize;
import com.kivsw.phonerecorder.model.settings.types.SoundSource;

import java.util.List;

import io.reactivex.Observable;

/**
 * Interface for settings
 */

public interface ISettings {

    Observable<String> getObservable();


    boolean getEnableCallRecording();
    void setEnableCallRecording(boolean value);

    boolean getEnableSmsRecording();
    void setEnableSmsRecording(boolean value);

    boolean getHiddenMode();
    void setHiddenMode(boolean value)  ;

    boolean getUseFileExtension();
    void setUseFileExtension(boolean value);

    boolean getUsingMobileInternet();
    void setUsingMobileInternet(boolean value);

    boolean getAllowSendingInRoaming();
    void setSendInRoaming(boolean value);

    boolean getAllowExportingJournal();
    void setAllowExportingJournal(boolean value);

    boolean getUseInternalPlayer();
    void setUseInternalPlayer(boolean value);

    boolean getAbonentToFileName();
    void setAbonentToFileName(boolean value);

    String getSavingUrlPath();
    void setSavingUrlPath(String value);
    String getInternalTempPath();

    SoundSource getSoundSource();
    void setSoundSource(SoundSource value);


    boolean getFileAmountLimitation();
    void setFileAmountLimitation(boolean value);

    int maxKeptFileAmount();
    int getKeptFileAmount();
    void setKeptFileAmount(int value);

    boolean getDataSizeLimitation();
    void setDataSizeLimitation(boolean value);

    long maxFileDataSize();
    DataSize getFileDataSize();
    void setFileDataSize(DataSize dataSize);

    String getSecretNumber();
    void setSecretNumber(String value);

    List<String> getViewUrlPathHistory();
    //void setPathViewHistory(List<String> history);
    void addToViewUrlPathHistory(String newUrlPath);
    String getCurrentViewUrlPath();

    long getCacheSize();
    int getCacheFilesNumber();

    AntiTaskKillerNotificationParam getAntiTaskKillerNotification();
    void setAntTaskKillerNotification(AntiTaskKillerNotificationParam param);
}
