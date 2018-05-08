package phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor;

/**
 * Created by ivan on 5/7/18.
 */

public interface IErrorProcessor {
    void onError(Throwable exception);
    void onError(Throwable exception, boolean writeToJournal);
}
