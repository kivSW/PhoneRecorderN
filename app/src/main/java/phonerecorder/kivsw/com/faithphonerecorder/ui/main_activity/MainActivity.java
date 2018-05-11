package phonerecorder.kivsw.com.faithphonerecorder.ui.main_activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kivsw.mvprxdialog.Contract;
import com.kivsw.mvprxdialog.messagebox.MvpMessageBoxBuilder;
import com.tbruyelle.rxpermissions2.RxPermissions;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.os.MyApplication;
import phonerecorder.kivsw.com.faithphonerecorder.ui.record_list.RecordListFragment;
import phonerecorder.kivsw.com.faithphonerecorder.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity
    implements Contract.IView
{
    private ViewPager pager;
    private final int SETTINGS_PAGE=1, REC_LIST_PAGE=0;
    protected static final String ACTION_SHOW_ERROR_MESSAGE ="MainActivity.ACTION_SHOW_ERROR_MESSAGE",
                                  MESSAGE="MESSAGE";
    Intent intentToBeProcessed;

    @Inject
    MainActivityContract.IMainActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.drawable.ear);
        toolbar.setLogoDescription(R.string.app_name);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);*/

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new OnChangePage());

        MyApplication.getComponent().inject(this);

        askForPermission();
        intentToBeProcessed = getIntent ();

    }
    @Override
    public void onNewIntent (Intent intent)
    {
        intentToBeProcessed = intent;

    };

    protected void processIntent()
    {
        if(intentToBeProcessed==null) return;

        if(intentToBeProcessed.getAction().equals(ACTION_SHOW_ERROR_MESSAGE)) {
            String msg = intentToBeProcessed.getStringExtra(MESSAGE);
            if (msg != null) {
                MvpMessageBoxBuilder.newInstance()
                        .setText(getText(R.string.error).toString(), msg)
                        .build(getSupportFragmentManager());
            }
        }

        intentToBeProcessed=null;
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        presenter.setUI(this);
    };
    @Override
    protected void onStop()
    {
        super.onStop();
        presenter.removeUI();
    };
    @Override
    protected void onResume()
    {
        super.onResume();
        processIntent();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;

    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        int currentPage=pager.getCurrentItem();
        MenuItem settingItem = menu.findItem(R.id.action_settings),
                 recListItem = menu.findItem(R.id.action_rec_list);

        settingItem.setVisible(currentPage!=SETTINGS_PAGE);
        recListItem.setVisible(currentPage!=REC_LIST_PAGE);


        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.action_rec_list:
                pager.setCurrentItem(REC_LIST_PAGE);
                return true;
            case R.id.action_settings:
                pager.setCurrentItem(SETTINGS_PAGE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * read more: https://github.com/tbruyelle/RxPermissions
     */
    protected void askForPermission()
    {
        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
        rxPermissions
                .request(Manifest.permission.READ_PHONE_STATE,
                         Manifest.permission.INTERNET,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE,
                         Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.READ_CONTACTS,
                         Manifest.permission.RECEIVE_BOOT_COMPLETED,
                         Manifest.permission.RECORD_AUDIO,
                         Manifest.permission.PROCESS_OUTGOING_CALLS
                        )
                .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean granted) throws Exception {
                            if (granted) { // Always true pre-M
                                // All requested permissions are granted
                            } else {
                                // Oups permission denied
                            }
                        }
                    });
    };

    protected class PagerAdapter extends FragmentPagerAdapter
    {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case SETTINGS_PAGE:
                    return new SettingsFragment();
                case REC_LIST_PAGE:
                    return new RecordListFragment();

            }
            return null;
        }
    }

    protected class  OnChangePage implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
             invalidateOptionsMenu();
        }
    }

    //---------------------------------------------------------------------------------
    public static void showActivity(Context context)
    {
        Intent i=new Intent(Intent.ACTION_VIEW, null, context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
    public static void showErrorMessage(Context context, String message)
    {
        Intent i=new Intent(ACTION_SHOW_ERROR_MESSAGE, null, context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        i.putExtra(MESSAGE, message);
        context.startActivity(i);
    };

}
