package phonerecorder.kivsw.com.faithphonerecorder.model.ErrorProcessor;

import android.content.Context;

import java.util.List;

import io.reactivex.exceptions.CompositeException;
import phonerecorder.kivsw.com.faithphonerecorder.model.persistent_data.IPersistentData;
import phonerecorder.kivsw.com.faithphonerecorder.ui.MainActivity;

/**
 * Created by ivan on 5/7/18.
 */

public class ErrorProcessor implements IErrorProcessor {
    Context context;
    IPersistentData persistentData;

    ErrorProcessor(Context context, IPersistentData persistentData)
    {
        this.context = context;
        this.persistentData = persistentData;
    }

    @Override
    public void onError(Throwable exception)
    {
        if(exception instanceof CompositeException)
        {
            List<Throwable> exceptions=((CompositeException)exception).getExceptions();
            for(Throwable t:exceptions)
                onError(t);
            return;
        }

        persistentData.journalAdd(exception);
        MainActivity.showErrorMessage(context, exception.getMessage());
    };
}
