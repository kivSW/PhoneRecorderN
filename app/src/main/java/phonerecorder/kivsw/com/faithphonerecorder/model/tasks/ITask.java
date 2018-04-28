package phonerecorder.kivsw.com.faithphonerecorder.model.tasks;

/**
 * class represents a phone task
 * it MUST be executed vie TaskExecutor class
 */

public interface ITask {
    void startTask();
    void stopTask();
}
