package phonerecorder.kivsw.com.faithphonerecorder.model.settings;

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

    String getSavingPath();
    void setSavingPath(String value);

    SoundSource getSoundSource();
    void setSoundSource(SoundSource value);


    boolean getFileAmountLimitation();
    void setFileAmountLimitation(boolean value);

    int maxKeptFile();
    int getKeptFileAmount();
    void setKeptFileAmount(int value);

    boolean getDataSizeLimitation();
    void setDataSizeLimitation(boolean value);

    long maxFileDataSize();
    DataSize getFileDataSize();
    void setFileDataSize(DataSize dataSize);

    String getSecretNumber();
    void setSecretNumber(String value);


}
