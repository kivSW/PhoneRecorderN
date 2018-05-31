package phonerecorder.kivsw.com.faithphonerecorder.ui.player;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.AndroidPlayer;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 *
 */
@Module
public class PlayerPresenterModule {
    @Provides
    PlayerPresenter  provideInnerPlayer(ISettings settings, AndroidPlayer androidPlayer, IErrorProcessor errorProcessor)
    {
        return new PlayerPresenter(settings, androidPlayer, errorProcessor);
    }
}
