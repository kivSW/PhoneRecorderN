package com.kivsw.phonerecorder.model;

import android.content.Context;

import com.kivsw.cloud.DiskContainer;
import com.kivsw.cloudcache.CloudCache;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.kivsw.phonerecorder.model.settings.ISettings;

/**
 * Creates cache for files
 */
@Module
public class CloudCacheModule {
    @Singleton
    @Provides
    CloudCache provideCloudCache(Context context, DiskContainer disks, ISettings settings)
    {
        return CloudCache.newInstance(context, context.getExternalCacheDir(), disks, settings.getCacheSize(), settings.getCacheFilesNumber());
    }
}
