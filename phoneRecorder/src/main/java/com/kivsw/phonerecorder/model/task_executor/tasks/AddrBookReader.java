package com.kivsw.phonerecorder.model.task_executor.tasks;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.kivsw.phonerecorder.model.addrbook.FileAddrBook;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.internal_filelist.IInternalFiles;
import com.kivsw.phonerecorder.model.persistent_data.IPersistentDataKeeper;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.ui.notification.NotificationShower;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * reads address book from the phone and saveList it into interval file of FileAddrBook
 */
public class AddrBookReader implements ITask {
    Context context;
    ISettings settings;
    IPersistentDataKeeper dataKeeper;
    ITaskExecutor taskExecutor;
    NotificationShower notification;
    IInternalFiles internalFiles;
    IErrorProcessor errorProcessor;
    FileAddrBook fileAddrBook;

    public AddrBookReader(Context context, ISettings settings, IPersistentDataKeeper dataKeeper, ITaskExecutor taskExecutor,
                          NotificationShower notification, IInternalFiles internalFiles, IErrorProcessor errorProcessor)
    {

        this.context = context;
        this.settings = settings;
        this.dataKeeper = dataKeeper;
        this.taskExecutor = taskExecutor;
        this.notification = notification;
        this.internalFiles = internalFiles;
        this.errorProcessor = errorProcessor;

        fileAddrBook = internalFiles.getInternalAddrBook();

    }

    final long READ_PERIOD=3600*24*1000;

    @Override
    public boolean startTask() {

        final long currentTime = System.currentTimeMillis();
        if(Math.abs(dataKeeper.getLastTimeOfReadingAddrBook()-currentTime) < READ_PERIOD )
            return false;

        Single.fromCallable(()->{
            doReadAddrBook();
            return "";
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String t) {
                taskExecutor.stopAddrBookReading();
                dataKeeper.setLastTimeOfReadingAddrBook(currentTime);
                try {
                    fileAddrBook.save();
                }catch(Exception e)
                {
                    errorProcessor.onError(e);
                }
            }

            @Override
            public void onError(Throwable e) {
                   taskExecutor.stopAddrBookReading();
                   errorProcessor.onError(e);
            }
        });
        ;
        return false;
    }

    @Override
    public void stopTask() {

    }

    protected void doReadAddrBook()
    {
        Cursor phones=null;
        try {
            phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                fileAddrBook.addItem(name, phoneNumber);
            }
        }finally {
            if(phones!=null)
                phones.close();
        }

    };
}
