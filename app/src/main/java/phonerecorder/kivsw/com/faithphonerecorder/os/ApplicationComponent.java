package phonerecorder.kivsw.com.faithphonerecorder.os;

import javax.inject.Singleton;

import dagger.Component;
import phonerecorder.kivsw.com.faithphonerecorder.model.DiskRepresentativeModule;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.SettingsModule;
import phonerecorder.kivsw.com.faithphonerecorder.ui.settings.SettingsFragment;
import phonerecorder.kivsw.com.faithphonerecorder.ui.settings.SettingsPresenterModule;

/**
 * Created by ivan on 3/21/18.
 */

@Component(modules={SettingsPresenterModule.class, SettingsModule.class, ApplicationModule.class, DiskRepresentativeModule.class})
@Singleton
public interface ApplicationComponent {
    void inject(SettingsFragment fragment);
}
