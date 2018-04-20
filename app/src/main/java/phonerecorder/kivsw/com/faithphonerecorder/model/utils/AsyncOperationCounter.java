package phonerecorder.kivsw.com.faithphonerecorder.model.utils;

/**
 * Created by ivan on 4/16/18.
 */

public class AsyncOperationCounter
{
    private int count=0;
    private boolean finished=false;
    public boolean isFinished(){return finished && (count<=0);}
    public void finish(){finished=true;};
    public void inc(){count++;};
    public void dec(){count--;};
}
