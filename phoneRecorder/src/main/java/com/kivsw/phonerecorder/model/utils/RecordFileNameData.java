package com.kivsw.phonerecorder.model.utils;

import android.support.annotation.NonNull;

import org.apache.commons.lang.StringEscapeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class holds data from a call file name.
 */

public class RecordFileNameData implements Comparable{

        public final static String RECORD_PATTERN ="^[0-9]{8}_[0-9]{6}_";

        public String origFileName;
        public int duration;
        public String date ="", time="", phoneNumber="", soundSource="",
                phoneId="", extension="";
        public boolean outgoing=false, income=false,
                isSent=false, isProtected=false,
                isSMS=false;

    /** decipher call information from file name
     */
        public static RecordFileNameData decipherFileName(String aFileName)
        {
            int i;

            RecordFileNameData fd= new RecordFileNameData();
            fd.origFileName = aFileName;

            // extracts file name
            i=aFileName.lastIndexOf("/");
            if(i>=0)     aFileName = aFileName.substring(i+1);

            try{
                // deleteRecord a file extension and "_s" flag
                i=aFileName.lastIndexOf("_s.");

                if(i==-1)
                {
                    i=aFileName.lastIndexOf(".");
                    if(i>0) fd.extension=aFileName.substring(i+1,aFileName.length());
                    else i=0;
                }
                else
                {
                    fd.isSent = true;
                    fd.extension=aFileName.substring(i+3,aFileName.length());
                };

                String fileName = aFileName.substring(0, i);

                String[] info=fileName.split("_");
                if(info.length<8)
                {
                   String[] newInfo = new String[8];
                   for(i=0;i<info.length; i++) newInfo[i]=info[i];
                   for(;i<info.length; i++) newInfo[i]="0";
                   info = newInfo;
                }

                fd.isSMS = fd.extension.toLowerCase().equals("sms");

                // sets date and time
                fd.date = info[0].substring(6, 8)+"."+info[0].substring(4, 6)+"."+info[0].substring(0, 4);
                fd.time = info[1].substring(0, 2)+":"+  info[1].substring(2, 4) +":"+  info[1].substring(4, 6);

                // call direction
                if(info[2].compareToIgnoreCase ("outgoing")==0) fd.outgoing=true;
                else if(info[2].compareToIgnoreCase("income")==0) fd.income=true;

                // phone number
                fd.phoneNumber = decodeStr(info[3]);

                fd.soundSource = info[4];
                fd.phoneId = info[5];
                fd.isProtected = info[6].equals("1");
                fd.duration = 0;
                fd.duration = Integer.parseInt(info[7]);
                fd.hashCode();

            }catch(Exception e)
            {

            };

            return fd;
        }

        public static RecordFileNameData generateNew(String phoneNumber, boolean income, String soundSource, String extension)
        {
            return generateNew(new Date(), phoneNumber, income, soundSource, extension);
        }
        public static RecordFileNameData generateNew(Date date, String phoneNumber, boolean income, String soundSource, String extension)
        {

            SimpleDateFormat sdfD = new SimpleDateFormat("dd.MM.yyyy"),
                    sdfT = new SimpleDateFormat("HH:mm:ss");

            RecordFileNameData fd= new RecordFileNameData();

            fd.date =sdfD.format(date);
            fd.time=sdfT.format(date);
            fd.phoneNumber=phoneNumber;
            fd.soundSource=soundSource;
            fd.phoneId=null;
            fd.extension=extension;
            fd.income = income;
            fd.outgoing = !income;
            fd.isSent=false;
            fd.duration = 0;

            return fd;
        }

        private  String correctStr(String str)
        {
           char ch;
           ch='_';
           String res= str;
           //res = res.replaceAll("_", String.format("\\\\u%04X", (int)ch));
           res = res.replaceAll("[\\x00-\\x1F_\\:\\/\\\\]", "");
           return res;
        }
        private static String decodeStr(String str)
        {
            return StringEscapeUtils.unescapeJava(str);
        }

        public String buildFileName()
        {
            StringBuilder sb=new StringBuilder();
		  /*sb.append(path);

	        if(path.length()==0 || path.charAt(path.length()-1)!='/')
	        	sb.append('/');*/

            String str;
            // 0
            str = date.substring(6, 10) + date.substring(3, 5) + date.substring(0, 2);
            sb.append(str);sb.append("_");
            //1
            str = time.replaceAll(":", "");
            sb.append(str);sb.append("_");

            if(outgoing) sb.append("outgoing_");//2
            else sb.append("income_");

            sb.append(correctStr(phoneNumber));sb.append("_"); //3

            sb.append(soundSource);//4
            sb.append("_");

            if(phoneId!=null && phoneId.length()>0)//5
            {
                sb.append(phoneId);
            };
            sb.append("_");

            sb.append(isProtected?"1":"0");//6
            sb.append("_");

            sb.append(String.valueOf(duration));//7

            if(isSent) sb.append("_s"); // should be before the file extension

            if(extension!=null && extension.length()>0)
            {
                if(extension.charAt(0)!='.')	sb.append('.');
                sb.append(extension);
            };

			/*if(showFileExtension) sb.append(".3gp");
			else sb.append(".dat");*/

            return sb.toString();
        }


    @Override
    public int compareTo(@NonNull Object o) {
        if(!(o instanceof RecordFileNameData))
                return Integer.MIN_VALUE;

        RecordFileNameData other=(RecordFileNameData)o;

        int r=0;
        r=date.compareTo(other.date);
        if(r!=0) return r;

        r=time.compareTo(other.time);
        if(r!=0) return r;

        r=phoneNumber.compareTo(other.phoneNumber);
        if(r!=0) return r;

        r=Boolean.valueOf(isSMS).compareTo(other.isSMS);
        if(r!=0) return r;

        return r;
    }
    @Override
    public boolean equals(@NonNull Object o)
    {
        return 0==compareTo(o);
    }
    @Override
    public int 	hashCode()
    {
        int hash=0, m=29;
        if(date!=null)
            hash=hash*m+date.hashCode();
        if(time!=null)
            hash=hash*m+time.hashCode();
        if(phoneNumber!=null)
            hash=hash*m+phoneNumber.hashCode();

        hash*=m;
        if(isSMS) hash++;

        return hash;
    }
}
