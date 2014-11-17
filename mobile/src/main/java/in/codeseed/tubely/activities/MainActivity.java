package in.codeseed.tubely.activities;

import android.animation.LayoutTransition;
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
import java.util.Date;

import in.codeseed.tubely.R;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.fragments.CurrentTubeStatusFragment;
import in.codeseed.tubely.fragments.WeekendTubeStatusFragment;
import in.codeseed.tubely.service.DownloadTubeStatusIntentService;
import in.codeseed.tubely.simplexml.allstations.Station;
import in.codeseed.tubely.util.Util;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager tubeStatusViewPager;
    private TubeStatusViewPagerAdapter tubeStatusViewPagerAdapter;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;
    private ArrayList<String> data;
    private MatrixCursor cursor;
    private CursorAdapter cursorAdapter;
    private SearchView searchView;
    private long timeinMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        injectViews();

        tubeStatusViewPager.setAdapter(tubeStatusViewPagerAdapter);

        DownloadTubeStatusIntentService.startActionLoadStationsData(getApplicationContext());

        if(isStationFacilityNeedToUpdate()){
            if(timeinMillis == 0){
                DownloadTubeStatusIntentService.startActionTubeStatusCurrent(getApplicationContext());
                DownloadTubeStatusIntentService.startActionTubeStatusWeekend(getApplicationContext());
                DownloadTubeStatusIntentService.startActionStationFacilities(getApplicationContext());
            }else{
                DownloadTubeStatusIntentService.startActionStationFacilities(getApplicationContext());
            }
        }

        if(preferences.getBoolean(Util.SHARED_PREF_FIRST_TIME_WELCOME, true)) {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void injectViews(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferenceEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        tubeStatusViewPager = (ViewPager) findViewById(R.id.tubeStatusViewPager);
        tubeStatusViewPagerAdapter = new TubeStatusViewPagerAdapter(getSupportFragmentManager());
    }

    boolean isStationFacilityNeedToUpdate(){
        timeinMillis = preferences.getLong(Util.SHARED_PREF_STATION_FACILITIES, 0);
        if(timeinMillis == 0)
            return  true;

        Date lastUpdate = new Date();
        lastUpdate.setTime(timeinMillis);
        long hoursDiff = Util.calculateHours(lastUpdate);

        return (hoursDiff > 24) ? true : false;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(!searchView.isIconified()){
            searchView.setIconified(true);
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
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.search);

        searchView.setQueryHint("Search tube stations");
        searchView.clearFocus();

        searchView.setLayoutTransition(new LayoutTransition());

        cursorAdapter = getSearchSuggestionsAdapter();
        searchView.setSuggestionsAdapter(cursorAdapter);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.clearFocus();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                updateSuggestions(queryText);
                cursor = getCursor(data);
                cursorAdapter.swapCursor(cursor);
                return true;
            }
        });

        return true;
        //return super.onCreateOptionsMenu(menu);
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

    CursorAdapter getSearchSuggestionsAdapter(){

        data = new ArrayList<>();
        cursor = getCursor(data);
        return new SuggestionsAdapter(getApplicationContext(), cursor, true);
    }

    private MatrixCursor getCursor(final ArrayList<String> suggestions) {

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
        data.clear();
        for(Station station : Util.getAllStations().getStations()){
            String temp = station.getName().toLowerCase();
            if(temp.contains(queryText)){
                data.add(station.getName());
            }
        }
        if(data.isEmpty())
            data.add("No stations found!");
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
            searchView.findFocus();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class TubeStatusViewPagerAdapter extends FragmentStatePagerAdapter{

        private CurrentTubeStatusFragment currentTubeStatusFragment;
        private WeekendTubeStatusFragment weekendTubeStatusFragment;

        public TubeStatusViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                currentTubeStatusFragment = new CurrentTubeStatusFragment();
                return currentTubeStatusFragment;
            } else if (i == 1) {
                weekendTubeStatusFragment = new WeekendTubeStatusFragment();
                return weekendTubeStatusFragment;
            }
            throw new UnsupportedOperationException("MainActivity PagerAdapter Index Out of Bound!");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Live Line Status";
            } else if (position == 1) {
                return "Weekend Line Status";
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
