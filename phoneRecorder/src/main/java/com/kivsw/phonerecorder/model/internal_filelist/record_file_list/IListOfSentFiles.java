package com.kivsw.phonerecorder.model.internal_filelist.record_file_list;

public interface IListOfSentFiles {

    void addFile(String fileName);
    boolean removeFile(String fileName);
    boolean hasFile(String fileName);

    void loadList(String filePath, String dataDir);
    void saveList(String filePath);
}
