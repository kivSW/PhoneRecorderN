package com.kivsw.phonerecorder.model.task_executor.tasks;

public interface ITaskProvider {
    final static public String TASK_CALL_RECORDING ="TASK_CALL_RECORDING",
            TASK_SEND_FILES ="TASK_SEND_FILES",
            TASK_SMS_READING="TASK_SMS_READING";

    ITask getTask(String taskId);

}
