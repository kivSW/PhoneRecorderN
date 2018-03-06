package phonerecorder.kivsw.com.faithphonerecorder.ui.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by ivan on 3/1/18.
 */

public class Settings {
    static Settings singletone=null;

    synchronized static public Settings getInstance(Context cnt)
    {
        if(singletone==null)
            singletone = new Settings(cnt);
        return singletone;
    }


    Subject<Settings> onChangeObservable=null;
    SharedPreferences preferences;
    Context cnt;
    private final static String NAME="data";

    protected Settings(Context cnt)
    {
        this.cnt = cnt;
        onChangeObservable = PublishSubject.create();
        preferences = cnt.getSharedPreferences(NAME, Context.MODE_PRIVATE);

    }

    /**
     * Emitts whenever an option was changed
     * @return
     */
    public Observable<Settings> getObservable()
    {
        return onChangeObservable;
    }

    private void emitOnChange()
    {
        onChangeObservable.onNext(this);
    }

    /**
     * parameters
     */

    private final static String ENABLE_CALL_RECORDING = "ENABLE_CALL_RECORDING";
    public boolean getEnableCallRecording()
    {
        return preferences.getBoolean(ENABLE_CALL_RECORDING,false);
    };
    public void setEnableCallRecording(boolean value)
    {
        preferences.edit()
                .putBoolean(ENABLE_CALL_RECORDING, value)
                .commit();
        emitOnChange();
    };

    private final static String ENABLE_SMS_RECORDING = "ENABLE_SMS_RECORDING";
    public boolean getEnableSmsRecording()
    {
        return preferences.getBoolean(ENABLE_SMS_RECORDING,false);
    };
    public void setEnableSmsRecording(boolean value)
    {
        preferences.edit()
                .putBoolean(ENABLE_SMS_RECORDING, value)
                .commit();
        emitOnChange();
    };

    private final static String HIDDEN_MODE = "HIDDEN_MODE";
    public boolean getHiddenMode()
    {
        return preferences.getBoolean(HIDDEN_MODE,false);
    };
    public void setHiddenMode(boolean value)
    {
        preferences.edit()
                .putBoolean(HIDDEN_MODE, value)
                .commit();
        emitOnChange();
    };

    private final static String USE_FILE_EXTENSION = "USE_FILE_EXTENSION";
    public boolean getUseFileExtension()
    {
        return preferences.getBoolean(USE_FILE_EXTENSION,false);
    };
    public void setUseFileExtension(boolean value)
    {
        preferences.edit()
                .putBoolean(USE_FILE_EXTENSION, value)
                .commit();
        emitOnChange();
    };

    private final static String SAVING_PATH = "SAVING_PATH";
    public String getSavingPath()
    {
        File file=cnt.getExternalFilesDir(null);
        String defPath = "file:\\\\"+file.getAbsolutePath();

        return preferences.getString(SAVING_PATH, file.getAbsolutePath());
    };
    public void setSavingPath(String value)
    {
        preferences.edit()
                .putString(SAVING_PATH, value)
                .commit();
        emitOnChange();
    };

    private final static String SOUND_SOURCE = "SOUND_SOURCE";
    enum SoundSource {MIC, VOICE_CALL, VOICE_COMMUNICATION};
    public SoundSource getSoundSource()
    {
        File file=cnt.getExternalFilesDir(null);

        int val= preferences.getInt(SOUND_SOURCE, SoundSource.MIC.ordinal());
        return SoundSource.values()[val];
    };
    public void setSoundSource(SoundSource value)
    {
        preferences.edit()
                .putInt(SAVING_PATH, value.ordinal())
                .commit();
        emitOnChange();
    };

    private final static String MAX_FILE_AMOUNT = "MAX_FILE_AMOUNT";
    public int getMaxFileAmount()
    {
        return preferences.getInt(MAX_FILE_AMOUNT, 1000);
    };
    public void setFileAmount(int value)
    {
        preferences.edit()
                .putInt(MAX_FILE_AMOUNT, value)
                .commit();
        emitOnChange();
    };

    private final static String MAX_DATA_SIZE = "MAX_DATA_SIZE";
    private final static String MAX_DATA_UNIT = "MAX_DATA_UNIT";
    public enum DataSizeUnit {BYTES, KBYTES, MBYTES, GBYTES, TBYTES};
    public class DataSize
    {
        long size;
        DataSizeUnit unit;
        long getBytes()
        {
            long res=size;
            for(int i=unit.ordinal();i>0;i--)
                res *= 1024;
            return res;
        }
        DataSize(long size, int unit)
        {
            this(size, DataSizeUnit.values()[unit]);
        };

        DataSize(long size, DataSizeUnit unit)
        {
            this.size=size;
            this.unit=unit;
        };
    }

    public DataSize getFileAmount()
    {
        long sz = preferences.getLong(MAX_DATA_SIZE, 64);
        int order = preferences.getInt(MAX_DATA_UNIT, 2);
        return new DataSize(sz, order);
    };

    public void setFileAmount(int value, DataSizeUnit unit)
    {
        preferences.edit()
                .putLong(MAX_DATA_SIZE, value)
                .putLong(MAX_DATA_UNIT, unit.ordinal())
                .commit();
        emitOnChange();
    };


    private final static String SECRET_NUMBER = "SECRET_NUMBER";
    public String getSecretNumber()
    {
        File file=cnt.getExternalFilesDir(null);

        return preferences.getString(SECRET_NUMBER,"*#12345#");
    };
    public void setSecretNumber(String value)
    {
        preferences.edit()
                .putString(SECRET_NUMBER, value)
                .commit();
        emitOnChange();
    };

}
