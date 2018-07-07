package com.kivsw.phonerecorder.ui.player;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kivsw.mvprxdialog.BaseMvpFragment;

import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 5/29/18.
 */

public class PlayerFragment extends BaseMvpFragment
implements PlayerContract.IPlayerView {

    static PlayerFragment newInstance(long presenterId)
    {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();

        args.putLong(PRESENTER_ID, presenterId);

        fragment.setArguments(args);
        return fragment;
    };

    TextView textViewCaption,
             textViewPosition;
    ImageView imageViewPlay, imageViewPause;//, imageViewStop;
    ProgressBar progressBar;

    protected PlayerContract.IPlayerPresenter getPresenter()
    {
        return (PlayerContract.IPlayerPresenter )super.getPresenter();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.player_fragment, container, false);

        textViewCaption = (TextView)rootView.findViewById(R.id.textViewCaption);
        textViewPosition = (TextView)rootView.findViewById(R.id.textViewPosition);
        imageViewPlay = (ImageView)rootView.findViewById(R.id.imageViewPlay);
        imageViewPause = (ImageView)rootView.findViewById(R.id.imageViewPause);
        //imageViewStop = (ImageView)rootView.findViewById(R.id.imageViewStop);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);

        initViews();

        return rootView;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width =  WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    };

    private void initViews()
    {
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().resumePlaying();
            }
        });
        imageViewPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().pause();
            }
        });

        View.OnTouchListener btnListener=new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action=event.getAction();
                switch(action) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundResource(R.drawable.shape_gray_rectangle);
                        break;

                    case MotionEvent.ACTION_UP:
                        if(event.getPointerCount()==1)
                           v.performClick();
                    case MotionEvent.ACTION_CANCEL:
                        v.setBackgroundDrawable(null);
                        break;

                }
                return true;
            }
        };
        imageViewPlay.setOnTouchListener(btnListener);
        imageViewPause.setOnTouchListener(btnListener);

        progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    event.getX();
                    int offset=(int)(event.getX()+0.5) - progressBar.getLeft();

                    int position = offset*progressBar.getMax()/progressBar.getWidth();
                    getPresenter().setPosition(position);
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public void setMaxPosition(int max) {
        progressBar.setProgress(0);
        progressBar.setMax(max);
    }

    @Override
    public void setPosition(int pos, String label) {
        textViewPosition.setText(label);
        progressBar.setProgress(pos);
    }

    @Override
    public void setCaption(String label) {
       textViewCaption.setText(Html.fromHtml(label));
    }

}
