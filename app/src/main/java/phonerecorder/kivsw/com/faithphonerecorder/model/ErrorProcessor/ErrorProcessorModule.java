package phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;

/**
 * Created by ivan on 5/7/18.
 */
@Module
public class ErrorProcessorModule {
    @Provides
    @Singleton
    IErrorProcessor provideErrorProcessor(Context context, IPersistentData data)
    {
        return new ErrorProcessor(context, data);
    }
}
