package phonerecorder.kivsw.com.faithphonerecorder.model.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class holds data from a call file name.
 */

public class FileNameData {

        public String fileName;
        public String date ="", time="", phoneNumber="", soundSource="",
                phoneId="", extension="";
        public boolean outgoing=false, income=false,
                isSent=false, isProtected=false;

    /** decipher call information from file name
     */
        public static FileNameData decipherFileName(String aFileName)
        {
            int i;

            FileNameData fd= new FileNameData();
            fd.fileName = aFileName;

            // extracts file name
            i=aFileName.lastIndexOf("/");
            if(i>=0)     aFileName = aFileName.substring(i+1);

            try{
                // delete a file extension and "_s" flag
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
                }


                String fileName = aFileName.substring(0, i);

                String[] info=fileName.split("_");
                if(info.length<6)
                {
                   String[] newInfo = new String[6];
                   for(i=0;i<info.length; i++) newInfo[i]=info[i];
                   for(;i<info.length; i++) newInfo[i]="";
                   info = newInfo;
                }


                // sets date and time
                fd.date = info[0].substring(6, 8)+"."+info[0].substring(4, 6)+"."+info[0].substring(0, 4);
                fd.time = info[1].substring(0, 2)+":"+  info[1].substring(2, 4) +":"+  info[1].substring(4, 6);

                // call direction
                if(info[2].compareToIgnoreCase ("outgoing")==0) fd.outgoing=true;
                else if(info[2].compareToIgnoreCase("income")==0) fd.income=true;

                // phone number
                fd.phoneNumber = info[3];

                fd.soundSource = info[4];
                fd.phoneId = info[5];
                fd.isProtected = info[6].equals("1");
            }catch(Exception e)
            {
               fd=null;
            };


            return fd;
        }

        public static FileNameData generateNew(String phoneNumber, boolean income, String soundSource,String extension)
        {

            SimpleDateFormat sdfD = new SimpleDateFormat("dd.MM.yyyy"),
                    sdfT = new SimpleDateFormat("HH:mm:ss");
            Date date= new Date();
            FileNameData fd= new FileNameData();

            fd.date =sdfD.format(date);
            fd.time=sdfT.format(date);
            fd.phoneNumber=phoneNumber;
            fd.soundSource=soundSource;
            fd.phoneId=null;
            fd.extension=extension;
            fd.income = income;
            fd.outgoing = !income;
            fd.isSent=false;

            return fd;
        }

        public String getFileName()
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

            sb.append(phoneNumber);sb.append("_"); //3

            sb.append(soundSource);//4
            sb.append("_");

            if(phoneId!=null && phoneId.length()>0)//5
            {
                sb.append(phoneId);
            };
            sb.append("_");

            sb.append(isProtected?"1":"0");//6


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

}
