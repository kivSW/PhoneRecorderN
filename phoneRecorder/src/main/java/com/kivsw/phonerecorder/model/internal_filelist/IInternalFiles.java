package com.kivsw.phonerecorder.model.internal_filelist;

import com.kivsw.phonerecorder.model.addrbook.FileAddrBook;

/**
 * this interface to access record files in the private directory
 */

public interface IInternalFiles {

    String[] getRecordFileList();
    String[] getFileListToSend(boolean includeJournal);

    boolean isOverflow();

    void markFileAsSent(String fileName);
    void unmarkFileAsSent(String fileName);
    boolean isSent(String fileName);
    void deleteOldFiles();

    FileAddrBook getInternalAddrBook();
}
