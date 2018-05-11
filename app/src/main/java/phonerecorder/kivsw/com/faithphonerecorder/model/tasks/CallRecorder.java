package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor.IErrorProcessor;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.ICallInfoKeeper;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.SoundSource;
import phonerecorder.kivsw.com.faithphonerecorder.model.task_executor.TaskExecutor;
import phonerecorder.kivsw.com.faithphonerecorder.model.utils.RecordFileNameData;
import phonerecorder.kivsw.com.faithphonerecorder.ui.notification.NotificationShower;

/**
 * Created by ivan on 4/26/18.
 */

public class CallRecorder implements ITask {

    private Context context;
    private ISettings settings;
    private ICallInfoKeeper callInfoKeeper;
    private IErrorProcessor errorProcessor;
    private TaskExecutor taskExecutor;
    private NotificationShower notification;
    private MediaRecorder recorder = null;
    private String tempFileName, recordFileName;

    @Inject
    public CallRecorder(Context context, ISettings settings, ICallInfoKeeper callInfoKeeper, TaskExecutor taskExecutor, NotificationShower notification, IErrorProcessor errorProcessor) {
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
            stopRecording();
            newTask=false;
        }
        startRecording();

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
        recordFileName = createFileName();

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
        recordFileName=null;

    }

    protected boolean isRecording()
	{
		return recorder!=null;
	}

    protected String createFileName()
    {
        ICallInfoKeeper.CallInfo callInfo=callInfoKeeper.getCallInfo();
        return  settings.getInternalTempPath() +
                RecordFileNameData.generateNew(callInfo.number, callInfo.isIncome, soundSourceToStr(), getExtension())
                .buildFileName();

    };

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


}
