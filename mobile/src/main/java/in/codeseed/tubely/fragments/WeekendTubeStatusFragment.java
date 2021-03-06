package in.codeseed.tubely.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import in.codeseed.tubely.adapters.TubeStatusWeekendAdapter;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.loaders.WeekendTubeStatusLoader;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.service.DownloadTubeStatusIntentService;
import in.codeseed.tubely.util.Util;

public class WeekendTubeStatusFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    @Bind(R.id.tubeStatusRecyclerView) RecyclerView recyclerView;
    @Bind(R.id.tubeLoader) RelativeLayout tubeLoader;
    @Bind(R.id.tubeLoaderTextView) TextView tubeLoaderTextView;
    @Bind(R.id.tubeStatusLastUpdate) TextView tubeStatusLastUpdate;
    @Bind(R.id.tubeLoaderImageView) ImageView tubeLoaderImageView;
    @Bind(R.id.tubeFragmentSwipeRefresh) SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = WeekendTubeStatusFragment.class.getSimpleName();
    private static final int LOADER_WEEKEND_TUBESTATUS_ID = 2;

    private GridLayoutManager gridLayoutManager;
    private TubeStatusWeekendAdapter tubeStatusAdapter;
    private WeekendTubeStatusLoader weekendTubeStatusLoader;
    private List<Tube> tubeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tubestatus, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tubeList = new ArrayList<>();

        //Setup swipe refresh Layout
        swipeRefreshLayout.setColorSchemeResources(R.color.circle_bg, R.color.overground_bg, R.color.victoria_bg, R.color.dlr_bg);

        //Setup GridLayout Manager and Recycler view
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        tubeStatusAdapter = new TubeStatusWeekendAdapter(getActivity().getApplicationContext(), tubeList, R.layout.grid_card_tubestatus);
        recyclerView.setAdapter(tubeStatusAdapter);

//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
                //Fix to let recycler view scroll to the top without getting disturbed by swipe refresh layout
//                swipeRefreshLayout.setEnabled(gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);

                //Hide Lastupdate Textview while scrolling tube status
//                tubeStatusLastUpdate.setVisibility(gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 ? View.VISIBLE:View.INVISIBLE);
//            }
//        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCurrentTubeStatus();
            }
        });

        //Initialize the loader and register for the callback
        LoaderManager loaderManager = this.getLoaderManager();
        loaderManager.initLoader(LOADER_WEEKEND_TUBESTATUS_ID, null, this);

        //Do Local Update
        doLocalUpdate();

        //Start the intent service to update current tube status
        if(isNetworkConnected())
            updateCurrentTubeStatus();
    }

    private void updateCurrentTubeStatus(){
        if(isNetworkConnected()) {
            if(!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
                DownloadTubeStatusIntentService.startActionTubeStatusWeekend(getActivity().getApplicationContext());
        }
        else {
            setNetworkError();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tubeList.isEmpty()) {
            doLocalUpdate();
        }else{
            tubeStatusLastUpdate.setText(getLastUpdatedWeekendTubeStatusTime());
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
            case LOADER_WEEKEND_TUBESTATUS_ID:
                weekendTubeStatusLoader = new WeekendTubeStatusLoader(getActivity().getApplicationContext(),
                        TubelyDBContract.LineStatusEntry.buildLineStatusUri("weekend"),
                        TubelyDBContract.LineStatusEntry.projection_weekend,
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
        if(tubeLoader.getVisibility() == View.VISIBLE) {
            tubeLoader.setVisibility(View.INVISIBLE);
        }

        tubeStatusAdapter.updateAdapter(tubeList);
        tubeStatusLastUpdate.setText(getLastUpdatedWeekendTubeStatusTime());

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        tubeStatusAdapter.updateAdapter(null);
    }

    public void setNetworkError(){

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);


        Snackbar.make(swipeRefreshLayout, R.string.network_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCurrentTubeStatus();
                    }
                })
                .show();

        if(tubeLoader.getVisibility() == View.VISIBLE)
            tubeLoaderTextView.setText("Pull down to refresh!");
    }

    public void setDataError(){

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        tubeLoaderTextView.setText("Swipe down to refresh!");
        tubeLoader.setVisibility(View.VISIBLE);
    }

    public boolean isNetworkConnected(){
        ConnectivityManager conn = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void doLocalUpdate(){
       this.getLoaderManager().getLoader(LOADER_WEEKEND_TUBESTATUS_ID).forceLoad();
    }

    public String getLastUpdatedWeekendTubeStatusTime(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        long lastUpdatedTimeInMillis = preferences.getLong(Util.SHARED_PREF_TUBESTATUS_WEEKEND, 0l);
        if( lastUpdatedTimeInMillis == 0) {
            return "";
        }else {
            Date lastUpdatedDate = new Date();
            lastUpdatedDate.setTime(lastUpdatedTimeInMillis);
            return "Updated " + Util.calculateTweetTime(lastUpdatedDate) + " ago!";
        }
    }
}
