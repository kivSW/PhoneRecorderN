package com.kivsw.phonerecorder.model.persistent_data;

import android.content.Intent;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;

/**
 * Created by ivan on 4/26/18.
 */

public interface IJournal {

    void journalAdd(String data);
    void journalAdd(Throwable throwable);
    void journalAdd(Intent intent);

    void setErrorProcessor(IErrorProcessor errorProcessor);

}
