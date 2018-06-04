package phonerecorder.kivsw.com.faithphonerecorder.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.kivsw.mvprxdialog.Contract;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import phonerecorder.kivsw.com.faithphonerecorder.model.error_processor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.player.AndroidPlayer;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.Utils;

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
    }

    @Override
    public void removeUI() {
        view=null;
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
        mplayer=null;
    }

    @Override
    void resumePlaying() {
        if(mplayer!=null) {
            mplayer.start();
            startPositionUpdating();
        }
    }

    @Override
    void pause() {
       if(mplayer!=null)
           mplayer.pause();
       stopPositionUpdating();
    }

    @Override
    void setPosition(int pos) {
        if(mplayer==null) return;

        if(pos<0) pos=0;
        int duration = mplayer.getDuration();
        if(pos>duration) pos=duration;
        mplayer.seekTo(pos);
        updatePosition();
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
        updatePosition();
    }
    protected void updatePosition()
    {
        if(view==null) return;
        int pos=mplayer.getCurrentPosition();
        view.setPosition(pos, Utils.durationToStr(pos/1000));
    }
    private Disposable updatePositionDisposable;
    protected void startPositionUpdating()
    {
        Observable.interval(250,250, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        updatePositionDisposable=d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        updatePosition();
                    }

                    @Override public void onError(Throwable e) {}

                    @Override public void onComplete() { }
                });

    }
    protected void stopPositionUpdating()
    {
        if(updatePositionDisposable==null)
            return;
        updatePositionDisposable.dispose();
        updatePositionDisposable=null;
    }



}
