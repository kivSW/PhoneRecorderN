package phonerecorder.kivsw.com.faithphonerecorder.model.error_processor;

/**
 * Created by ivan on 5/7/18.
 */

public interface IErrorProcessor {
    void onError(Throwable exception);
    void onError(Throwable exception, boolean writeToJournal);
}
