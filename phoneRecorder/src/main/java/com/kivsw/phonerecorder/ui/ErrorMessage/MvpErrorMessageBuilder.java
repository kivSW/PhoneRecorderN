package com.kivsw.phonerecorder.ui.ErrorMessage;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxPresenter;

import java.util.ArrayList;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 6/1/18.
 */

public class MvpErrorMessageBuilder  {

    MvpMessageBoxBuilder messageBoxBuilder;
    Context context;
    ArrayList<MvpMessageBoxPresenter> messageBoxList;
    static final private int MAX_VISIBLE_MESSAGES=1;

    MvpErrorMessageBuilder(Context context)
    {
        this.context = context;
        messageBoxList = new ArrayList<>();
        messageBoxBuilder=MvpMessageBoxBuilder.newInstance();
    };

    public void showMessage(FragmentManager fm, String msg)
    {
        for(int i=0, s=messageBoxList.size()-MAX_VISIBLE_MESSAGES; i<s ;i++) // closes oldest messageBoxes
            messageBoxList.get(i).cancelMessageBox();

        messageBoxBuilder.setText(context.getResources().getText(R.string.error).toString(), msg);
        final MvpMessageBoxPresenter presenter = messageBoxBuilder.build(fm);
        messageBoxList.add(presenter);
        presenter.getSingle().subscribe(new SingleObserver<Integer>() {
            @Override public void onSubscribe(Disposable d) {}

            @Override public void onSuccess(Integer integer) {
                messageBoxList.remove(presenter);
            }

            @Override
            public void onError(Throwable e) {
                messageBoxList.remove(presenter);
            }
        });
    }





}
