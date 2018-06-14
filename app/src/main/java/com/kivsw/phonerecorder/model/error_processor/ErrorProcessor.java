package com.kivsw.phonerecorder.model.error_processor;

import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.ui.main_activity.MainActivityContract;

import java.util.List;

import io.reactivex.exceptions.CompositeException;

/**
 * Created by ivan on 5/7/18.
 */

public class ErrorProcessor implements IErrorProcessor {

    private IJournal persistentData;
    private MainActivityContract.IMainActivityPresenter mainActivityPresenter;

    ErrorProcessor(IJournal persistentData, MainActivityContract.IMainActivityPresenter mainActivityPresenter)
    {
        this.persistentData = persistentData;
        this.mainActivityPresenter = mainActivityPresenter;
    }

    @Override
    public void onError(Throwable exception)
    {
        onError(exception, true);
    };
    @Override
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
                    persistentData.journalAdd(t);

            }
        }
        else
        {
            message.append(exception.getMessage());
            if(writeToJournal)
                persistentData.journalAdd(exception);
        }

        mainActivityPresenter.showErrorMessage(message.toString());
    };
}