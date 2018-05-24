package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;

/**
 * Created by ivan on 5/22/18.
 */

public class RecListFilter {

    private String filter;
    private int eventCount;

    private IErrorProcessor errorProcessor;

    private List<RecordListContract.RecordFileInfo> records;
    private Subject<List<RecordListContract.RecordFileInfo>> subject;

    private Disposable processChainDisposable;
    private Subject<List<RecordListContract.RecordFileInfo>> processChain;

    public RecListFilter(IErrorProcessor errorProcessor)
    {
        filter="";
        this.errorProcessor = errorProcessor;
        records = new ArrayList<>(1024);
        subject = PublishSubject.create();
        createProcessChain();
    };


    protected void createProcessChain()
    {
        if(processChainDisposable!=null)
            processChainDisposable.dispose();
        processChainDisposable=null;

        eventCount=0;
        processChain=PublishSubject.create();
        processChain
                .observeOn(Schedulers.io())
                //.observeOn(Schedulers.newThread())
                .map(new Function<List<RecordListContract.RecordFileInfo>, List<RecordListContract.RecordFileInfo>>() {
                    @Override
                    public List<RecordListContract.RecordFileInfo> apply(List<RecordListContract.RecordFileInfo> records) throws Exception {
                        List<RecordListContract.RecordFileInfo> res = new ArrayList(records.size());
                        for(RecordListContract.RecordFileInfo item:records)
                        {
                            if(checkFilter(item, filter))
                                res.add(item);
                        };
                        return res;
                    }
                })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RecordListContract.RecordFileInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        processChainDisposable = d;
                    }

                    @Override
                    public void onNext(List<RecordListContract.RecordFileInfo> newRecords) {

                        int oldSize=records.size();
                        records.addAll(newRecords);
                        if(oldSize>0 && records.size()>oldSize+1
                           && (records.get(oldSize-1).compareTo(records.get(oldSize)) <0)
                          )
                        {
                            int i,s;
                            for(i=1,s=records.size(); i<s; i++)
                            {
                                if(records.get(i-1).compareTo(records.get(i))<0)
                                    break;
                            }
                            records.get(i-1).compareTo(records.get(i));
                            Collections.sort(records, Collections.reverseOrder());
                        };
                        subject.onNext(records);
                        eventCount--;
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorProcessor.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    protected boolean checkFilter(RecordListContract.RecordFileInfo fileData, String filter)
    {
        if (fileData.recordFileNameData.phoneNumber.indexOf(filter) >= 0) return true;
        if (fileData.callerName.toLowerCase().indexOf(filter) >= 0) return true;

        return false;
    }

    public Observable<List<RecordListContract.RecordFileInfo>> getObservable()
    {
        return subject;
    };

    public void clearData()
    {
        records.clear();
        createProcessChain();
    };

    public void addData(List<RecordListContract.RecordFileInfo> newRecords)
    {
        eventCount++;
        processChain.onNext(newRecords);
    };

    public boolean isProcessing()
    {
        return eventCount>0;
    };

    public void setFilter(String filter)
    {
        this.filter = filter.toLowerCase();
    };

}
