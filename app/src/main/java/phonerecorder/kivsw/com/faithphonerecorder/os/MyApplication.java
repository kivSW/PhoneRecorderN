package phonerecorder.kivsw.com.faithphonerecorder.os;


/**
 * Created by ivan on 3/20/18.
 */

public class MyApplication extends android.app.Application {
    protected static ApplicationComponent applicationComponent;

    public MyApplication()
    {
        super();
        applicationComponent =DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }



    public static ApplicationComponent getComponent()
    {
        return applicationComponent;
    };
}
