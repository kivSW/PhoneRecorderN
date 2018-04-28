package phonerecorder.kivsw.com.faithphonerecorder.model.player;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ivan on 4/19/18.
 */
@Module
public class AndroidPlayerModule {
    @Singleton
    @Provides
    IPlayer providePlayer()
    {
        return new AndroidPlayer();
    }
}
