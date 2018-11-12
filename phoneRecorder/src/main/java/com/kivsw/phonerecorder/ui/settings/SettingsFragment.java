package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.model.settings.types.AntiTaskKillerNotificationParam;
import com.kivsw.phonerecorder.model.settings.types.DataSize;
import com.kivsw.phonerecorder.model.settings.types.SoundSource;
import com.kivsw.phonerecorder.os.MyApplication;
import com.kivsw.phonerecorder.ui.notification.AntiTaskKillerNotification;

import javax.inject.Inject;

import phonerecorder.kivsw.com.phonerecorder.R;

/**

 */
public class SettingsFragment extends Fragment
    implements SettingsContract.ISettingsView {

    @Inject protected SettingsContract.ISettingsPresenter presenter;
    @Inject protected ISettings settings;

    private View rootView;
    private Spinner spinnerRecording;
    private CheckBox /*checkBoxCallEnabled,
                    checkBoxSmsEnabled,*/
                    checkHiddenMode,
                    checkShowFileExtension,
                    checkAllowMobileInternet,
                    checkAllowRoaming,
                    checkUseInternalPlayer,
                    checkAbonentToFileName;
    private TextView textViewPath;
    private ImageView buttonSelDir;
    private Spinner spinnerSoundSource;
    private CheckBox checkFileAmountLimitation;
    private EditText editMaxFileNumber;
    private CheckBox checkDataSizeLimitation;
    private EditText editMaxDataSize;
    private Spinner spinnerDataUnit;
    private EditText editPhoneSecretNumber;

    private CheckBox checkBoxShowNotification;
    private Spinner spinnerNotificationIcon;
    private Button buttonExportJournal;
    //private EditText editNotificationTitle;


    public SettingsFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.setting_fragment, container, false);

        findViews();
        //setupTitle(rootView);
        initViews();

        injectDependancy();
        readAllSettings();

        return rootView;
    }

    void injectDependancy()
    {
        MyApplication.getComponent().inject(this);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    @Override
    public void onViewStateRestored(Bundle bundle)
    {
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onStart()
    {
        presenter.setUI(SettingsFragment.this);
        readAllSettings();
        super.onStart();
    }
    @Override
    public void onStop()
    {
        presenter.removeUI();
        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onDestroyView() {
        presenter.removeUI();
        super.onDestroyView();
    };

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void findViews() {
        /*checkBoxCallEnabled = (CheckBox) rootView.findViewById(R.id.checkBoxCallEnabled);
        checkBoxCallEnabled.setId(View.NO_ID);
        checkBoxSmsEnabled = (CheckBox) rootView.findViewById(R.id.checkBoxSmsEnabled);*/

        spinnerRecording = (Spinner) rootView.findViewById(R.id.spinnerRecording);

        checkHiddenMode = (CheckBox) rootView.findViewById(R.id.checkHiddenMode);
        checkShowFileExtension = (CheckBox) rootView.findViewById(R.id.checkShowFileExtension);
        checkAllowMobileInternet = (CheckBox) rootView.findViewById(R.id.checkAllowMobileInternet);
        checkAllowRoaming = (CheckBox) rootView.findViewById(R.id.checkAllowRoaming);
        checkUseInternalPlayer = (CheckBox) rootView.findViewById(R.id.checkUseInternalPlayer);
        checkAbonentToFileName = (CheckBox) rootView.findViewById(R.id.checkAbonentToFileName);

        textViewPath = (TextView) rootView.findViewById(R.id.textViewPath);
        buttonSelDir = (ImageView) rootView.findViewById(R.id.buttonSelDir);
        spinnerSoundSource = (Spinner) rootView.findViewById(R.id.spinnerSoundSource);
        checkFileAmountLimitation = (CheckBox) rootView.findViewById(R.id.checkFileNumberLimitation);
        editMaxFileNumber = (EditText) rootView.findViewById(R.id.editMaxFileNumber);
        checkDataSizeLimitation = (CheckBox) rootView.findViewById(R.id.checkDataSizeLimitation);
        editMaxDataSize = (EditText) rootView.findViewById(R.id.editMaxDataSize);
        spinnerDataUnit = (Spinner) rootView.findViewById(R.id.spinnerDataUnit);
        editPhoneSecretNumber = (EditText) rootView.findViewById(R.id.editPhoneSecretNumber);
        checkBoxShowNotification = (CheckBox) rootView.findViewById(R.id.checkBoxShowNotification);
        spinnerNotificationIcon = (Spinner) rootView.findViewById(R.id.spinnerNotificationIcon);

        buttonExportJournal = (Button)rootView.findViewById(R.id.buttonExportJournal);
        //editNotificationTitle = (EditText) rootView.findViewById(R.id.editNotificationTitle);
    }


    private boolean ignoreChanges=false;
    private final int CALL_RECORDING=1, SMS_RECORDING=2;
    private void initViews() {
        ArrayAdapter<CharSequence> recordingAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.recording_data, android.R.layout.simple_spinner_item);
        recordingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecording.setAdapter(recordingAdapter);
        spinnerRecording.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(ignoreChanges) return;
                if(settings!=null) {
                    settings.setEnableCallRecording((position&CALL_RECORDING)!=0);
                    settings.setEnableSmsRecording((position&SMS_RECORDING)!=0);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        /*checkBoxCallEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setEnableCallRecording(checkBoxCallEnabled.isChecked());
            }
        });

        checkBoxSmsEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setEnableSmsRecording(checkBoxSmsEnabled.isChecked());
            }
        });*/

        checkHiddenMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setHiddenMode(checkHiddenMode.isChecked());
            }
        });

        checkShowFileExtension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setUseFileExtension(checkShowFileExtension.isChecked());
            }
        });

        checkAllowMobileInternet .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setUsingMobileInternet(checkAllowMobileInternet.isChecked());
                checkAllowRoaming.setEnabled(checkAllowMobileInternet.isChecked());
            }
        });
        checkAllowRoaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setSendInRoaming(checkAllowRoaming.isChecked());
            }
        });

        checkUseInternalPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setUseInternalPlayer(checkUseInternalPlayer.isChecked());
            }
        });
        checkAbonentToFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setAbonentToFileName(checkAbonentToFileName.isChecked());
            }
        });

        buttonSelDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreChanges) return;
                selectDir();
            }
        });

        ArrayAdapter<CharSequence> soundSourceAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sound_source_array, android.R.layout.simple_spinner_item);
        soundSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSoundSource.setAdapter(soundSourceAdapter);
        spinnerSoundSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(ignoreChanges) return;
                if(settings!=null)
                  settings.setSoundSource(SoundSource.values()[position]);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        checkFileAmountLimitation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 userChangedFileAmountLimitation();
            }
        });

        editMaxFileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {     }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {   }

            @Override
            public void afterTextChanged(Editable s) {
                if(ignoreChanges) return;
                if(settings==null) return;
                try {
                    int v = Integer.parseInt(s.toString());
                    if ((v < 1) || (v > settings.maxKeptFileAmount())) throw new Exception();
                    settings.setKeptFileAmount(v);
                    editMaxFileNumber.setError(null);
                } catch (Exception e) {
                    editMaxFileNumber.setError(getText(R.string.incorrect_value));
                }
            }
        });

        checkDataSizeLimitation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userChangedDataSizeLimitation();
            }
        });
        ArrayAdapter<CharSequence> dataUnitAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.data_size_units, android.R.layout.simple_spinner_item);
        dataUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataUnit.setAdapter(dataUnitAdapter);
        editMaxDataSize.addTextChangedListener(new TextWatcher() {
            @Override  public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override  public void onTextChanged(CharSequence s, int start, int before, int count) {  }

            @Override
            public void afterTextChanged(Editable s) {
                userChangedDataSize();
            }
        });
        spinnerDataUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userChangedDataSize();
            }

            @Override  public void onNothingSelected(AdapterView<?> parent) {}
        });

        editPhoneSecretNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(ignoreChanges) return;
                if(settings==null) return;
                settings.setSecretNumber(s.toString());
            }
        });

        checkBoxShowNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userChangedPermanentNotification();
            }
        });

        IconNotificationSpinnerAdapter iconSpinnerAdapter= IconNotificationSpinnerAdapter.create(getContext(), AntiTaskKillerNotification.notificationIcons);
        spinnerNotificationIcon.setAdapter(iconSpinnerAdapter);
        spinnerNotificationIcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userChangedPermanentNotification();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        buttonExportJournal.setOnClickListener((View v)->{
            presenter.sendJournal();
        });
    }


    private void userChangedFileAmountLimitation()
    {
        editMaxFileNumber.setEnabled(checkFileAmountLimitation.isChecked());
        if(ignoreChanges) return;
        if(settings==null) return;
        settings.setFileAmountLimitation(checkFileAmountLimitation.isChecked());
    }
    private void userChangedDataSizeLimitation()
    {
        spinnerDataUnit.setEnabled(checkDataSizeLimitation.isChecked());
        editMaxDataSize.setEnabled(checkDataSizeLimitation.isChecked());
        if(ignoreChanges) return;
        if(settings==null) return;
        settings.setDataSizeLimitation(checkDataSizeLimitation.isChecked());
    }
    private void userChangedDataSize()
    {
        if(ignoreChanges) return;
        if(settings==null) return;
        try {
            long sz = Long.parseLong(editMaxDataSize.getText().toString());
            int unit = spinnerDataUnit.getSelectedItemPosition();
            DataSize dataSize = new DataSize(sz, unit);
            long bytes=dataSize.getBytes();
            long maxSize=settings.maxFileDataSize();
            if ( (bytes < 1) || ( bytes> maxSize))
                throw new Exception();
            settings.setFileDataSize(dataSize);
            editMaxDataSize.setError(null);
        } catch (Exception e) {
            editMaxDataSize.setError(getText(R.string.incorrect_value));
        }
    }

    private void userChangedPermanentNotification()
    {
        if(ignoreChanges) return;
        if(settings==null) return;

        settings.setAntTaskKillerNotification(new AntiTaskKillerNotificationParam(
                checkBoxShowNotification.isChecked(),
                spinnerNotificationIcon.getSelectedItemPosition()  ));

        spinnerNotificationIcon.setEnabled(checkBoxShowNotification.isChecked());
    };


    @Override
    public void updateSavePath()
    {
        textViewPath.setText(settings.getSavingUrlPath());
    };
    protected void readAllSettings()
    {
        ignoreChanges=true;

        int pos=0;
        if(settings.getEnableCallRecording()) pos = pos|CALL_RECORDING;
        if(settings.getEnableSmsRecording()) pos = pos|SMS_RECORDING;
        spinnerRecording.setSelection(pos);

        /*checkBoxCallEnabled.setChecked(settings.getEnableCallRecording());
        checkBoxSmsEnabled.setChecked(settings.getEnableSmsRecording());*/
        checkHiddenMode.setChecked(settings.getHiddenMode());
        checkShowFileExtension.setChecked(settings.getUseFileExtension());
        checkAllowMobileInternet.setChecked(settings.getUsingMobileInternet());
        checkAllowRoaming.setChecked(settings.getAllowSendingInRoaming());
        checkAllowRoaming.setEnabled(checkAllowMobileInternet.isChecked());

        checkUseInternalPlayer.setChecked(settings.getUseInternalPlayer());
        checkAbonentToFileName.setChecked(settings.getAbonentToFileName());
        updateSavePath();

        spinnerSoundSource.setSelection(settings.getSoundSource().ordinal());

        checkFileAmountLimitation.setChecked(settings.getFileAmountLimitation());
        userChangedFileAmountLimitation();
        editMaxFileNumber.setText(String.valueOf(settings.getKeptFileAmount()));

        checkDataSizeLimitation.setChecked(settings.getDataSizeLimitation());
        userChangedDataSizeLimitation();

        DataSize dataSize=settings.getFileDataSize();
        editMaxDataSize.setText(String.valueOf(dataSize.getUnitSize()));
        spinnerDataUnit.setSelection(dataSize.getUnit().ordinal());
        editPhoneSecretNumber.setText(settings.getSecretNumber());

        AntiTaskKillerNotificationParam antiTaskKillerNotificationParam=settings.getAntiTaskKillerNotification();
        checkBoxShowNotification.setChecked(antiTaskKillerNotificationParam.visible);
        spinnerNotificationIcon.setSelection(antiTaskKillerNotificationParam.iconNum);


        ignoreChanges=false;

    }

    protected void selectDir()
    {
        presenter.chooseDataDir();
    };


}
