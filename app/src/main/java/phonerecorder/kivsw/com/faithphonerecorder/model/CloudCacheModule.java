package phonerecorder.kivsw.com.faithphonerecorder.model;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;

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
    CloudCache provideCloudCache(Context context, DiskContainer disks, ISettings settings)
    {
        return CloudCache.newInstance(context, context.getExternalCacheDir(), disks.getDiskList(), settings.getCacheSize(), settings.getCacheFilesNumber());
    }
}
