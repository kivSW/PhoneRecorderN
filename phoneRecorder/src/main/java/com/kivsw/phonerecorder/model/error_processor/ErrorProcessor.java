package com.kivsw.phonerecorder.model.error_processor;

import android.content.Context;
import android.widget.Toast;

import com.kivsw.phonerecorder.model.metrica.IMetrica;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.ui.main_activity.MainActivityContract;

import java.util.List;

import io.reactivex.exceptions.CompositeException;

/**
 * Created by ivan on 5/7/18.
 */

public class ErrorProcessor implements IErrorProcessor {

    private IJournal journal;
    private MainActivityContract.IMainActivityPresenter mainActivityPresenter;
    private IMetrica metrica;
    private Context appContext;


    ErrorProcessor(Context appContext, IJournal journal, MainActivityContract.IMainActivityPresenter mainActivityPresenter, IMetrica metrica)
    {
        this.journal = journal;
        this.mainActivityPresenter = mainActivityPresenter;
        this.metrica = metrica;
        this.appContext = appContext;
    }

    protected void doShowMessage(String message)
    {
        mainActivityPresenter.showErrorMessage(message);
    }
    protected void doShowToast(String message)
    {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG)
                .show();
    }
    @Override
    public void onError(Throwable exception)
    {
        onError(exception, true);
    };
    @Override
    public void onSmallError(Throwable exception)
    {
        onError(exception, false);
    };


    public void onError(Throwable exception, boolean writeToJournal)
    {
        StringBuilder message=new StringBuilder();

        if(exception instanceof CompositeException)
        {
            List<Throwable> exceptions=((CompositeException)exception).getExceptions();
            for(Throwable t:exceptions)
            {
                message.append(t.getMessage());
                message.append("\n");
                if(writeToJournal)
                    journal.journalAdd(t);
            }
        }
        else
        {
            message.append(exception.getMessage());
            if(writeToJournal) {
                journal.journalAdd(exception);
                metrica.notifyError(exception);
            }
        }
        if(writeToJournal)
            doShowMessage(message.toString());
        else
            doShowToast(message.toString());

    };
}
