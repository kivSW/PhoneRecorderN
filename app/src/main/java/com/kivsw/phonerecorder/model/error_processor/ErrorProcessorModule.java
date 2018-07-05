package com.kivsw.phonerecorder.model.error_processor;

import android.content.Context;

import com.kivsw.phonerecorder.model.persistent_data.IJournal;
import com.kivsw.phonerecorder.ui.main_activity.MainActivityContract;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 5/7/18.
 */
@Module
public class ErrorProcessorModule {
    @Provides
    @Singleton
    IErrorProcessor provideErrorProcessor(Context context, IJournal data, MainActivityContract.IMainActivityPresenter mainActivityPresenter)
    {
        IErrorProcessor res= new ErrorProcessor(context, data, mainActivityPresenter);
        data.setErrorProcessor(res);
        return res;
    }

}
