package phonerecorder.kivsw.com.faithphonerecorder.model;

import android.content.Context;

import com.kivsw.cloud.disk.IDiskRepresenter;
import com.kivsw.cloudcache.CloudCache;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Creates cache for files
 */
@Module
public class CloudCacheModule {
    @Singleton
    @Provides
    CloudCache provideCloudCache(Context context, List<IDiskRepresenter> disks, ISettings settings)
    {
        return CloudCache.newInstance(context, context.getExternalCacheDir(), disks, settings.getCacheSize(), settings.getCacheFilesNumber());
    }
}
