package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import phonerecorder.kivsw.com.faithphonerecorder.R;
import phonerecorder.kivsw.com.faithphonerecorder.model.settings.ISettings;
import phonerecorder.kivsw.com.faithphonerecorder.os.MyApplication;

//import android.widget.Toolbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment
            implements RecordListContract.IRecordListView, RecordListAdapter.UIEventHandler
{

    private Menu menu;
    private View rootView;
    private TextView pathTextView;
    private Spinner spinnerPath;
    private ImageView buttonSelDir;
    private RecyclerView recordList;
    private RecordListAdapter recordListAdapter;
    private Toolbar toolbar;
    private ISettings settings;
    private ProgressBar progressBar;

    @Inject
    protected RecordListPresenter presenter;


    public RecordListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.record_list_fragment, container, false);
        findViews();
        initViews();
        injectDependancy();
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.setUI(this);
    };

    @Override
    public void onStop()
    {
        presenter.removeUI();
        super.onStop();

    }
    private void findViews() {
        pathTextView = (TextView) rootView.findViewById(R.id.checkBoxCallEnabled);
        spinnerPath=(Spinner)rootView.findViewById(R.id.spinnerPath);
        buttonSelDir=(ImageView)rootView.findViewById(R.id.buttonSelDir);
        recordList=(RecyclerView)rootView.findViewById(R.id.recordList);
        toolbar=(Toolbar)rootView.findViewById(R.id.toolbar);
        progressBar=(ProgressBar)rootView.findViewById(R.id.progressBar);
    };

    private void initViews()
    {
        getActivity().getMenuInflater()
                .inflate(R.menu.record_list_menu, toolbar.getMenu());
        MenuItem search=toolbar.getMenu().findItem(R.id.app_bar_search);
        View actionView=search.getActionView();
        ((SearchView)actionView).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.setFilter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.setFilter(newText);
                return true;
            }
        });

        buttonSelDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               presenter.chooseCurrentDir();
            }
        });

        recordList.setHasFixedSize(false);
        recordList.setLayoutManager(new LinearLayoutManager(getContext()));
        recordListAdapter = new RecordListAdapter();
        recordList.setAdapter(recordListAdapter);

    }
    private void injectDependancy()
    {
        MyApplication.getComponent()
                .inject(this);
    };

    @Override
    public void setSettings(ISettings settings) {
        this.settings = settings;


    }

    @Override
    public void setRecordList(List<RecordListContract.RecordFileInfo> fileList) {
      recordListAdapter.setData(fileList);

    }

    @Override
    public void setProgressBarVisible(boolean show) {
        if(show)  progressBar.setVisibility(View.VISIBLE);
        else      progressBar.setVisibility(View.GONE);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.itemUnselectAll:
                presenter.unselectAll();
                return true;
            case R.id.itemSelectAll:
                presenter.selectAll();
                return true;
            case R.id.itemDelete:
                presenter.deleteSelectedItems();
                return true;
            case R.id.itemRefresh:
                presenter.updateDir();
                return true;
        };
        return super.onContextItemSelected(item);
    }


    @Override
    public void playItem(int position) {
       presenter.playItem(position);
    }

    @Override
    public void selectPlayerItem(int position) {
        presenter.selectPlayerItem(position);
    }

    @Override
    public void selectItem(int position, boolean select) {
        presenter.selectItem(position, select);
    }
}
