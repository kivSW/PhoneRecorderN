package com.kivsw.phonerecorder.model.settings;

/**
 * Created by ivan on 5/11/18.
 */

public class AntiTaskKillerNotificationParam {
    public boolean visible;
    //public String text;
    public int iconNum;
    public AntiTaskKillerNotificationParam(boolean visible, int iconNum)
    {
        this.visible=visible;
        //this.text=text;
        this.iconNum=iconNum;
    }
    @Override
    public  boolean equals(Object o)
    {
        if(!(o instanceof AntiTaskKillerNotificationParam) )
            return false;
        AntiTaskKillerNotificationParam other=(AntiTaskKillerNotificationParam)o;
        return  visible==other.visible  &&
                //text.equals(other.text) &&
                iconNum == other.iconNum;
    }
}
