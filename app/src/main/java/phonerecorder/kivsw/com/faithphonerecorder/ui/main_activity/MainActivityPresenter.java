package phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity;

import android.content.Context;

import com.kivsw.mvprxdialog.Contract;

import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;

/**
 * Created by ivan on 5/10/18.
 */

public class MainActivityPresenter implements MainActivityContract.IMainActivityPresenter {

    private Context context;
    private ISettings settings;
    MainActivity view;
    protected MainActivityPresenter(Context context, ISettings settings)
    {
        this.context = context;
        this.settings = settings;
        view=null;
    }

    @Override
    public Contract.IView getUI() {
        return view;
    }

    @Override
    public void setUI(Contract.IView view) {
        if(view instanceof MainActivity)
                this.view = (MainActivity) view;
    }

    @Override
    public void removeUI() {
        this.view = null;
    }

    @Override
    public void showErrorMessage(String msg) {
           if(settings.getHiddenMode() && view==null)
               return;
           MainActivity.showErrorMessage(context, msg);
    }
}
