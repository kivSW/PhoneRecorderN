package com.kivsw.phonerecorder.model.internal_filelist;

import com.kivsw.phonerecorder.model.addrbook.FileAddrBook;
import com.kivsw.phonerecorder.model.addrbook.PhoneAddrBook;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.internal_filelist.record_file_list.IListOfSentFiles;
import com.kivsw.phonerecorder.model.internal_filelist.record_file_list.ListOfSentFiles;
import com.kivsw.phonerecorder.model.settings.ISettings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class InternalFilesModule {
    @Singleton
    @Provides
    IInternalFiles provideInternalFiles(ISettings settings, IErrorProcessor errorProcessor, PhoneAddrBook phoneAddrBook)
    {
        String addrBookFilePath =  settings.getInternalTempPath() + FileAddrBook.DEFAULT_FILE_NAME;
        InternalFileAddrBook fileAddrBook = new InternalFileAddrBook(addrBookFilePath, phoneAddrBook, errorProcessor);
        IListOfSentFiles sentFiles = new ListOfSentFiles();

        InternalFiles res = new InternalFiles(settings, fileAddrBook, sentFiles, errorProcessor);
        fileAddrBook.setInternalFiles(res);

        return res;
    }
}
