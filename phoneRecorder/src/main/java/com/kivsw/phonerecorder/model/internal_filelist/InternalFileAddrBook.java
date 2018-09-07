package com.kivsw.phonerecorder.model.internal_filelist;

import com.kivsw.phonerecorder.model.addrbook.FileAddrBook;
import com.kivsw.phonerecorder.model.addrbook.PhoneAddrBook;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;

public class InternalFileAddrBook extends FileAddrBook {

    IInternalFiles internalFiles=null;

    public InternalFileAddrBook(String fileName, PhoneAddrBook phoneAddrBook, IErrorProcessor errorProcessor)
    {
        super(fileName, phoneAddrBook, errorProcessor);
    };

    @Override
    public void saveTo(String fileName) throws Exception
    {
        if(!isMustBeSaved())
            return ;
        if( internalFiles!=null)
                internalFiles.unmarkFileAsSent(fileName);
        super.saveTo(fileName);
    }


    public void setInternalFiles(IInternalFiles internalFiles) {
        this.internalFiles = internalFiles;
    }

}
