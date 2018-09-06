package com.kivsw.phonerecorder.model.addrbook;

import com.google.gson.Gson;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.utils.SimpleFileIO;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileAddrBook implements IAddrBook {
    private IErrorProcessor errorProcessor;
    private Map<String, String> addrBookMap;
    private boolean modified;
    private String fileName;
    private PhoneAddrBook phoneAddrBook;

    public static final String DEFAULT_FILE_NAME="addrbook";


    protected class AddrBookItem
    {
        String name, phoneNum;
        public AddrBookItem(){};
        public AddrBookItem(String name, String phoneNum)
        {
            this.name = name;
            this.phoneNum = phoneNum;
        };
    }


    public FileAddrBook(String fileName, IErrorProcessor errorProcessor)
    {
        this(fileName, null, errorProcessor);
    }

    public FileAddrBook(PhoneAddrBook phoneAddrBook, IErrorProcessor errorProcessor)
    {
        this.errorProcessor = errorProcessor;
        this.phoneAddrBook = phoneAddrBook;
        addrBookMap = new ConcurrentHashMap<>();
    }

    public FileAddrBook(String fileName, PhoneAddrBook phoneAddrBook, IErrorProcessor errorProcessor)
    {
        this(phoneAddrBook, errorProcessor);
        load(fileName);
    }

    synchronized public void load(String fileName)
    {
            String filedata;

            this.fileName = fileName;
            synchronized (this) {
                filedata = SimpleFileIO.readFile(fileName);
            }

            //Set<String> sentFiles = Collections.newSetFromMap(new ConcurrentHashMap());
            Map<String, String> addrBookMap = new ConcurrentHashMap();

            try {
                Gson gson = new Gson();
                AddrBookItem[] addrBookList = gson.fromJson(filedata, AddrBookItem[].class);

                for (Object item : addrBookList) {
                        addrBookMap.put( ((AddrBookItem)item).phoneNum, ((AddrBookItem)item).name);
                }
            }catch(Exception e){
                e.toString();
            };

            synchronized (this) {
                this.addrBookMap = addrBookMap;
                modified = false;
            }

    };
    public void save() throws Exception
    {
        saveTo(fileName);
    }
    public void saveTo(String fileName) throws Exception
    {
        if(!modified) return;

        Iterator<Map.Entry<String, String>> iterator=   addrBookMap.entrySet().iterator();

        AddrBookItem items[]=new AddrBookItem[addrBookMap.size()];

        for(int i=0; iterator.hasNext(); i++)
        {
            Map.Entry<String, String> entry=iterator.next();
            items[i]=new AddrBookItem( entry.getValue(), entry.getKey());
        }

        Gson gson = new Gson();
        String data=gson.toJson(items);
        synchronized (this) {
            try {
                SimpleFileIO.writeFile(fileName, data);

            } catch (Exception e) {
                if(errorProcessor!=null)
                    errorProcessor.onError(e);
                else
                    throw e;
            }

            modified = false;
        }
    };
    public void addItem(String name, String phoneNumber)
    {
        if(name==null || name.isEmpty())
            return;
        if(phoneNumber==null)
            return;

        phoneNumber = normalizePhoneNumber(phoneNumber);
        if(phoneNumber.isEmpty())
            return;

        try {
            String currentName = addrBookMap.get(phoneNumber);
            if (currentName != null && currentName.equals(name))
                return;

            addrBookMap.put(phoneNumber, name);
            modified = true;
        }catch (Exception e)
        {
            errorProcessor.onError(e);
        }
    };

    public void addItem(String phoneNumber)
    {
        if(phoneAddrBook!=null) {
            String name=phoneAddrBook.getNameFromPhone(phoneNumber);
                addItem(name, phoneNumber);
        }
    };
    @Override
    public String getNameFromPhone(String phoneNumber) {

        String res = addrBookMap.get(normalizePhoneNumber(phoneNumber));

        if( (res == null || res.isEmpty()) && (phoneAddrBook!=null))
            res = phoneAddrBook.getNameFromPhone(phoneNumber);

        return res;
    };
    protected String normalizePhoneNumber(String phoneNumber)
    {
        String res = phoneNumber.replaceAll ("\\D", "");
        if(res.length()>10)
            res = res.substring(res.length()-10);

        return res;

    }

    public String getFullFileName()
    {
        return fileName;
    };
    public String getFileName()
    {
        int i=fileName.lastIndexOf(File.separatorChar);
        if(i<0) return fileName;
        return fileName.substring(i+1);
    };
}
