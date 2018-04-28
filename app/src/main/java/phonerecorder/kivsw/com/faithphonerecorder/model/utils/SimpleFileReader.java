package phonerecorder.kivsw.com.faithphonerecorder.model.utils;

import java.io.File;

/**
 * Created by ivan on 4/20/18.
 */

public class SimpleFileReader {
    static final long MAX_LENGTH=10*1024;
    public static String readFile(String fileName)
    {
        try {
            File file = new File(fileName);
            long l = file.length();
            if (l < 0) l = 0;
            if (l > MAX_LENGTH) l=MAX_LENGTH;
            char data[] = new char[(int) l];
            java.io.FileReader reader = new java.io.FileReader(fileName);
            l = reader.read(data, 0, (int)l);
            reader.close();
            if (l < 0) l = 0;

            return new String(data, 0, (int)l);
        }catch(Exception e){}
        return "";
    }
}
