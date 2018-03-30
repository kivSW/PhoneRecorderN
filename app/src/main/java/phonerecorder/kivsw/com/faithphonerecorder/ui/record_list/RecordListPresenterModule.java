package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import com.kivsw.cloud.disk.IDiskRepresenter;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 3/27/18.
 */
@Module
public class RecordListPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public RecordListPresenter providePresenter(ISettings settings, List<IDiskRepresenter> diskList)
    {
        return new RecordListPresenter(settings, diskList);
    }
}
