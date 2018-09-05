package com.kivsw.phonerecorder.model.addrbook;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import javax.inject.Inject;

public class PhoneAddrBook implements IAddrBook {

    Context appContext;

    @Inject
    PhoneAddrBook(Context context)
    {
        appContext = context;
    }

    @Override
    public String getNameFromPhone(String phoneNumber) {
        String res = null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            ContentResolver resolver = appContext.getContentResolver();
            Cursor cur = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (cur != null) {
                if (cur.moveToFirst())
                    res = cur.getString(1);

                if (!cur.isClosed()) cur.close();
            }
        }catch(Exception e)
        {
            e.toString();
        }
        if (res == null) res = "";
        return res;
    }
}
