package phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IJournal;

/**
 * Created by ivan on 5/7/18.
 */
@Module
public class ErrorProcessorModule {
    @Provides
    @Singleton
    IErrorProcessor provideErrorProcessor(Context context, IJournal data)
    {
        IErrorProcessor res= new ErrorProcessor(context, data);
        data.setErrorProcessor(res);
        return res;
    }
}
