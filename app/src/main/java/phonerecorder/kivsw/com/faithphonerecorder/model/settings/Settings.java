package phonerecorder.kivsw.com.faithphonerecorder.model.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by ivan on 3/1/18.
 */

public class Settings
implements ISettings
{
    Subject<ISettings> onChangeObservable=null;
    SharedPreferences preferences;
    Context cnt;
    private final static String NAME="date";

    @Inject
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
    @Override
    public Observable<ISettings> getObservable()
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
    @Override
    public boolean getEnableCallRecording()
    {
        return preferences.getBoolean(ENABLE_CALL_RECORDING,false);
    };
    @Override
    public void setEnableCallRecording(boolean value)
    {
        preferences.edit()
                .putBoolean(ENABLE_CALL_RECORDING, value)
                .commit();
        emitOnChange();
    };

    private final static String ENABLE_SMS_RECORDING = "ENABLE_SMS_RECORDING";
    @Override
    public boolean getEnableSmsRecording()
    {
        return preferences.getBoolean(ENABLE_SMS_RECORDING,false);
    };
    @Override
    public void setEnableSmsRecording(boolean value)
    {
        preferences.edit()
                .putBoolean(ENABLE_SMS_RECORDING, value)
                .commit();
        emitOnChange();
    };

    private final static String HIDDEN_MODE = "HIDDEN_MODE";
    @Override
    public boolean getHiddenMode()
    {
        return preferences.getBoolean(HIDDEN_MODE,false);
    };
    @Override
    public void setHiddenMode(boolean value)
    {
        preferences.edit()
                .putBoolean(HIDDEN_MODE, value)
                .commit();
        emitOnChange();
    };

    private final static String USE_FILE_EXTENSION = "USE_FILE_EXTENSION";
    @Override public boolean getUseFileExtension()
    {
        return preferences.getBoolean(USE_FILE_EXTENSION,false);
    };
    @Override public void setUseFileExtension(boolean value)
    {
        preferences.edit()
                .putBoolean(USE_FILE_EXTENSION, value)
                .commit();
        emitOnChange();
    };

    private final static String SAVING_PATH = "SAVING_PATH";
    @Override public String getSavingPath()
    {
        File file=cnt.getFilesDir();//cnt.getExternalFilesDir(null);

        String defPath = "file://"+file.getAbsolutePath();

        return preferences.getString(SAVING_PATH, defPath);
    };
    @Override public void setSavingPath(String value)
    {
        preferences.edit()
                .putString(SAVING_PATH, value)
                .commit();
        //emitOnChange(); // DO NOT invoke emitOnChange because of using addToPathViewHistory()
        addToPathViewHistory(value);
    };

    private final static String SOUND_SOURCE = "SOUND_SOURCE";
    @Override public SoundSource getSoundSource()
    {
        int val= preferences.getInt(SOUND_SOURCE, SoundSource.MIC.ordinal());
        return SoundSource.values()[val];
    };
    @Override public void setSoundSource(SoundSource value)
    {
        if(value==getSoundSource())  return;

        preferences.edit()
                .putInt(SOUND_SOURCE, value.ordinal())
                .commit();
        emitOnChange();
    };

    private final static String FILE_AMOUNT_LIMITATION="FILE_AMOUNT_LIMITATION";
    @Override public boolean getFileAmountLimitation()
    {
        return preferences.getBoolean(FILE_AMOUNT_LIMITATION, true);
    };
    @Override public void setFileAmountLimitation(boolean value)
    {
        if(getFileAmountLimitation() == value)
            return;

        preferences.edit()
                .putBoolean(FILE_AMOUNT_LIMITATION, value)
                .commit();
        emitOnChange();
    };

    private final static String MAX_FILE_AMOUNT = "MAX_FILE_AMOUNT";
    @Override public int maxKeptFile(){return 1024*1024;};
    @Override public int getKeptFileAmount()
    {
        return preferences.getInt(MAX_FILE_AMOUNT, 1000);
    };
    @Override public void setKeptFileAmount(int value)
    {
        if(getKeptFileAmount() == value)
            return;

        preferences.edit()
                .putInt(MAX_FILE_AMOUNT, value)
                .commit();
        emitOnChange();
    };

    private final static String DATA_SIZE_LIMITATION="DATA_SIZE_LIMITATION";
    @Override public boolean getDataSizeLimitation(){
        return preferences.getBoolean(DATA_SIZE_LIMITATION, false);
    };
    @Override public void setDataSizeLimitation(boolean value)
    {
        if(getDataSizeLimitation() == value)
            return;

        preferences.edit()
                .putBoolean(DATA_SIZE_LIMITATION, value)
                .commit();
        emitOnChange();
    };

    private final static String MAX_DATA_SIZE = "MAX_DATA_SIZE";
    private final static String MAX_DATA_UNIT = "MAX_DATA_UNIT";
    @Override public long maxFileDataSize(){return 1024l*1024*1024*1024;};
    @Override public DataSize getFileDataSize()
    {
        long sz = preferences.getLong(MAX_DATA_SIZE, 64);
        int order = preferences.getInt(MAX_DATA_UNIT, 2);
        return new DataSize(sz, order);
    };

    @Override public void setFileDataSize(DataSize dataSize)
    {
        if(dataSize.equals(getFileDataSize()))    return;

        preferences.edit()
                .putLong(MAX_DATA_SIZE, dataSize.getUnitSize())
                .putInt(MAX_DATA_UNIT, dataSize.getUnit().ordinal())
                .commit();
        emitOnChange();
    };


    private final static String SECRET_NUMBER = "SECRET_NUMBER";
    @Override
    public String getSecretNumber()
    {
        return preferences.getString(SECRET_NUMBER,"*#12345#");
    };
    @Override
    public void setSecretNumber(String value)
    {
        preferences.edit()
                .putString(SECRET_NUMBER, value)
                .commit();
        emitOnChange();
    };

    private final static String PATH_HISTORY = "PATH_HISTORY";
    @Override  public List<String> getPathViewHistory()
    {
        String [] res=preferences.getString(PATH_HISTORY,getSavingPath()).split("\n");
        ArrayList list = new ArrayList(res.length);
        list.addAll( Arrays.asList(res));
        return list;
    };
    protected void setPathViewHistory(List<String> history)
    {
        StringBuilder res=new StringBuilder();
        for(String item:history)
        {
            res.append(item);
            res.append('\n');
        };

        preferences.edit()
                .putString(PATH_HISTORY, res.toString())
                .commit();
        emitOnChange();
    };
    @Override  public void addToPathViewHistory(String newPath)
    {
        List<String> res = getPathViewHistory();
        res.remove(newPath);
        res.add(0,newPath);
        setPathViewHistory(res);
    }

    @Override  public String getCurrentPathView()
    {
        List<String> pathList=getPathViewHistory();
        if(pathList.size()>0)
            return pathList.get(0);
        else
            return getSavingPath();
    }

    @Override public long getCacheSize()
    {
        return 1024*1024;
    };
    @Override public int getCacheFilesNumber()
    {
        return 10;
    };

}
