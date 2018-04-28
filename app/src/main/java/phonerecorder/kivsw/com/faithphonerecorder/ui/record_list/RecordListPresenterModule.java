package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloudcache.CloudCache;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.IPlayer;

/**
 * Created by ivan on 3/27/18.
 */
@Module
public class RecordListPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public RecordListPresenter providePresenter(Context appContext, ISettings settings, IPlayer player, List<IDiskRepresenter> diskList, CloudCache cloudCache )
    {
        return new RecordListPresenter(appContext, settings, player,  diskList, cloudCache );
    }
}
