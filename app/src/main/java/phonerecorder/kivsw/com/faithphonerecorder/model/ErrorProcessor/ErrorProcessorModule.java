package phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IJournal;
import phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity.MainActivityContract;

/**
 * Created by ivan on 5/7/18.
 */
@Module
public class ErrorProcessorModule {
    @Provides
    @Singleton
    IErrorProcessor provideErrorProcessor(Context context, IJournal data, MainActivityContract.IMainActivityPresenter mainActivityPresenter)
    {
        IErrorProcessor res= new ErrorProcessor( data, mainActivityPresenter);
        data.setErrorProcessor(res);
        return res;
    }
}
