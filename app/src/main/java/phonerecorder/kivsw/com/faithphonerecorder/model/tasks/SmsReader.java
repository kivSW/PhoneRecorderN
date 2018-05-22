package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IJournal;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentDataKeeper;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.ui.notification.NotificationShower;

/**
 * thuis class Reads sms and sends them to
 */

public class SmsReader implements ITask  {

    private Context context;
    private ISettings settings;
    private IJournal journal;
    private TaskExecutor taskExecutor;
    private NotificationShower notification;
    private IErrorProcessor errorProcessor;
    private IPersistentDataKeeper persistentDataKeeper;

    public SmsReader(Context context, ISettings settings, IJournal journal, IPersistentDataKeeper persistentDataKeeper, TaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor)
    {
        this.context=context;
        this.settings=settings;
        this.journal=journal;
        this.taskExecutor=taskExecutor;
        this.notification=notification;
        this.errorProcessor=errorProcessor;
        this.persistentDataKeeper = persistentDataKeeper;
    }

    @Override
    public boolean startTask() {
        if(!settings.getEnableSmsRecording())
            return false;

        final long lastIncomeSmsId=persistentDataKeeper.getLastIncomeSms(),
                   lastOutgoingSmsId=persistentDataKeeper.getLastOutgoingSms();

        Observable.fromCallable(new Callable<List<Sms> >() {
            @Override
            public List<Sms>  call() throws Exception {
                List<Sms> in=readSms(true, lastIncomeSmsId);
                List<Sms> out=readSms(false, lastOutgoingSmsId);

                in.addAll(out);
                Collections.reverse(in);
                //Collections.sort(in);
                return in;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .flatMap(new Function<List<Sms>, ObservableSource<Sms>>() {
            @Override
            public ObservableSource<Sms> apply(List<Sms> smsList) throws Exception {
                return Observable.fromIterable(smsList);
            }
        })
        .doOnNext(new Consumer<Sms>() {
            @Override
            public void accept(Sms sms) throws Exception {
                saveSms(sms);
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Sms>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onNext(Sms sms) {
                if(sms.isIncome()) persistentDataKeeper.setLastIncomeSms(sms.date);
                else persistentDataKeeper.setLastOutgoingSms(sms.date);
            }

            @Override
            public void onComplete() {
                taskExecutor.stopSMSreading();
                taskExecutor.startFileSending();
            }

            @Override
            public void onError(Throwable e) {
                errorProcessor.onError(e);
                taskExecutor.stopSMSreading();
                taskExecutor.startFileSending();
            }
        });

        return false;
    }

    @Override
    public void stopTask() {

    }

    static class Sms implements Comparable
    {
        String body,
               address;
        long date;
        int  type;
        boolean isIncome(){return type==1;}

        @Override
        public int compareTo(@NonNull Object o) {
            long diff= (date - ((Sms)o).date);
            if(diff<0) return -1;
            if(diff>0) return 1;
            return 0;
        }
    }

    protected List<Sms> readSms(boolean in, long last_id)
    {
        Uri uri;
        if(in) uri = Uri.parse("content://sms/inbox");
        else   uri = Uri.parse("content://sms/sent");

        Cursor cursor=null;
        ArrayList<Sms> res=new ArrayList<>();
        try {
            String where = "date>"+String.valueOf(last_id);
            cursor = context.getContentResolver().query(uri, null, where, null, null);
            //String[] names=cursor.getColumnNames();
            int bodyIndex=cursor.getColumnIndexOrThrow("body"),
                    addressIndex = cursor.getColumnIndexOrThrow("address"),
                    dateIndex = cursor.getColumnIndexOrThrow("date"),
                    typeIndex = cursor.getColumnIndexOrThrow("type");

            if(cursor.moveToFirst())
            do{
                int type=cursor.getType(dateIndex);
                Sms sms=new Sms();
                sms.address=cursor.getString(addressIndex);
                sms.body = cursor.getString(bodyIndex);
                sms.date = cursor.getLong(dateIndex);
                sms.type =  cursor.getInt(typeIndex);

                res.add(sms);
            }while(cursor.moveToNext());
        }
        catch (Exception e)
        {
            errorProcessor.onError(e);
        };
        if(cursor!=null)
            cursor.close();

     return res;
    }

    protected void saveSms(Sms sms) throws IOException
    {
        String fileName = createFileName(sms);

        FileWriter writer = new FileWriter(fileName,true);
        writer.append(sms.body);
        writer.close();

    };
    protected String createFileName(Sms sms)
    {
        Date date= new Date(sms.date);

        String recordFileNameData = RecordFileNameData.generateNew(date, sms.address, sms.isIncome(), "", "sms")
                .buildFileName();
        return settings.getInternalTempPath() + recordFileNameData;
    }

}
