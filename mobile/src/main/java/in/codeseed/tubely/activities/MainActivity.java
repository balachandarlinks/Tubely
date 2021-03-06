package in.codeseed.tubely.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.codeseed.tubely.R;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.fragments.CurrentTubeStatusFragment;
import in.codeseed.tubely.fragments.NearbyStationsFragment;
import in.codeseed.tubely.fragments.WeekendTubeStatusFragment;
import in.codeseed.tubely.service.DownloadTubeStatusIntentService;
import in.codeseed.tubely.simplexml.allstations.Station;
import in.codeseed.tubely.util.Util;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int IDLE_PAGE_LIMIT = 2;
    @Bind(R.id.tubeStatusViewPager) ViewPager mTubeStatusViewPager;
    SharedPreferences mSharedPreferences;
    private TubeStatusViewPagerAdapter mTubeStatusViewPagerAdapter;
    private ArrayList<String> mSuggestedStations;
    private MatrixCursor mSuggestionsCursor;
    private CursorAdapter mSuggestionsCursorAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mTubeStatusViewPagerAdapter = new TubeStatusViewPagerAdapter(getSupportFragmentManager());
        mTubeStatusViewPager.setAdapter(mTubeStatusViewPagerAdapter);
        mTubeStatusViewPager.setOffscreenPageLimit(IDLE_PAGE_LIMIT);
        mTubeStatusViewPager.setCurrentItem(1, true);
        mTubeStatusViewPager.setOnPageChangeListener(this);

        //Load Tube Stations Code into Memory -> Util.allStations.
        DownloadTubeStatusIntentService.startActionLoadStationsData(getApplicationContext());

        if (isFirstAppStart()) {
            DownloadTubeStatusIntentService.startActionTubeStatusWeekend(getApplicationContext());
            DownloadTubeStatusIntentService.startActionTubeStatusCurrent(getApplicationContext());
            DownloadTubeStatusIntentService.startActionStationFacilities(getApplicationContext());

            Intent welcomeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(welcomeIntent);
        }else{
            if (isStationFacilityNeedToUpdate())
                DownloadTubeStatusIntentService.startActionStationFacilities(getApplicationContext());

            DownloadTubeStatusIntentService.startActionTubeStatusWeekend(getApplicationContext());
            DownloadTubeStatusIntentService.startActionTubeStatusCurrent(getApplicationContext());
        }
    }

    private boolean isFirstAppStart() {
        return mSharedPreferences.getBoolean(Util.SHARED_PREF_FIRST_TIME_WELCOME, true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Util.NEARBY_FRAGMENTS_VISIBLE = position == 0;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSearchView != null && mSearchView.isEnabled())
            mSearchView.setIconified(true);
    }

    boolean isStationFacilityNeedToUpdate(){
        final int new_db_code = getResources().getInteger(R.integer.db_code);
        final int current_db_code = mSharedPreferences.getInt(Util.SHARED_PREF_DB_UPDATE_CODE, 0);
        return new_db_code > current_db_code;
    }

    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return;
        }else{
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //Search Config
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem= menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(true);
        mSearchView.setQueryHint("Search a tube station..");

        final int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchIcon = (ImageView) mSearchView.findViewById(searchImgId);
        searchIcon.setImageResource(R.drawable.search);

        mSuggestionsCursorAdapter = getSearchSuggestionsAdapter();
        mSearchView.setSuggestionsAdapter(mSuggestionsCursorAdapter);

        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchView.clearFocus();
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                updateSuggestions(queryText);
                mSuggestionsCursor = getSuggestionsCursor(mSuggestedStations);
                mSuggestionsCursorAdapter.swapCursor(mSuggestionsCursor);
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onNewIntent(Intent searchIntent) {
        super.onNewIntent(searchIntent);
        if(null == searchIntent.getAction())
            return;

        if(searchIntent.getAction().equals(Intent.ACTION_SEARCH)){
            String stationName = searchIntent.getDataString();
            if(stationName.equalsIgnoreCase("No stations found!"))
                return;
            String stationCode = "";
            String line = "";
            for(Station station : Util.getAllStations().getStations()){
                if(station.getName().equalsIgnoreCase(stationName)){
                    stationCode = station.getCode();
                    line = station.getLine();
                }
            }

            //Log.d(TAG, stationName + "--" + stationCode + "--" + line );
            Intent stationIntent = new Intent(getApplicationContext(), StationActivity.class);
            stationIntent.putExtra("station", stationName);
            stationIntent.putExtra("code", stationCode);
            stationIntent.putExtra("line", line);
            startActivity(stationIntent);
        }
    }

    private CursorAdapter getSearchSuggestionsAdapter() {

        mSuggestedStations = new ArrayList<>();
        mSuggestionsCursor = getSuggestionsCursor(mSuggestedStations);
        return new SuggestionsAdapter(getApplicationContext(), mSuggestionsCursor, true);
    }

    private MatrixCursor getSuggestionsCursor(final ArrayList<String> suggestions) {

        final String[] columns = new String[] {TubelyDBContract.StationTable._ID, "suggest_intent_data"};
        final Object[] object = new Object[] { 0, "default" };

        final MatrixCursor matrixCursor = new MatrixCursor(columns);

        for (int i = 0; i < suggestions.size(); i++) {

            object[0] = i;
            object[1] = suggestions.get(i);

            matrixCursor.addRow(object);
        }

        return matrixCursor;
    }

    private void updateSuggestions(String queryText){
        queryText = queryText.toLowerCase();
        mSuggestedStations.clear();
        if(Util.getAllStations() != null) {
            for (Station station : Util.getAllStations().getStations()) {
                String temp = station.getName().toLowerCase();
                if (temp.contains(queryText)) {
                    mSuggestedStations.add(station.getName());
                }
            }
        }
        if (mSuggestedStations.isEmpty())
            mSuggestedStations.add("No stations found!");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingActivity = new Intent(this, TubelySettingsActivity.class);
            startActivity(settingActivity);
            return true;
        }else if(id == R.id.action_map){
            Intent intent = new Intent(getApplicationContext(), TubeMapActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_search){
            mSearchView.findFocus();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class TubeStatusViewPagerAdapter extends FragmentStatePagerAdapter{

        private CurrentTubeStatusFragment currentTubeStatusFragment;
        private WeekendTubeStatusFragment weekendTubeStatusFragment;
        private NearbyStationsFragment nearbyStationsFragment;

        public TubeStatusViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                nearbyStationsFragment = new NearbyStationsFragment();
                return nearbyStationsFragment;
            } else if (i == 1) {
                currentTubeStatusFragment = new CurrentTubeStatusFragment();
                return currentTubeStatusFragment;
            } else if(i == 2){
                weekendTubeStatusFragment = new WeekendTubeStatusFragment();
                return weekendTubeStatusFragment;
            }
            throw new UnsupportedOperationException("MainActivity PagerAdapter Index Out of Bound!");
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Nearby Stations";
            } else if (position == 1) {
                return "Live";
            } else if (position == 2){
                return "Weekend";
            } else {
                return "Tubely";
            }
        }
    }

    private static class SuggestionsAdapter extends CursorAdapter{

        private LayoutInflater mInflater;
        private Context appContext;

        public SuggestionsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            appContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.search_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView searchItem = (TextView) view.findViewById(R.id.station_name);
            searchItem.setText(cursor.getString(cursor.getColumnIndex("suggest_intent_data")));
        }
    }
}
