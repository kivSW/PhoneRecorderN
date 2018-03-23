package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment {

    private Menu menu;
    private View rootView;
    TextView pathTextView;
    Spinner spinnerPath;
    ImageView buttonSelDir;
    RecyclerView recordList;
    Toolbar toolbar;


    public RecordListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.record_list_fragment, container, false);
        findViews();
        initViews();
        return rootView;
    }

    private void findViews() {
        pathTextView = (TextView) rootView.findViewById(R.id.checkBoxCallEnabled);
        spinnerPath=(Spinner)rootView.findViewById(R.id.spinnerPath);
        buttonSelDir=(ImageView)rootView.findViewById(R.id.buttonSelDir);
        recordList=(RecyclerView)rootView.findViewById(R.id.recordList);
        toolbar=(Toolbar)rootView.findViewById(R.id.toolbar);
    };

    private void initViews()
    {
       toolbar.inflateMenu(R.menu.record_list_menu);

    }



}
