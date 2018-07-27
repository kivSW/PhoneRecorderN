package com.kivsw.phonerecorder.ui.player;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.player.AndroidPlayer;
import com.kivsw.phonerecorder.model.settings.ISettings;

import dagger.Module;
import dagger.Provides;

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
