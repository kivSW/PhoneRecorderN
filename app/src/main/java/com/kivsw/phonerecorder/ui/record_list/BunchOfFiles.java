package com.kivsw.phonerecorder.ui.record_list;

import com.kivsw.cloud.disk.IDiskIO;

import java.util.List;

/**
 * holds path and some files from that path
 */

public class BunchOfFiles {
    String path;
    List<IDiskIO.ResourceInfo> content;
    boolean cache;

    public BunchOfFiles(String path, List<IDiskIO.ResourceInfo> content, boolean cache)
    {
        this.path = path;
        this.content = content;
        this.cache = cache;
    };
}
