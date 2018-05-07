package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 5/4/18.
 */
@Module
public class NotificationShowerModule {
    private static int id=1;
    @Provides
    static public NotificationShower provideDisks(Context context, ISettings settings)
    {
        return new NotificationShower(context, settings, id++);
    }
}
