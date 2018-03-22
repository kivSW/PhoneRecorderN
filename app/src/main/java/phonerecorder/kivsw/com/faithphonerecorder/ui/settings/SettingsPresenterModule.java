package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import com.kivsw.cloud.disk.IDiskRepresenter;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 3/21/18.
 */

@Module
public class SettingsPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public SettingsPresenter providePresenter(ISettings settings, List<IDiskRepresenter> diskList)
    {
        return new SettingsPresenter(settings, diskList);
    }

}
