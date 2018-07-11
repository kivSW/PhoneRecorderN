package com.kivsw.phonerecorder.model.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ivan on 4/20/18.
 */

public class SimpleFileIO {
    static final long MAX_LENGTH = 10 * 1024;

    public static String readFile(String fileName) {
        try {
            File file = new File(fileName);
            long l = file.length();
            if (l < 0) l = 0;
            if (l > MAX_LENGTH) l = MAX_LENGTH;
            char data[] = new char[(int) l];
            java.io.FileReader reader = new java.io.FileReader(fileName);
            l = reader.read(data, 0, (int) l);
            reader.close();
            if (l < 0) l = 0;

            return new String(data, 0, (int) l);
        } catch (Exception e) {
        }
        return "";
    }

    public static void writeFile(String fileName, String data) throws IOException
    {
        FileWriter writer = new FileWriter(fileName,false);
        writer.append(data);
        writer.close();
    }

    public static String extractFileName(String path)
    {
        int index = path.lastIndexOf(File.separatorChar);
        if(index<0) return path;
        return path.substring(index+1);
    }

}
