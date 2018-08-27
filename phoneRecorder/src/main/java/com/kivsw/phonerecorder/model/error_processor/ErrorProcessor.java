package com.kivsw.phonerecorder.model.error_processor;

import android.content.Context;
import android.widget.Toast;

import com.kivsw.cloud.OAuth.OAuthCancelledException;
import com.kivsw.phonerecorder.model.metrica.IMetrica;
import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.ui.main_activity.MainActivityContract;

import java.util.List;

import io.reactivex.exceptions.CompositeException;
import phonerecorder.kivsw.com.phonerecorder.R;

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

    @Override
    public void onError(Throwable exception)
    {
        onError(exception, true);
    };

    @Override
    public void onError(Throwable exception, boolean alwaysShowMessage)
    {
        processError(exception, true,
                alwaysShowMessage?ErrorIndication.ShowMessageAlways:ErrorIndication.ShowMessageIfActivityIsVisible);
    };
    @Override
    public void onSmallError(Throwable exception)
    {
        processError(exception, false, ErrorIndication.ShowToast);
    };

    public enum ErrorIndication {ShowMessageAlways, ShowMessageIfActivityIsVisible, ShowToast}

    public void processError(Throwable exception, boolean writeToJournal, ErrorIndication indication)
    {
        StringBuilder message=new StringBuilder();

        if(exception instanceof OAuthCancelledException)
        {
            exception = new Exception(appContext.getString(R.string.auth_cancelled));
        }

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

        doIndicateError(message.toString(), indication);

    };

    protected void doIndicateError(String message, ErrorIndication indication)
    {
        if(message!=null && !message.isEmpty())
            switch(indication) {
                case ShowMessageAlways:
                    doShowMessage(message.toString(), true);
                    break;
                case ShowMessageIfActivityIsVisible:
                    doShowMessage(message.toString(), false);
                    break;
                case ShowToast:
                    doShowToast(message.toString());
                    break;
            }
    }

    protected void doShowMessage(String message, boolean alwaysShow)
    {
        mainActivityPresenter.showErrorMessage(message, alwaysShow);
    }
    protected void doShowToast(String message)
    {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG)
                .show();
    }
}
