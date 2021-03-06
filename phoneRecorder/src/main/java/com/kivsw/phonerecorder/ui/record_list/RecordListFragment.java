package com.kivsw.phonerecorder.ui.record_list;


import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kivsw.phonerecorder.model.settings.ISettings;
import com.kivsw.phonerecorder.os.MyApplication;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import phonerecorder.kivsw.com.phonerecorder.R;

//import android.widget.Toolbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordListFragment extends Fragment
            implements RecordListContract.IRecordListView, Toolbar.OnMenuItemClickListener
{

    protected class WrapContentLinearLayoutManager extends LinearLayoutManager {
        WrapContentLinearLayoutManager(Context context, @RecyclerView.Orientation int orientation,
                            boolean reverseLayout)
        {
            super(context,orientation,reverseLayout);
        }
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                
            }
        }
    }

    private Menu menu;
    private View rootView;
    private TextView pathTextView, noItemTextView;
    private Spinner spinnerPath;

    private ImageView buttonSelDir;
    private RecyclerView recordList;
    private RecordListAdapter recordListAdapter;
    private Toolbar toolbar;
    private ISettings settings;

    private ProgressBar progressBar;


    @Inject
    protected RecordListContract.IRecordListPresenter presenter;


    public RecordListFragment() {
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

        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.record_list_fragment, container, false);
        findViews();
        initViews();
        injectDependency();
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

    private void findViews() {
        pathTextView = (TextView) rootView.findViewById(R.id.pathTextView);
        noItemTextView = (TextView) rootView.findViewById(R.id.noItemTextView);
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

        spinnerPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String dir =(String)spinnerPath.getAdapter().getItem(position);
                presenter.setCurrentDir(dir);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        toolbar.setOnMenuItemClickListener(this);

        //recordList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recordList.setHasFixedSize(false);
        recordList.setLayoutManager(new LinearLayoutManager(getContext()));
        recordListAdapter = new RecordListAdapter();
        recordListAdapter.setUIEventHandler(new RecordListAdapter.UIEventHandler(){
            @Override
            public void playItem(int position) {
                presenter.playItem(position);
            }

            @Override
            public void playItemWithPlayerChoosing(int position) {
                presenter.playItemWithPlayerChoosing(position);
            }

            @Override
            public void selectItem(int position, boolean select) {
                presenter.selectItem(position, select);

            }

            @Override
            public void protectItem(int position, boolean select) {
                presenter.setUndeletable(position, select);
            }
        });
        recordList.setAdapter(recordListAdapter);

    }
    private void injectDependency()
    {
        MyApplication.getComponent()
                .inject(this);
    };

    @Override
    public void setSettings(ISettings settings) {
        this.settings = settings;
        updatePathList();
    }

    @Override
    public void setRecordList(List<RecordListContract.RecordFileInfo> fileList, boolean scrollToBegin) {
      if(fileList.size()>0) noItemTextView.setVisibility(View.GONE);
      else                  noItemTextView.setVisibility(View.VISIBLE);

      recordListAdapter.setData(fileList);
      if(scrollToBegin)
          recordList.scrollToPosition(0);
      updateMenu();
      updatePathList();
    }

    @Override
    public void setRecListProgressBarVisible(boolean show) {
        int visibility;
        /*Log.v("DBG", "progressBar.visibility="+show);
        Thread.dumpStack();*/

        if(show)  visibility=(View.VISIBLE);
        else      visibility=(View.GONE);
        progressBar.setVisibility(visibility);
    };

    @Override
    public void onRecordListChanged()
    {
        //recordList.invalidateItemDecorations();
        recordList.getAdapter().notifyDataSetChanged();
        updateMenu();
    }

    @Override
    public void onRecordChanged(int index) {
        recordList.getAdapter().notifyItemChanged(index);
        updateMenu();
    }

    ;

    @Override
    public boolean onMenuItemClick(MenuItem item)
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
                presenter.updateDir(true);
                return true;
        };
        return super.onContextItemSelected(item);
    }

    private void updateMenu()
    {
        boolean hasSelectedRecords=presenter.hasSelectedItem(false);
        boolean hasDelitable = presenter.hasSelectedItem(true);
        Menu menu=toolbar.getMenu();
        menu.findItem(R.id.itemUnselectAll).setVisible(hasSelectedRecords);
        menu.findItem(R.id.itemDelete).setVisible(hasDelitable);
        toolbar.invalidate();
    }
    private void updatePathList()
    {
        List<String> paths;
        if(settings!=null)  paths=settings.getViewUrlPathHistory();
        else paths=new ArrayList();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, paths);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPath.setAdapter(arrayAdapter);
        spinnerPath.setSelection(0);

    };

}
