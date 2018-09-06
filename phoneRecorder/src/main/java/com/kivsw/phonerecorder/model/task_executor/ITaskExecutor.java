package com.kivsw.phonerecorder.model.task_executor;

/**
 * Created by ivan on 5/29/18.
 */

public interface ITaskExecutor {
    void startCallRecording();
    void stopCallRecording();

    void startFileSending();
    void stopFileSending();

    void startSMSreading();
    void stopSMSreading();

    void startAddrBookReading();
    void stopAddrBookReading();
}
