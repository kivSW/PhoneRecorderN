package phonerecorder.kivsw.com.faithphonerecorder.ui.mainscreen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment {


    private Menu menu;

    public RecordListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.record_list_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.record_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
        this.menu= menu;
    }
}
