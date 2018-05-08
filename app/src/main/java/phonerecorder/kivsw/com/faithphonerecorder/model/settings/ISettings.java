package phonerecorder.kivsw.com.faithphonerecorder.model.settings;

import java.util.List;

import io.reactivex.Observable;

/**
 * Interface for settings
 */

public interface ISettings {

    Observable<ISettings> getObservable();


    boolean getEnableCallRecording();
    void setEnableCallRecording(boolean value);

    boolean getEnableSmsRecording();
    void setEnableSmsRecording(boolean value);

    boolean getHiddenMode();
    void setHiddenMode(boolean value)  ;

    boolean getUseFileExtension();
    void setUseFileExtension(boolean value);

    boolean getJournalExporting();
    void setJournalExporting(boolean value);

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

}
