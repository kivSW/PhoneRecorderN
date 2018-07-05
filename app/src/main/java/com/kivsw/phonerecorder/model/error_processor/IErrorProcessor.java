package com.kivsw.phonerecorder.model.error_processor;

/**
 * Created by ivan on 5/7/18.
 */

public interface IErrorProcessor {
    void onError(Throwable exception);
    void onSmallError(Throwable exception);
    //void onError(Throwable exception, boolean writeToJournal);
}
