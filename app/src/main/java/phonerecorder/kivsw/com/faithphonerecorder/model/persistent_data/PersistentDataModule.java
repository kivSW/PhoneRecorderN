package phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 4/26/18.
 */
@Module
public class PersistentDataModule {
    @Provides
    @Singleton
    IPersistentData provideSettings(Context cntx)
    {
        return new PersistentData(cntx);
    }
}
