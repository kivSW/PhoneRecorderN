package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.annotations.NonNull;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.tasks.RecordSender;

/**
 * Created by ivan on 3/27/18.
 */
@Module
public class RecordListPresenterModule {
    @Provides
    @NonNull
    @Singleton
    public RecordListContract.IRecordListPresenter providePresenter(Context appContext, ISettings settings, DiskContainer disks, CloudCache cloudCache, IErrorProcessor errorProcessor, RecordSender recordSender)
    {
        return new RecordListPresenter(appContext, settings, disks, cloudCache,  errorProcessor, recordSender);
    }
}
