package com.kivsw.phonerecorder.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.kivsw.mvprxdialog.Contract;
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.error_processor.InsignificantException;
import com.kivsw.phonerecorder.model.player.AndroidPlayer;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.model.utils.Utils;

import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by ivan on 5/29/18.
 */

public class PlayerPresenter
        extends PlayerContract.IPlayerPresenter


{
    private PlayerContract.IPlayerView view;
    //private Context appContext;
    private ISettings settings;
    private IErrorProcessor errorProcessor;
    private AndroidPlayer androidPlayer;
    private String filePath;
    private String callerName;
    private RecordFileNameData recordFileNameData;
    private int currentPos;
    private boolean isPlaying=false;
    private int trackDuration=0;

    @Inject
    public  PlayerPresenter(ISettings settings, AndroidPlayer androidPlayer, IErrorProcessor errorProcessor)
    {
        super();
        //appContext = context;
        this.settings = settings;
        this.androidPlayer = androidPlayer;
        this.errorProcessor = errorProcessor;
        trackDuration=0;

        registerDialogPresenter();
    }

    @Override
    public Contract.IView getUI() {
        return view;
    }


    @Override
    public void setUI(@NonNull Contract.IView view) {
        this.view = (PlayerContract.IPlayerView)view;
        setUiParam();
        cancelAutoStopTimer();
    }

    @Override
    public void removeUI() {
        view=null;
        initAutoStopTimer();
    }

    private Disposable stopTimerDisposable=null;
    private void initAutoStopTimer()
    {
        Single.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        stopTimerDisposable=d;
                    }

                    @Override
                    public void onSuccess(Long aLong) {
                        pausePlaying();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }
    private void cancelAutoStopTimer()
    {
        if(stopTimerDisposable!=null)
        {
            stopTimerDisposable.dispose();
            stopTimerDisposable=null;
        }
    }


    @Override
    public void setUiLabels(String callerName, RecordFileNameData recordFileNameData) {
        this.callerName = callerName;
        this.recordFileNameData = recordFileNameData;
    }

    @Override
    public void play(Context activity, String filePath) {
        createUI(activity);
        this.filePath = filePath;
        currentPos=0;
        startPlaying();

    }

    @Override
    public void playItemWithChooser(Context activity, String filePath) {
        androidPlayer.playItemWithChooser(activity, filePath);
    }

    private MediaPlayer mplayer;


    protected void startPlaying()
    {
        trackDuration=0;
        if(mplayer!=null)
            stopPlaying();

        try {
            mplayer = new MediaPlayer();
            mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mplayer.setDataSource(filePath);
            mplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                              @Override
                                              public void onPrepared(MediaPlayer mp) {
                                                  trackDuration=mp.getDuration();
                                                  mp.seekTo(currentPos);
                                                  setUiParam();
                                                  mp.start();
                                                  onStartPlaying();
                                              }
                                          });
            mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    errorProcessor.onError(new InsignificantException("Player error ("+what+")"));
                    return false;
                }
            });
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
            mplayer.prepareAsync();

        }catch(Exception e)
        {
            errorProcessor.onError(e);
            stopPlaying();
            deletePresenter();
            if(view!=null)
                view.dismiss();
        }

    };
    @Override
    void stopPlaying()
    {
        if(mplayer!=null) {
            mplayer.stop();
            mplayer.reset();
            mplayer.release();
        }
        onStopPlaying();
        currentPos=0;
        mplayer=null;
    }

    @Override
    void resumePlaying() {
        if(mplayer!=null) {
            mplayer.start();
            onStartPlaying();
        }
        else
            startPlaying();
    }

    @Override
    void pausePlaying() {
       if(mplayer!=null)
           mplayer.pause();
       onStopPlaying();
    }

    @Override
    void setPosition(int pos) {
        if(mplayer!=null) {
            if (pos < 0) pos = 0;
            int duration = mplayer.getDuration();
            if (pos > duration) pos = duration;
            mplayer.seekTo(pos);
        }
        currentPos = pos;
        updatePosition(currentPos);
    }


    protected void deletePresenter()
    {
        super.deletePresenter();
        stopPlaying();
    }

    private final String FRAGMENT_TAG="PlayerFragmentTag";
    protected void createUI(Context activity)
    {
        FragmentManager fragmentManager=((AppCompatActivity)activity).getSupportFragmentManager();
        DialogFragment previousFragment=(DialogFragment)fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if(previousFragment!=null)
            previousFragment.dismiss();

        long id=getDialogPresenterId();
        PlayerFragment fragment = PlayerFragment.newInstance(id);
        fragment.show(fragmentManager, FRAGMENT_TAG);
    }
    protected void setUiParam()
    {
        if(view==null) return;

        String label = "<small> "+recordFileNameData.date+" "+recordFileNameData.time+"</small><br>" +
                       "<big><b>"+callerName+"</b></big><br>" +
                      "<small> "+recordFileNameData.phoneNumber+"</small>";
        view.setCaption(label);

        if (trackDuration >= 0)
            view.setMaxPosition(trackDuration);

        if(mplayer!=null) {
            currentPos = mplayer.getCurrentPosition();
        }
        updatePosition(currentPos);
        setKeepScreenOn();
    }
    protected void updatePosition(int pos)
    {
        if(view==null) return;
        view.setPosition(pos, Utils.timeToStr(pos/1000));
    }
    private Subscription updatePositionSubscription;
    protected void onStartPlaying()
    {
        final int TIME_PERIOD=50;

        isPlaying=true;
        setKeepScreenOn();
        Observable.interval(TIME_PERIOD,TIME_PERIOD, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .toFlowable(BackpressureStrategy.LATEST)
                .subscribe(new FlowableSubscriber<Long>(){
                    @Override
                    public void onSubscribe(final Subscription s) {
                        updatePositionSubscription =s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        updatePositionSubscription.request(1);
                        if(mplayer!=null) {
                            currentPos = mplayer.getCurrentPosition();
                            updatePosition(currentPos);
                        }
                    }

                    @Override public void onError(Throwable e) {}

                    @Override public void onComplete() { }
                });

    }
    protected void onStopPlaying()
    {
        if(updatePositionSubscription ==null)
            return;
        updatePositionSubscription.cancel();
        updatePositionSubscription =null;

        isPlaying=false;
        setKeepScreenOn();
    }

    protected void setKeepScreenOn()
    {
        if(view!=null) view.setKeepScreenOn(isPlaying);
    }



}
