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
           abstract void stop();
           abstract void resumePlaying();
           abstract void pause();
           abstract void setPosition(int pos);
       }
       interface IPlayerView extends com.kivsw.mvprxdialog.Contract.IView
       {
           void setMaxPosition(int max);
           void setPosition(int pos, String label);
           void setCaption(String label);
           void dismiss();
       }


}
