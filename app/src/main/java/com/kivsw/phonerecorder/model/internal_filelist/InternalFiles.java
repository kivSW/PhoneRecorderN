package com.kivsw.phonerecorder.model.internal_filelist;

import com.google.gson.Gson;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.persistent_data.Journal;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.model.utils.SimpleFileIO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class controls record files in private directory
 */

public class InternalFiles implements IInternalFiles {

    private ISettings settings;
    private IErrorProcessor errorProcessor;
    private Map<String, Object> sentFiles;
    private String filePath;

    private static final int MAX_FILES_NUM = 20;


    InternalFiles( ISettings settings, IErrorProcessor errorProcessor)
    {
        this.settings = settings;
        this.errorProcessor = errorProcessor;
        filePath = settings.getInternalTempPath() + "sentFileList";
        loadSentFileList();

    };

    @Override
    public String[] getRecordFileList()
    {
        return getFileList(settings.getInternalTempPath(), RecordFileNameData.RECORD_PATTERN);
    };

    @Override
    public String[] getFileListToSend()
    {
        String pattern;
        if(settings.getAllowExportingJournal())
            pattern = "("+ RecordFileNameData.RECORD_PATTERN+"|^"+ Journal.JOURNAL_FILE_NAME+")";
        else
            pattern = RecordFileNameData.RECORD_PATTERN;

        String fileList[] = getFileList(settings.getInternalTempPath(), pattern);
        String res[] = removeSentFiles(fileList);
        return res;
    };

    @Override
    public void markFileAsSent(String fileName) {
        fileName = SimpleFileIO.extractFileName(fileName);
        if(fileName.indexOf(Journal.JOURNAL_FILE_NAME)==0)
                return;
        sentFiles.put(fileName, Boolean.FALSE);
        saveSentFileList();
    }

    @Override
    public boolean isSent(String fileName) {
        fileName = SimpleFileIO.extractFileName(fileName);
        /*Object v=sentFiles.get(fileName);
        return v!=null;*/
        boolean res = sentFiles.containsKey(fileName);
        return res;
    }

    @Override
    public void deleteOldFiles() {
        String[] files = getRecordFileList();

        int filecount=0;
        boolean modified=false;
        for(String item:files)
        {
            filecount++;
            if(!isSent(item)) continue;

            if(filecount>MAX_FILES_NUM)
            {
                File file=new File(settings.getInternalTempPath(), item);
                if(file.delete()) {
                    sentFiles.remove(item);
                    modified=true;
                }
            }
        }
        if(modified)
            saveSentFileList();

    }

    private void loadSentFileList()
    {
        Set<String> availableFiles = new HashSet(Arrays.asList(getRecordFileList()));

        String sentFilesList;
        synchronized (this) {
            sentFilesList = SimpleFileIO.readFile(filePath);
        }

        //Set<String> sentFiles = Collections.newSetFromMap(new ConcurrentHashMap());
        Map<String, Object> sentFiles = new ConcurrentHashMap();
        try {
            Gson gson = new Gson();
            Object[] dataList = gson.fromJson(sentFilesList, Object[].class);

            for (Object item : dataList) {
                String fn=item.toString();
                if(availableFiles.contains(fn)) // if this file exists
                   sentFiles.put(fn, Boolean.FALSE);
            }
            ;
        }catch(Exception e){};

        this.sentFiles = sentFiles;

    };
    private void saveSentFileList()
    {
        Object[] values=sentFiles.keySet().toArray();//sentFiles.toArray();
        Gson gson = new Gson();

        String data=gson.toJson(values);
        synchronized (this) {
            try {
                SimpleFileIO.writeFile(filePath, data);
            } catch (Exception e) {
                errorProcessor.onError(e);
            }
        }

    };

    protected String[] getFileList(String localDir, String regExp)
    {
        File dir=new File(localDir);

        final Pattern p = Pattern.compile(regExp);
        String[] fileList = dir.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                Matcher m = p.matcher(name);
                return m.find();
            }
        });
        if(fileList==null)
            fileList = new String[0];

        return removeEmptyFiles(localDir, fileList);
    }

    protected String[] removeEmptyFiles(String localDir, String[] fileList)
    {
        ArrayList<String> res = new ArrayList(fileList.length);
        for(String fileName:fileList)
        {
            File file=new File(localDir, fileName);
            if(!file.exists() || file.length()<1)
                file.delete();
            else
                res.add(fileName);
        };
        return res.toArray(new String[res.size()]);
    };

    protected String[] removeSentFiles(String[] fileList)
    {
        Arrays.sort(fileList, Collections.reverseOrder());
        ArrayList<String> res = new ArrayList(fileList.length);
        for(String fileName:fileList)
        {
            if(isSent(fileName)) continue;
            res.add(fileName);
        };
        return res.toArray(new String[res.size()]);
    };


}
