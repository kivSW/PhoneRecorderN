package phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity;

/**
 * Created by ivan on 5/10/18.
 */

public interface MainActivityContract {
    interface IMainActivityPresenter  extends com.kivsw.mvprxdialog.Contract.IPresenter
    {
        void showErrorMessage(String msg);
        void showActivity();
    }


}
