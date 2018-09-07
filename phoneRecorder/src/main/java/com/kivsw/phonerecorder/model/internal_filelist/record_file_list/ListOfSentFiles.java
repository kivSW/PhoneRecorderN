package com.kivsw.phonerecorder.model.internal_filelist.record_file_list;

import com.google.gson.Gson;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.model.utils.SimpleFileIO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ListOfSentFiles implements IListOfSentFiles {

    private Map<Object, String> sentFiles;

    public ListOfSentFiles()
    {
        sentFiles=null;
    };

    @Override
    public void addFile(String fileName)
    {
        fileName = SimpleFileIO.extractFileName(fileName);
        sentFiles.put(createKey(fileName), fileName);

    };
    @Override
    public boolean removeFile(String fileName)
    {

        return (sentFiles.remove(createKey(fileName))!=null);

    };
    @Override
    public boolean hasFile(String fileName)
    {
        boolean res = sentFiles.containsKey(createKey(fileName));
        return res;
    };
    @Override
    public void loadList(String filePath, String workdir)
    {
        String sentFilesList;
        synchronized (this) {
            sentFilesList = SimpleFileIO.readFile(filePath);
        }

        //Set<String> sentFiles = Collections.newSetFromMap(new ConcurrentHashMap());
        Map<Object, String> sentFiles = new ConcurrentHashMap();
        try {
            Gson gson = new Gson();
            Object[] dataList = gson.fromJson(sentFilesList, Object[].class);

            for (Object item : dataList) {
                String fn=item.toString();
                if(SimpleFileIO.fileExists(workdir+fn)) // if this file exists
                    sentFiles.put(createKey(fn) , fn);
            }

        }catch(Exception e){};

        this.sentFiles = sentFiles;
    };
    @Override
    public void saveList(String filePath)
    {

        Object[] files=sentFiles.values().toArray();

        Gson gson = new Gson();

        String data=gson.toJson(files);
        synchronized (this) {
            try {
                SimpleFileIO.writeFile(filePath, data);
            } catch (Exception e) {
            }
        }
    };

    protected Object createKey(String fileName)
    {
        fileName = SimpleFileIO.extractFileName(fileName);
        RecordFileNameData res = RecordFileNameData.decipherFileName(fileName);

        if(res.hashCode()==0)
            return fileName;
        return res;
    };
}
