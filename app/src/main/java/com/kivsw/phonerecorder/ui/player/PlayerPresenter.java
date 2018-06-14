package com.kivsw.phonerecorder.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.kivsw.mvprxdialog.Contract;

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
import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.player.AndroidPlayer;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.model.utils.Utils;

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

    @Inject
    public  PlayerPresenter(ISettings settings, AndroidPlayer androidPlayer, IErrorProcessor errorProcessor)
    {
        super();
        //appContext = context;
        this.settings = settings;
        this.androidPlayer = androidPlayer;
        this.errorProcessor = errorProcessor;

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
                        pause();
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
    public void setUiParam(String callerName, RecordFileNameData recordFileNameData) {
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

    MediaPlayer mplayer;
    protected void startPlaying()
    {
        if(mplayer!=null)
            stop();

        try {
            mplayer = new MediaPlayer();
            mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mplayer.setDataSource(filePath);
            mplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                              @Override
                                              public void onPrepared(MediaPlayer mp) {
                                                  mp.seekTo(currentPos);
                                                  setUiParam();
                                                  mp.start();
                                                  startPositionUpdating();
                                              }
                                          });
            mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    errorProcessor.onError(new Exception("Player error ("+what+")"));
                    return false;
                }
            });
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
            mplayer.prepareAsync();

        }catch(Exception e)
        {
            errorProcessor.onError(e);
            stop();
            deletePresenter();
            if(view!=null)
                view.dismiss();
        }

    };
    @Override
    void stop()
    {
        if(mplayer!=null) {
            mplayer.stop();
            mplayer.reset();
            mplayer.release();
        }
        stopPositionUpdating();
        currentPos=0;
        mplayer=null;
    }

    @Override
    void resumePlaying() {
        if(mplayer!=null) {
            mplayer.start();
            startPositionUpdating();
        }
        else
            startPlaying();
    }

    @Override
    void pause() {
       if(mplayer!=null)
           mplayer.pause();
       stopPositionUpdating();
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
        stop();
    }

    protected void createUI(Context activity)
    {
        FragmentManager fragmentManager=((AppCompatActivity)activity).getSupportFragmentManager();
        long id=getDialogPresenterId();
        PlayerFragment fragment = PlayerFragment.newInstance(id);
        fragment.show(fragmentManager, String.valueOf(id));
    }
    protected void setUiParam()
    {
        if(mplayer==null) return;
        if(view==null) return;

        int duration=mplayer.getDuration();
        if(duration>=0) view.setMaxPosition(duration);

        String label = "<small> "+recordFileNameData.date+" "+recordFileNameData.time+"</small><br>" +
                       "<big><b>"+callerName+"</b></big><br>" +
                      "<small> "+recordFileNameData.phoneNumber+"</small>";


        view.setCaption(label);
        currentPos=mplayer.getCurrentPosition();
        updatePosition(currentPos);
    }
    protected void updatePosition(int pos)
    {
        if(view==null) return;
        view.setPosition(pos, Utils.timeToStr(pos/1000));
    }
    private Subscription updatePositionSubscription;
    protected void startPositionUpdating()
    {
        final int TIME_PERIOD=50;
        /*Observable.interval(TIME_PERIOD,TIME_PERIOD, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        updatePositionSubscription=d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mplayer != null) {
                            currentPos = mplayer.getCurrentPosition();
                            updatePosition(currentPos);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() { }
                });*/

         Observable.interval(TIME_PERIOD,TIME_PERIOD, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .toFlowable(BackpressureStrategy.LATEST)
                .subscribe(new FlowableSubscriber<Long>(){
                    @Override
                    public void onSubscribe(final Subscription s) {
                        s.request(1);
                        updatePositionSubscription =s;
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
    protected void stopPositionUpdating()
    {
        if(updatePositionSubscription ==null)
            return;
        updatePositionSubscription.cancel();
        updatePositionSubscription =null;
    }



}
