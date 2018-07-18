package com.kivsw.phonerecorder.model.task_executor.tasks;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.SystemClock;

import com.kivsw.phonerecorder.model.error_processor.IErrorProcessor;
import com.kivsw.phonerecorder.model.persistent_data.IPersistentDataKeeper;
import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.types.SoundSource;
import com.kivsw.phonerecorder.model.task_executor.ITaskExecutor;
import com.kivsw.phonerecorder.model.utils.RecordFileNameData;
import com.kivsw.phonerecorder.ui.notification.NotificationShower;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 4/26/18.
 */

public class CallRecorder implements ITask {

    private Context context;
    private ISettings settings;
    private IPersistentDataKeeper callInfoKeeper;
    private IErrorProcessor errorProcessor;
    private ITaskExecutor taskExecutor;
    private NotificationShower notification;
    private MediaRecorder recorder = null;
    private String tempFileName;

    private long startTime;
    private RecordFileNameData recordFileNameData;

    @Inject
    public CallRecorder(Context context, ISettings settings, IPersistentDataKeeper callInfoKeeper, ITaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor) {
        this.context = context;
        this.settings = settings;
        this.callInfoKeeper = callInfoKeeper;
        this.taskExecutor = taskExecutor;
        this.notification = notification;
        this.errorProcessor = errorProcessor;
    }

    @Override
    protected void finalize() throws Throwable {
        stopRecording();
        super.finalize();
    }

    @Override
    public boolean startTask() {
        boolean newTask=true;
        notification.show(context.getText(R.string.recording_call).toString());
        if(isRecording()) {
            newTask=false;
        }
        else {
            startRecording();
        }

        return newTask;
    }

    @Override
    public void stopTask() {
        notification.hide();
        stopRecording();
        taskExecutor.startFileSending();
    }

    private int getAudioSource()
    {
        SoundSource soundSource=settings.getSoundSource();
        switch(soundSource)
        {
            case VOICE_CALL:
                return MediaRecorder.AudioSource.VOICE_CALL;
            case VOICE_COMMUNICATION:
                return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        };
        return MediaRecorder.AudioSource.MIC;

    }
    protected boolean startRecording()
    {
        if(isRecording()) return true;
        tempFileName = settings.getInternalTempPath() + "temp~";
        createRecordFileName();

        try{

            recorder = new MediaRecorder();
            recorder.reset();
            recorder.setAudioSource(getAudioSource());

            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(tempFileName);

            recorder.prepare();
            recorder.start();   // Recording is now started
        }catch(Exception e)
        {
            errorProcessor.onError(e);
            stopRecording();
        }

        return isRecording();
    };

    protected void stopRecording()
    {
        if(recorder==null) return;

        String recordFileName = generateRecordFileName();
        try{
            recorder.stop();
            File file=new File(tempFileName);
            file.renameTo(new File(recordFileName));
        }catch(Exception e)
        {
            errorProcessor.onError(e);
        };

        recorder.release();
        recorder=null;
        recordFileNameData=null;

        onRecordObservable.onNext("");
    }

    protected boolean isRecording()
	{
		return recorder!=null;
	}

    protected void createRecordFileName()
    {
        IPersistentDataKeeper.CallInfo callInfo=callInfoKeeper.getCallInfo();
        recordFileNameData = RecordFileNameData.generateNew(callInfo.number, callInfo.isIncome, soundSourceToStr(), getExtension());
        startTime = SystemClock.elapsedRealtime();
    };
    protected String generateRecordFileName()
    {
        recordFileNameData.duration = (int)((SystemClock.elapsedRealtime()-startTime+500L)/1000L);
        return settings.getInternalTempPath() + recordFileNameData.buildFileName();
    }

    static HashMap<Integer,String> callSources;
    {
        callSources = new HashMap<Integer, String>(5);
        callSources.put(MediaRecorder.AudioSource.VOICE_CALL, "VOICE-CALL");
        callSources.put(MediaRecorder.AudioSource.MIC, "MIC");
        callSources.put(MediaRecorder.AudioSource.VOICE_COMMUNICATION, "VOICE-COMMUNICATION");
        callSources.put(MediaRecorder.AudioSource.VOICE_UPLINK, "VOICE-UPLINK");
        callSources.put(MediaRecorder.AudioSource.VOICE_DOWNLINK, "VOICE-DOWNLINK");
    }

    protected String soundSourceToStr()
    {
        String res = callSources.get( getAudioSource() );
        if(res == null)
            res="";
        return res;
    };

    protected String getExtension()
    {
        if(settings.getUseFileExtension())
            return ".3gp";
        return ".dat";
    }

    Subject<Object> onRecordObservable = PublishSubject.create();
    /**
     * emitts on start and stop recording
     * @return
     */
    public Observable<Object> getOnRecordObservable()
    {
        return onRecordObservable;
    }


}
