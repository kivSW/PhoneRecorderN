package phonerecorder.kivsw.com.faithphonerecorder.os;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;

/**
 * Created by ivan on 3/21/18.
 */
@Module
public class ApplicationModule {
    private Context context;
    public ApplicationModule(@NonNull Context context)
    {
        this.context = context;
    };

    @Provides
    @Singleton
    Context provideContext()
    {
        return context;
    }
}
