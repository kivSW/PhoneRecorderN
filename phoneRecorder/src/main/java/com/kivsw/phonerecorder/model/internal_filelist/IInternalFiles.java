package com.kivsw.phonerecorder.model.internal_filelist;

/**
 * this interface to access record files in the private directory
 */

public interface IInternalFiles {

    String[] getRecordFileList();
    String[] getFileListToSend();

    boolean isOverflow();

    void markFileAsSent(String fileName);
    boolean isSent(String fileName);
    void deleteOldFiles();
}
