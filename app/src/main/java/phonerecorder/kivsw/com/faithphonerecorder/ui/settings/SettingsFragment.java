package phonerecorder.kivsw.com.faithphonerecorder.ui.settings;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kivsw.mvprxdialog.BaseMvpFragment;

import phonerecorder.kivsw.com.faithphonerecorder.ui.model.Settings;

/**

 */
public class SettingsFragment extends BaseMvpFragment
    implements SettingsContract.ISettingsView
{

    private SettingsPresenter presenter;
    private Settings settings;
    private View rootView;
    private CheckBox checkBoxCallEnabled,
                     checkBoxSmsEnabled,
                     checkHiddenMode,
                     checkShowFileExtension;
    private TextView textViewPath;
    private ImageView buttonSelDir;
    private Spinner spinnerSoundSource;
    private EditText editMaxFileNumber;
    private EditText editMaxDataSize;
    private Spinner  spinnerDataUnit;
    private EditText editPhoneSecretNumber;

    public static SettingsFragment newInstance(long id)
    {
        SettingsFragment fragment=new SettingsFragment();
        Bundle args = new Bundle();
        args.putLong(PRESENTER_ID, id);

        fragment.setArguments(args);
        return fragment;
    };

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
        rootView= inflater.inflate(R.layout.fragment_setting, container, false);

        FindViews();
        presenter = (SettingsPresenter)getPresenter();
        presenter.setUI(this);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDestroyView()
    {
        presenter.setUI(null);
        super.onDestroyView();

    };

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void FindViews()
    {
        checkBoxCallEnabled=(CheckBox)rootView.findViewById(R.id.checkBoxCallEnabled);
        checkBoxSmsEnabled=(CheckBox)rootView.findViewById(R.id.checkBoxSmsEnabled);
        checkHiddenMode=(CheckBox)rootView.findViewById(R.id.checkHiddenMode);
        checkShowFileExtension=(CheckBox)rootView.findViewById(R.id.checkShowFileExtension);
        textViewPath=(TextView)rootView.findViewById(R.id.textViewPath);
        buttonSelDir=(ImageView)rootView.findViewById(R.id.buttonSelDir);
        spinnerSoundSource=(Spinner)rootView.findViewById(R.id.spinnerSoundSource);
        editMaxFileNumber=(EditText)rootView.findViewById(R.id.editMaxFileNumber);
        editMaxDataSize=(EditText)rootView.findViewById(R.id.editMaxDataSize);
        spinnerDataUnit=(Spinner)rootView.findViewById(R.id.spinnerDataUnit);
        editPhoneSecretNumber=(EditText)rootView.findViewById(R.id.editPhoneSecretNumber);
    };

    @Override
    public void setSettings(Settings settings) {

    }
}
