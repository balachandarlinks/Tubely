package in.codeseed.tubely.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.adapters.TubeStatusAdapter;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.loaders.CurrentTubeStatusLoader;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.service.DownloadTubeStatusIntentService;
import in.codeseed.tubely.util.Util;

public class CurrentTubeStatusFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    /*
    @InjectView(R.id.cts_dummytext) private TextView ctsDummyText;
    @InjectView(R.id.current_tubestatus_container) private FrameLayout currentTubeStatusContainer;
    */


    private static final String TAG = CurrentTubeStatusFragment.class.getSimpleName();
    private static final int LOADER_CURRENT_TUBESTATUS_ID = 1;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout tubeLoader;
    private TextView tubeLoaderTextView, tubeStatusLastUpdate;
    private ImageView tubeLoaderImageView;
    private GridLayoutManager gridLayoutManager;
    private TubeStatusAdapter tubeStatusAdapter;
    private CurrentTubeStatusLoader weekendTubeStatusLoader;
    private List<Tube> tubeList;
    private SharedPreferences preferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(tubeList.isEmpty()) {
            doLocalUpdate();
        }else{
            tubeStatusLastUpdate.setText(getLastUpdatedCurrentTubeStatusTime());
            tubeStatusAdapter.updateAdapter(tubeList);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tubestatus, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tubeList = new ArrayList<>();
        injectResources(view);

        //Setup swipe refresh Layout
        swipeRefreshLayout.setColorSchemeColors(R.color.circle_bg, R.color.overground_bg, R.color.victoria_bg, R.color.jubilee_bg);
        //swipeRefreshLayout.setProgressBackgroundColor(getResources().getColor(R.color.piccadilly_bg));

        //Setup GridLayout Manager and Recycler view
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        }else {
            gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        }

        recyclerView.setLayoutManager(gridLayoutManager);
        tubeStatusAdapter = new TubeStatusAdapter(getActivity().getApplicationContext(), tubeList, R.layout.grid_card_tubestatus);
        recyclerView.setAdapter(tubeStatusAdapter);

        //Fix to let recycler view scroll to the top without getting disturbed by swipe refresh layout
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        if(isNetworkConnected())
            updateCurrentTubeStatus();
    }

    private void injectResources(View view){

        recyclerView = (RecyclerView) view.findViewById(R.id.tubeStatusRecyclerView);
        tubeLoader = (RelativeLayout)view.findViewById(R.id.tubeLoader);
        tubeLoaderTextView = (TextView) view.findViewById(R.id.tubeLoaderTextView);
        tubeLoaderImageView = (ImageView) view.findViewById(R.id.tubeLoaderImageView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.tubeFragmentSwipeRefresh);
        tubeStatusLastUpdate = (TextView) view.findViewById(R.id.tubeStatusLastUpdate);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }

    private void updateCurrentTubeStatus(){
        if(isNetworkConnected()) {
            if(!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
                DownloadTubeStatusIntentService.startActionTubeStatusCurrent(getActivity().getApplicationContext());
        }
        else {
            setNetworkError();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        if(tubeLoader.getVisibility() == View.VISIBLE)
            tubeLoader.setVisibility(View.INVISIBLE);

        tubeStatusAdapter.updateAdapter(tubeList);
        tubeStatusLastUpdate.setText(getLastUpdatedCurrentTubeStatusTime());

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //TODO:Clear off the content from UI
        tubeStatusAdapter.updateAdapter(null);
    }

    void setNetworkError(){

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_network_error,
                (ViewGroup) getActivity().findViewById(R.id.toast_network_error));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText("Network Failure!");

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        if(tubeLoader.getVisibility() == View.VISIBLE)
            tubeLoaderTextView.setText("Pull down to refresh!");

    }

    void setDataError(){

        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        tubeLoader.setVisibility(View.VISIBLE);
        tubeLoaderTextView.setText("Swipe down to refresh!");
    }

    boolean isNetworkConnected(){
        ConnectivityManager conn = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
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
