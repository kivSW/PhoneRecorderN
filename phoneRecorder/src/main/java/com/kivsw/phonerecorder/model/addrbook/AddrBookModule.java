package com.kivsw.phonerecorder.model.addrbook;

import android.content.Context;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AddrBookModule {
    @Provides
    FileAddrBook provideInternalFileAddBook(String fileName, PhoneAddrBook phoneAddrBook, IErrorProcessor errorProcessor)
    {
        return new FileAddrBook(fileName, phoneAddrBook, errorProcessor);
    };

    @Provides
    @Singleton
    PhoneAddrBook providePhoneAddBook(Context context)
    {
        return new PhoneAddrBook(context);
    };

}
