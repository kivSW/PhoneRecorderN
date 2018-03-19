package phonerecorder.kivsw.com.faithphonerecorder.ui.mainscreen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment {


    public static RecordListFragment newInstance() {
        RecordListFragment fragment = new RecordListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Menu menu;

    public RecordListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //setHasOptionsMenu(true);
        return inflater.inflate(R.layout.record_list_fragment, container, false);
    }



}
