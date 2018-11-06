package com.kivsw.phonerecorder.model.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.model.settings.types.DataSize;
import com.kivsw.phonerecorder.model.settings.types.SoundSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 
 */

public class Settings
implements ISettings {
    private Subject<String> onChangeObservable = null;
    private SharedPreferences preferences;
    private Context cnt;
    private final static String NAME = "date";

    @Inject
    Settings(Context cnt) {
        this.cnt = cnt;
        onChangeObservable = PublishSubject.create();
        preferences = cnt.getSharedPreferences(NAME, Context.MODE_PRIVATE);

    }

    /**
     * @return observable that emitts whenever an option was changed
     */
    @Override
    public Observable<String> getObservable() {
        return onChangeObservable;
    }

    private void emitOnChange(String id) {
        onChangeObservable.onNext(id);
    }

    /**
     * parameters
     */

    private final static String ENABLE_CALL_RECORDING = "ENABLE_CALL_RECORDING";

    @Override
    public boolean getEnableCallRecording() {
        return preferences.getBoolean(ENABLE_CALL_RECORDING, false);
    }

    @Override
    public void setEnableCallRecording(boolean value) {
        if(value==getEnableCallRecording())
            return;
        preferences.edit()
                .putBoolean(ENABLE_CALL_RECORDING, value)
                .apply();
        emitOnChange(ENABLE_CALL_RECORDING);
    }

    public final static String ENABLE_SMS_RECORDING = "ENABLE_SMS_RECORDING";

    @Override
    public boolean getEnableSmsRecording() {
        return preferences.getBoolean(ENABLE_SMS_RECORDING, false);
    }

    @Override
    public void setEnableSmsRecording(boolean value) {
        if(value==getEnableSmsRecording())
            return;
        preferences.edit()
                .putBoolean(ENABLE_SMS_RECORDING, value)
                .apply();
        emitOnChange(ENABLE_SMS_RECORDING);
    }

    public final static String HIDDEN_MODE = "HIDDEN_MODE";

    @Override
    public boolean getHiddenMode() {
        return preferences.getBoolean(HIDDEN_MODE, false);
    }

    @Override
    public void setHiddenMode(boolean value) {
        preferences.edit()
                .putBoolean(HIDDEN_MODE, value)
                .apply();
        emitOnChange(HIDDEN_MODE);
    }

    private final static String USE_FILE_EXTENSION = "USE_FILE_EXTENSION";

    @Override
    public boolean getUseFileExtension() {
        return preferences.getBoolean(USE_FILE_EXTENSION, true);
    }

    @Override
    public void setUseFileExtension(boolean value) {
        preferences.edit()
                .putBoolean(USE_FILE_EXTENSION, value)
                .apply();
        emitOnChange(USE_FILE_EXTENSION);
    }

    private final static String USE_MOBILE_INTERNET = "USE_MOBILE_INTERNET";

    @Override
    public boolean getUsingMobileInternet() {
        return preferences.getBoolean(USE_MOBILE_INTERNET, false);
    }

    @Override
    public void setUsingMobileInternet(boolean value) {
        preferences.edit()
                .putBoolean(USE_MOBILE_INTERNET, value)
                .apply();
        emitOnChange(USE_MOBILE_INTERNET);
    }

    private final static String SEND_IN_ROAMING = "SEND_IN_ROAMING";
    @Override
    public boolean getAllowSendingInRoaming() {
        return preferences.getBoolean(SEND_IN_ROAMING, false);
    }

    @Override
    public void setSendInRoaming(boolean value)
    {
        preferences.edit()
                .putBoolean(SEND_IN_ROAMING, value)
                .apply();
        emitOnChange(SEND_IN_ROAMING);
    }

    private final static String JOURNAL_EXPORTING= "JOURNAL_EXPORTING";
    private boolean allowExportingJournal=false;
    @Override
    public boolean getAllowExportingJournal() {
        //return false;
        //return preferences.getBoolean(JOURNAL_EXPORTING,false);
        return allowExportingJournal;
    }

    @Override
    public void setAllowExportingJournal(boolean value) {
        allowExportingJournal = value;
        /*preferences.edit()
                .putBoolean(JOURNAL_EXPORTING, value)
                .apply();*/
        emitOnChange(JOURNAL_EXPORTING);
    }

    private final static String USE_INTERNAL_PLAYER= "USE_INTERNAL_PLAYER";
    @Override
    public boolean getUseInternalPlayer()
    {
         return preferences.getBoolean(USE_INTERNAL_PLAYER,true);
    };

    @Override
    public void setUseInternalPlayer(boolean value)
    {
        preferences.edit()
                .putBoolean(USE_INTERNAL_PLAYER, value)
                .apply();
        emitOnChange(USE_INTERNAL_PLAYER);
    };

    private final static String ABONENT_TO_FILENAME= "ABONENT_TO_FILENAME";
    @Override
    public boolean getAbonentToFileName()
    {
        return preferences.getBoolean(ABONENT_TO_FILENAME,true);
    }
    public void setAbonentToFileName(boolean value)
    {
        preferences.edit()
                .putBoolean(ABONENT_TO_FILENAME, value)
                .apply();
        emitOnChange(ABONENT_TO_FILENAME);
    }

    public final static String SAVING_PATH = "SAVING_PATH";
    @Override public String getSavingUrlPath()
    {
        String res=preferences.getString(SAVING_PATH, null);
        if(res!=null)
            return res;

        //File file=cnt.getFilesDir();//
        File file= cnt.getExternalFilesDir(null);
        String defPath = "file://"+addLastSeparator(file.getAbsolutePath());
        setSavingUrlPath(defPath);
        return preferences.getString(SAVING_PATH, defPath);
    }
    @Override public void setSavingUrlPath(String value)
    {
        preferences.edit()
                .putString(SAVING_PATH, value)
                .apply();

        addToViewUrlPathHistory(value);
        emitOnChange(SAVING_PATH);
    }

    protected String addLastSeparator(String path)
    {
        if(path==null || path.length()==0)
            return String.valueOf(File.separatorChar);

        if(path.charAt(path.length()-1)!= File.separatorChar)
            return path + File.separatorChar;

        return path;
    }
    @Override public String getInternalTempPath()
    {
        String res= cnt.getFilesDir().getAbsolutePath();

        return addLastSeparator(res);
    }

    private final static String SOUND_SOURCE = "SOUND_SOURCE";
    @Override public SoundSource getSoundSource()
    {
        int val= preferences.getInt(SOUND_SOURCE, SoundSource.MIC.ordinal());
        return SoundSource.values()[val];
    }
    @Override public void setSoundSource(SoundSource value)
    {
        if(value.equals(getSoundSource()))  return;

        preferences.edit()
                .putInt(SOUND_SOURCE, value.ordinal())
                .apply();
        emitOnChange(SOUND_SOURCE);
    }

    private final static String FILE_AMOUNT_LIMITATION="FILE_AMOUNT_LIMITATION";
    @Override public boolean getFileAmountLimitation()
    {
        return preferences.getBoolean(FILE_AMOUNT_LIMITATION, true);
    }
    @Override public void setFileAmountLimitation(boolean value)
    {
        if(getFileAmountLimitation() == value)
            return;

        preferences.edit()
                .putBoolean(FILE_AMOUNT_LIMITATION, value)
                .apply();
        emitOnChange(FILE_AMOUNT_LIMITATION);
    }

    private final static String MAX_FILE_AMOUNT = "MAX_FILE_AMOUNT";
    @Override public int maxKeptFileAmount(){return 128*1024;}
    @Override public int getKeptFileAmount()
    {
        return preferences.getInt(MAX_FILE_AMOUNT, 10000);
    }
    @Override public void setKeptFileAmount(int value)
    {
        if(getKeptFileAmount() == value)
            return;

        preferences.edit()
                .putInt(MAX_FILE_AMOUNT, value)
                .apply();
        emitOnChange(MAX_FILE_AMOUNT);
    }

    private final static String DATA_SIZE_LIMITATION="DATA_SIZE_LIMITATION";
    @Override public boolean getDataSizeLimitation(){
        return preferences.getBoolean(DATA_SIZE_LIMITATION, false);
    }
    @Override public void setDataSizeLimitation(boolean value)
    {
        if(getDataSizeLimitation() == value)
            return;

        preferences.edit()
                .putBoolean(DATA_SIZE_LIMITATION, value)
                .apply();
        emitOnChange(DATA_SIZE_LIMITATION);
    }

    private final static String MAX_DATA_SIZE = "MAX_DATA_SIZE";
    private final static String MAX_DATA_UNIT = "MAX_DATA_UNIT";
    @Override public long maxFileDataSize(){return 1024L*1024*1024*1024;}
    @Override public DataSize getFileDataSize()
    {
        long sz = preferences.getLong(MAX_DATA_SIZE, 64);
        int order = preferences.getInt(MAX_DATA_UNIT, 2);
        return new DataSize(sz, order);
    }

    @Override public void setFileDataSize(DataSize dataSize)
    {
        if(dataSize.equals(getFileDataSize()))    return;

        preferences.edit()
                .putLong(MAX_DATA_SIZE, dataSize.getUnitSize())
                .putInt(MAX_DATA_UNIT, dataSize.getUnit().ordinal())
                .apply();
        emitOnChange(MAX_DATA_SIZE);
    }


    private final static String SECRET_NUMBER = "SECRET_NUMBER";
    @Override
    public String getSecretNumber()
    {
        return preferences.getString(SECRET_NUMBER,"*#12345#");
    }
    @Override
    public void setSecretNumber(String value)
    {
        preferences.edit()
                .putString(SECRET_NUMBER, value)
                .apply();
        emitOnChange(SECRET_NUMBER);
    }

    public final static String PATH_HISTORY = "PATH_HISTORY";
    @Override  public List<String> getViewUrlPathHistory()
    {
        String [] res=preferences.getString(PATH_HISTORY, getSavingUrlPath())
                .split("\n");

        ArrayList<String> list = new ArrayList(res.length);
        list.addAll( Arrays.asList(res));

        for(int i=res.length-1; // because of the bag: https://issuetracker.google.com/issues/37032278#c6
            i>=0 && list.get(i).trim().length()==0 ;
            i--)
        {
            list.remove(i);
        };
        return list;
    }
    protected void setPathViewHistory(List<String> history)
    {
        StringBuilder res=new StringBuilder();
        for(String item:history)
        {
            res.append(item);
            res.append('\n');
        }
        if(res.length()>0)
        res.setLength(res.length()-1); // remove the last '\n' because of the bag: https://issuetracker.google.com/issues/37032278#c6

        preferences.edit()
                .putString(PATH_HISTORY, res.toString())
                .apply();
        emitOnChange(PATH_HISTORY);
    }
    @Override  public void addToViewUrlPathHistory(String newUrlPath)
    {
        newUrlPath = addLastSeparator(newUrlPath);

        List<String> res = getViewUrlPathHistory();
        res.remove(newUrlPath);
        res.add(0, newUrlPath);
        setPathViewHistory(res);
    }

    @Override  public String getCurrentViewUrlPath()
    {
        List<String> pathList= getViewUrlPathHistory();
        if(pathList.size()>0)
            return addLastSeparator(pathList.get(0));
        else
            return getSavingUrlPath();
    }

    @Override public long getCacheSize()
    {
        return 1024*1024;
    }
    @Override public int getCacheFilesNumber()
    {
        return 10;
    }

    public final static String ANTI_TASKKILLER_NOTOFICATION = "ANTI_TASKKILLER_NOTOFICATION";
                                //ANTI_TASK_KILLER_NOTOFICATION_TEXT = "ANTI_TASK_KILLER_NOTOFICATION_TEXT",
    private final static String ANTI_TASK_KILLER_NOTOFICATION_ICON = "ANTI_TASK_KILLER_NOTOFICATION_ICON";
    @Override public AntiTaskKillerNotificationParam getAntiTaskKillerNotification()
    {
        return
                new AntiTaskKillerNotificationParam( preferences.getBoolean(ANTI_TASKKILLER_NOTOFICATION,false),
                                                //preferences.getString(ANTI_TASK_KILLER_NOTOFICATION_TEXT, cnt.getText(R.string.app_name).toString()),
                                                preferences.getInt(ANTI_TASK_KILLER_NOTOFICATION_ICON, 0) );
    }
    @Override public void setAntTaskKillerNotification(AntiTaskKillerNotificationParam param)
    {
        if(getAntiTaskKillerNotification().equals(param))    return;

        preferences.edit()
                .putBoolean(ANTI_TASKKILLER_NOTOFICATION, param.visible)
                //.putString(ANTI_TASK_KILLER_NOTOFICATION_TEXT, param.text)
                .putInt(ANTI_TASK_KILLER_NOTOFICATION_ICON, param.iconNum)
                .apply();
        emitOnChange(ANTI_TASKKILLER_NOTOFICATION);
    }


}
