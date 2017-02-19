package in.codeseed.tubely.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.codeseed.tubely.R;
import in.codeseed.tubely.adapters.TubeStatusAdapter;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.loaders.CurrentTubeStatusLoader;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.service.DownloadTubeStatusIntentService;
import in.codeseed.tubely.util.Util;

public class CurrentTubeStatusFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = CurrentTubeStatusFragment.class.getSimpleName();
    private static final int LOADER_CURRENT_TUBESTATUS_ID = 1;

    @Bind(R.id.tubeStatusRecyclerView) RecyclerView mTubeStausRecyclerView;
    @Bind(R.id.tubeFragmentSwipeRefresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.tubeLoader) RelativeLayout mTubeLoader;
    @Bind(R.id.tubeLoaderTextView) TextView mTubeLoaderTextView;
    @Bind(R.id.tubeStatusLastUpdate) TextView mTubeStatusLastUpdate;
    @Bind(R.id.tubeLoaderImageView) ImageView mTubeLoaderImageView;

    private GridLayoutManager gridLayoutManager;
    private TubeStatusAdapter tubeStatusAdapter;
    private CurrentTubeStatusLoader weekendTubeStatusLoader;
    private List<Tube> tubeList;
    private SharedPreferences preferences;
    private Util util;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        util = Util.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rooView = inflater.inflate(R.layout.fragment_tubestatus, container, false);
        ButterKnife.bind(this, rooView);
        return rooView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tubeList = new ArrayList<>();

        //Setup swipe refresh Layout
        mSwipeRefreshLayout.setColorSchemeResources(R.color.circle_bg, R.color.overground_bg, R.color.victoria_bg, R.color.dlr_bg);

        //Setup GridLayout Manager and Recycler view
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        }else {
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        }

        mTubeStausRecyclerView.setLayoutManager(gridLayoutManager);
        tubeStatusAdapter = new TubeStatusAdapter(getActivity().getApplicationContext(), tubeList, R.layout.grid_card_tubestatus);
        mTubeStausRecyclerView.setAdapter(tubeStatusAdapter);

//        mTubeStausRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                Fix to let recycler view scroll to the top without getting disturbed by swipe refresh layout
//                mSwipeRefreshLayout.setEnabled(gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
//
//                Hide Lastupdate Textview while scrolling tube status
//                mTubeStatusLastUpdate.setVisibility(gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 ? View.VISIBLE : View.INVISIBLE);
//            }
//        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCurrentTubeStatus();
            }
        });

        //Initialize the loader and register for the callback
        LoaderManager loaderManager = this.getLoaderManager();
        loaderManager.initLoader(LOADER_CURRENT_TUBESTATUS_ID, null, this);

        //Do Local Update
        doLocalUpdate();

        //Start the intent service to update current tube status
        if(util.isNetworkConnected())
            updateCurrentTubeStatus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Refresh");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch((String)item.getTitle()){
            case "Refresh":
                updateCurrentTubeStatus();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(tubeList.isEmpty()) {
            doLocalUpdate();
        }else{
            mTubeStatusLastUpdate.setText(getLastUpdatedCurrentTubeStatusTime());
            tubeStatusAdapter.updateAdapter(tubeList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case LOADER_CURRENT_TUBESTATUS_ID:
                weekendTubeStatusLoader = new CurrentTubeStatusLoader(getActivity().getApplicationContext(),
                        TubelyDBContract.LineStatusEntry.buildLineStatusUri("current"),
                        TubelyDBContract.LineStatusEntry.projection_current,
                        null,
                        null,
                        null);
                return weekendTubeStatusLoader;
            default:
                throw new UnsupportedOperationException("Loader ID not found!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        tubeList = new ArrayList<>();

        if(cursor != null && cursor.moveToFirst()){
            do{
                Tube tube = new Tube();
                tube.setName(cursor.getString(1));
                tube.setStatus(cursor.getString(2));
                tube.setExtraInformation(cursor.getString(3));
                if(tube.getStatus().equalsIgnoreCase("")){
                    setDataError();
                    return;
                }
                tubeList.add(tube);
            }while(cursor.moveToNext());
        }else{
            //set error as there is no data in the DB
            setDataError();
            return;
        }

        //Update UI
        if(mTubeLoader.getVisibility() == View.VISIBLE)
            mTubeLoader.setVisibility(View.INVISIBLE);

        tubeStatusAdapter.updateAdapter(tubeList);
        mTubeStatusLastUpdate.setText(getLastUpdatedCurrentTubeStatusTime());

        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        tubeStatusAdapter.updateAdapter(null);
    }

    private void updateCurrentTubeStatus(){
        if(util.isNetworkConnected()) {
            if(!mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(true);
            DownloadTubeStatusIntentService.startActionTubeStatusCurrent(getActivity().getApplicationContext());
        }
        else {
            setNetworkError();
        }
    }

    void setNetworkError(){

        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);

        Snackbar.make(mSwipeRefreshLayout, R.string.network_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCurrentTubeStatus();
                    }
                })
                .show();
        if(mTubeLoader.getVisibility() == View.VISIBLE)
            mTubeLoaderTextView.setText("Pull down to refresh!");

    }

    void setDataError(){

        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
        mTubeLoader.setVisibility(View.VISIBLE);
        mTubeLoaderTextView.setText("Swipe down to refresh!");
    }

    void doLocalUpdate(){
       this.getLoaderManager().getLoader(LOADER_CURRENT_TUBESTATUS_ID).forceLoad();
    }

    String getLastUpdatedCurrentTubeStatusTime(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        long lastUpdatedTimeInMillis = preferences.getLong(Util.SHARED_PREF_TUBESTATUS_CURRENT, 0l);
        if( lastUpdatedTimeInMillis == 0) {
            return "";
        }else {
            Date lastUpdatedDate = new Date();
            lastUpdatedDate.setTime(lastUpdatedTimeInMillis);
            return "Updated " + Util.calculateTweetTime(lastUpdatedDate) + " ago!";
        }
    }
}
