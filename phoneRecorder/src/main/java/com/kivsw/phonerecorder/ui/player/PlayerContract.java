package com.kivsw.phonerecorder.ui.player;

import com.kivsw.mvprxdialog.BaseMvpPresenter;

import com.kivsw.phonerecorder.model.player.IPlayer;

/**
 * Created by ivan on 5/29/18.
 */

public class PlayerContract {
       abstract static class IPlayerPresenter  extends BaseMvpPresenter
                implements IPlayer
       {
           abstract void stopPlaying();
           abstract void resumePlaying();
           abstract void pausePlaying();
           abstract void setPosition(int pos);
       }
       interface IPlayerView extends com.kivsw.mvprxdialog.Contract.IView
       {
           void setMaxPosition(int max);
           void setPosition(int pos, String label);
           void setCaption(String label);
           void setKeepScreenOn(boolean v);
           void dismiss();
       }


}
