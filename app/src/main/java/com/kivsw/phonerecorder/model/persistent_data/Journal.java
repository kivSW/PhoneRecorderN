package com.kivsw.phonerecorder.model.persistent_data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 4/26/18.
 */

public class Journal implements IJournal {

    private ISettings settings;
    private IErrorProcessor errorProcessor;
    private Context cnt;
    private String fileName;
    final private long MAX_LENGTH=1024*32;


    final static public String JOURNAL_FILE_NAME="journal";

    @Inject
    public Journal(Context context, ISettings settings) {
        cnt = context;
        errorProcessor=null;
        this.settings = settings;
        fileName = settings.getInternalTempPath() + JOURNAL_FILE_NAME;

    }


    protected void removeOldFile() throws Exception
    {
        File file = new File(fileName);
        if (file.length() > MAX_LENGTH) {
            File oldFile = new File(fileName + ".old");
            if(oldFile.exists())
                oldFile.delete();
            file.renameTo(oldFile);
        }
    }
    @Override
    public void journalAdd(String message)
    {
        try {
            removeOldFile();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //sdf.format(new Date());

            FileWriter writer = new FileWriter(fileName,true);
            writer.append("\n\n");
            writer.append(sdf.format(new Date()));
            writer.append('\n');
            writer.append(message);
            writer.close();
        }catch(Exception e)
        {
           if(errorProcessor!=null)
               errorProcessor.onError(e, false);
        }
    };

    @Override
    public void journalAdd(Throwable throwable)
    {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        throwable.printStackTrace(pw);
        journalAdd(writer.toString());
    };

    @Override
    public void journalAdd(Intent intent)
    {
       StringBuilder info=new StringBuilder();
       info.append("Intent: ");
       info.append(intent.getAction());

       info.append("\nData: ");
       if(intent.getData()!=null)  info.append(intent.getData().toString());
       else   info.append("null");

       info.append("\nExtras:");
       Bundle extra=intent.getExtras();
       if(extra != null) {

           Set<String> keys = extra.keySet();
           for(String key:keys) {
               info.append(key);
               info.append(':');
               info.append(extra.get(key));
               info.append(" , ");
           }
       }

       journalAdd(info.toString());
    };

    @Override
    public void setErrorProcessor(IErrorProcessor errorProcessor)
    {
        this.errorProcessor = errorProcessor;
    };
}
