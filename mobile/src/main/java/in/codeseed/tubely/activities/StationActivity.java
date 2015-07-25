package in.codeseed.tubely.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import in.codeseed.tubely.R;
import in.codeseed.tubely.customviews.LinesBatchLayout;
import in.codeseed.tubely.customviews.ObservableScrollView;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.parsers.TflWebService;
import in.codeseed.tubely.simplexml.platform.Train;
import in.codeseed.tubely.simplexml.platform.TrainPlatform;
import in.codeseed.tubely.simplexml.platform.TrainPrediction;
import in.codeseed.tubely.util.Util;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.SimpleXMLConverter;

public class StationActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, ObservableScrollView.CallBack,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = StationActivity.class.getSimpleName();

    @Bind(R.id.station_address) TextView mStationAddress;
    @Bind(R.id.station_phone) TextView mStationPhone;
    @Bind(R.id.zones_textview) TextView mZoneTextView;
    @Bind(R.id.platform_refresh_textview) TextView mPlatformRefreshTextView;
    @Bind(R.id.platform_refresh_imageview) ImageView mPlatformRefreshImageView;
    @Bind(R.id.lines_spinner_arrow) ImageView mLinesSpinnerArrow;
    @Bind(R.id.directMeButton) Button mDirectMeButton;
    @Bind(R.id.pinItButton) Button mPinItButton;
    @Bind(R.id.lines_spinner) Spinner mLinesSpinner;
    @Bind(R.id.station_scrollview) ObservableScrollView mScrollView;
    @Bind(R.id.platform_refresh) RelativeLayout mPlatformRefresh;
    @Bind(R.id.train_prediction_layout) LinearLayout mTrainPredictionLinearLayout;
    @Bind(R.id.train_prediction_header_layout) LinearLayout mTrainPredictionHeader;
    @Bind(R.id.lines_batch_layout) LinesBatchLayout mLinesBatchLayout;
    @Bind(R.id.train_prediction_header_colorbar) View mHeaderColorBar;
    @Bind(R.id.station_swipe_refresh) SwipeRefreshLayout mStationSwipeRefresh;

    private static final int STATION_DATA_LOADER = 1;
    private static String MAP_URL = "http://maps.google.com/maps?saddr=CURLAT,CURLONG&daddr=DESLAT,DESLONG&mode=transit";

    private GoogleMap mMap;
    private Util util;
    private ArrayAdapter mLinesSpinnerAdapter;
    private ColorDrawable actionBarBackground;
    private ObjectAnimator refreshAnimator;
    private ObjectAnimator platformCardAnimator;
    private String stationName;
    private String stationCode;
    private String stationLine;
    private String[] stationLines;
    private String address;
    private String phoneNumber;
    private String zone;
    private GoogleApiClient mGoogleApiClient;
    private int mActionBarAlpha = 0;
    private double latitude, longitude;
    private double currentLatitude, currentLongitude;
    private boolean directionEnabled = false;
    private boolean stationLocationEnabled = false;
    private boolean callFromShortcut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        ButterKnife.bind(this);

        util = Util.getInstance(getApplicationContext());

        if (util.checkPlayServices(this))
            buildGoogleApiClient();

        getIntentData();
        setUpActionBar();
        setLinesSpinner();
        setUpSwipeRefresh();
        setUpAnimators();

        mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mScrollView.setCallBack(this);

        LoaderManager lm = getSupportLoaderManager();
        lm.initLoader(STATION_DATA_LOADER, null, this);
        lm.getLoader(STATION_DATA_LOADER).forceLoad();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_station, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if(callFromShortcut){
                    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(homeIntent);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        directionEnabled = false;
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @OnClick(R.id.station_phone)
    public void callStation(View view)
    {
        util.callDialer(phoneNumber);
    }

    @OnClick(R.id.lines_spinner_arrow)
    public void openStationsPopupSpinner(View view){
        popupSpinner();
    }

    @OnItemSelected(R.id.lines_spinner)
    public void stationSelected(int position){
        String line = (String) mLinesSpinnerAdapter.getItem(position);

        if (!line.isEmpty()) {
            mStationSwipeRefresh.setColorSchemeResources(util.getLineColorResource(line));
            setPlatformLoader();
            loadTrainPrediction(line);
        } else {
            setError();
        }

        setLineColorToViews(line);
        actionBarBackground.setAlpha(mActionBarAlpha);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void setUpActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        actionBarBackground = new ColorDrawable();
        actionBarBackground.setColor(getResources().getColor(util.getLineColorResource(stationLines[0])));
        getSupportActionBar().setBackgroundDrawable(actionBarBackground);
        actionBarBackground.setAlpha(0);
    }

    public void getIntentData() {
        stationName = getIntent().getStringExtra("station");
        stationCode = getIntent().getStringExtra("code");
        stationLine = getIntent().getStringExtra("line");
        callFromShortcut = getIntent().getBooleanExtra("shortcut", false);
        stationLines = stationLine.split(",");
        mLinesBatchLayout.setLines(stationLines);
    }

    public void setLinesSpinner() {
        if (stationLine.isEmpty()) {
            mLinesSpinner.setEnabled(false);
            mLinesSpinnerArrow.setVisibility(View.INVISIBLE);
        }
        mLinesSpinnerArrow.setVisibility(View.VISIBLE);
        mLinesSpinnerAdapter = new ArrayAdapter(getApplicationContext(), R.layout.lines_spinner_item, stationLines);
        mLinesSpinner.setAdapter(mLinesSpinnerAdapter);
        mLinesSpinnerAdapter.setDropDownViewResource(R.layout.lines_spinner_dropdown_item);
        mLinesSpinner.setSelection(0);


    }

    public void setUpAnimators() {
        refreshAnimator = ObjectAnimator.ofFloat(mPlatformRefreshImageView, "rotation", 360);
        refreshAnimator.setDuration(600);
        refreshAnimator.setInterpolator(new LinearInterpolator());
        refreshAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        platformCardAnimator = ObjectAnimator.ofFloat(mTrainPredictionLinearLayout, "alpha", 0.0f, 1.0f);
        platformCardAnimator.setDuration(500);
        platformCardAnimator.setInterpolator(new LinearInterpolator());
    }

    public void popupSpinner() {
        mLinesSpinner.performClick();
    }

    public void setError() {
        mPlatformRefreshTextView.setText(getResources().getString(R.string.departure_not_avail));
    }

    private void setUpSwipeRefresh() {
        mStationSwipeRefresh.setColorSchemeResources(util.getLineColorResource(mLinesSpinner.getSelectedItem().toString()));
        mStationSwipeRefresh.setProgressViewOffset(false, 0, util.getActionBarHeight() + 50);
        mStationSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPlatform();
            }
        });
    }

    public void setLineColorToViews(String line) {
        int lineColor = util.getLineColorResource(line);
        mTrainPredictionHeader.setBackgroundResource(lineColor);
        mHeaderColorBar.setBackgroundResource(lineColor);
        actionBarBackground.setColor(getResources().getColor(lineColor));
        mDirectMeButton.setBackgroundResource(lineColor);
        mPinItButton.setBackgroundResource(lineColor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        StationCursorLoader stationCursorLoader = new StationCursorLoader(getApplicationContext(),
                TubelyDBContract.StationTable.buildStationUriWithName(stationName),
                null,
                null,
                null,
                null);
        return stationCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String lat = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LAT));
            String longt = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LONG));

            if (!lat.equalsIgnoreCase("") && !longt.equalsIgnoreCase("")) {
                try {
                    latitude = Double.parseDouble(lat);
                    longitude = Double.parseDouble(longt);
                    stationLocationEnabled = true;
                } catch (Exception e) {
                    //Log.d(TAG, e.getMessage());
                }
            }

            phoneNumber = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_PHONE));
            address = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_ADDRESS));
            address = address.trim();

            zone = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_ZONES));
            zone = zone.replace(Util.STATION_TABLE_SPLITTER, " ,");

            if (!phoneNumber.equalsIgnoreCase(""))
                mStationPhone.setText(phoneNumber);


            if (!address.equalsIgnoreCase(""))
                mStationAddress.setText(address);

            if (!zone.equalsIgnoreCase(""))
                mZoneTextView.setText(zone);

            setupMap(latitude, longitude);
        } else {
            //Log.d(TAG, "Cursor Empty");
            stationLocationEnabled = false;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void setupMap(double latitude, double longitude) {
        if (stationLocationEnabled) {
            LatLng LOCATION = new LatLng(latitude, longitude);
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.addMarker(new MarkerOptions()
                        .position(LOCATION)
                        .title("Acton Town"));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(LOCATION)      // Sets the center of the map to Mountain View
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        } else {
            Toast.makeText(getApplicationContext(), "Station Location Not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshPlatform()
    {
        String selectedLine = mLinesSpinner.getSelectedItem().toString();
        if (!selectedLine.isEmpty())
            loadTrainPrediction(mLinesSpinner.getSelectedItem().toString());
    }

    @OnClick(R.id.platform_refresh_imageview)
    public void loadTrainsInPlatforms(View view) {
        setPlatformLoader();
        refreshPlatform();
    }

    public void loadTrainPrediction(String lineName) {

        if (!util.isNetworkConnected()) {
            mPlatformRefresh.setVisibility(View.VISIBLE);
            mTrainPredictionLinearLayout.setVisibility(View.GONE);
            mPlatformRefreshTextView.setText("No Internet connection!");
            mStationSwipeRefresh.setRefreshing(false);
            return;
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://cloud.tfl.gov.uk")
                .setConverter(new SimpleXMLConverter())
                .build();

        //restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

        TflWebService tflWebService = restAdapter.create(TflWebService.class);
        String lineCode;
        if (lineName.equalsIgnoreCase("Circle")) {
            lineCode = "H";
        } else {
            lineCode = lineName.substring(0, 1).toUpperCase();
        }
        tflWebService.getTrainPrediction(lineCode, stationCode, new Callback<TrainPrediction>() {
            @Override
            public void success(TrainPrediction trainPrediction, Response response) {
                //Log.d(TAG, "Train Prediction Success : " + response.getStatus() + "---" + response.getReason() );

                refreshAnimator.cancel();
                mStationSwipeRefresh.setRefreshing(false);

                if (trainPrediction == null || null == trainPrediction.getPlatforms() || trainPrediction.getPlatforms().isEmpty()) {
                    mPlatformRefreshTextView.setText("Sorry. No service at this moment!");
                    return;
                }

                mPlatformRefresh.setVisibility(View.INVISIBLE);
                mTrainPredictionLinearLayout.removeAllViewsInLayout();
                mTrainPredictionLinearLayout.setVisibility(View.VISIBLE);

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                for (TrainPlatform platform : trainPrediction.getPlatforms()) {
                    View platformHeader = inflater.inflate(R.layout.card_platform_name, null, false);
                    ((TextView) platformHeader.findViewById(R.id.platform_name)).setText(platform.getDirection());
                    mTrainPredictionLinearLayout.addView(platformHeader);

                    if (null != platform.getTrainList()) {
                        for (Train train : platform.getTrainList()) {
                            View trainView = inflater.inflate(R.layout.card_platform_train, null, false);
                            ((TextView) trainView.findViewById(R.id.destination)).setText(train.getDestination());
                            ((TextView) trainView.findViewById(R.id.current_location)).setText(train.getLocation());

                            //Calculate Seconds to Mins
                            int seconds = Integer.parseInt(train.getSeconds());
                            int minutes = seconds / 60;

                            String timeValue = minutes + " mins ";

                            ((TextView) trainView.findViewById(R.id.time)).setText(timeValue);
                            mTrainPredictionLinearLayout.addView(trainView);

                            TextView line = new TextView(getApplicationContext());
                            line.setHeight(1);
                            line.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            line.setAlpha(0.5f);

                            mTrainPredictionLinearLayout.addView(line);
                        }
                    } else {
                        View trainView = inflater.inflate(R.layout.card_platform_train, null, false);
                        ((TextView) trainView.findViewById(R.id.destination)).setText("No service at this moment!");
                        ((TextView) trainView.findViewById(R.id.time)).setText("");

                        mTrainPredictionLinearLayout.addView(trainView);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                //Log.d(TAG, "Train Prediction Failure"  + error.getMessage());
                mPlatformRefresh.setVisibility(View.VISIBLE);
                mStationSwipeRefresh.setRefreshing(false);
                mTrainPredictionLinearLayout.setVisibility(View.GONE);

                if (null != error.getResponse()) {
                    mPlatformRefreshTextView.setText(error.getResponse().getReason());
                } else {
                    mPlatformRefreshTextView.setText(getResources().getString(R.string.unknown_error));
                }
                refreshAnimator.cancel();
                Toast.makeText(getApplicationContext(), "Sorry! We did our best.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPlatformLoader() {
        mTrainPredictionLinearLayout.setVisibility(View.GONE);
        mPlatformRefresh.setVisibility(View.VISIBLE);
        mPlatformRefreshTextView.setText("Loading " + mLinesSpinner.getSelectedItem().toString() + " trains..");
        refreshAnimator.start();
    }

    public void directToGoogleMap(View view) {
        if (directionEnabled && stationLocationEnabled) {
            String curLat, curLong, desLat, desLong;
            curLat = String.valueOf(currentLatitude);
            curLong = String.valueOf(currentLongitude);
            desLat = String.valueOf(latitude);
            desLong = String.valueOf(longitude);
            MAP_URL = MAP_URL.replace("CURLAT", curLat);
            MAP_URL = MAP_URL.replace("CURLONG", curLong);
            MAP_URL = MAP_URL.replace("DESLAT", desLat);
            MAP_URL = MAP_URL.replace("DESLONG", desLong);

            //Log.d(TAG, MAP_URL);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            mapIntent.setData(Uri.parse(MAP_URL));
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(getApplicationContext(), "Kindly install a browser or Google maps applicaton for directions!", Toast.LENGTH_LONG).show();
            }

        } else {
            if (!stationLocationEnabled)
                Toast.makeText(getApplicationContext(), "Station Location Not available", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Current Location Not Available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();
            directionEnabled = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onScrollChanged(int l, int oldl, int t, int oldt) {
        final int headerHeight = findViewById(R.id.header_card).getHeight() - getSupportActionBar().getHeight();
        final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
        final int newAlpha = (int) (ratio * 255);
        actionBarBackground.setAlpha(newAlpha);

        if (newAlpha > 100)
            getSupportActionBar().setTitle(stationName);
        else
            getSupportActionBar().setTitle("");

        mActionBarAlpha = newAlpha;
    }

    public void pinStationToHome(View view) {
        Intent stationIntent = new Intent(getApplicationContext(), StationActivity.class);
        stationIntent.setAction(Intent.ACTION_MAIN);
        stationIntent.putExtra("station", stationName);
        stationIntent.putExtra("code", stationCode);
        stationIntent.putExtra("line", stationLine);
        stationIntent.putExtra("shortcut", true);

        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, stationIntent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, stationName);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(shortcutIntent);

        Toast.makeText(getApplicationContext(), stationName + " " + getResources().getString(R.string.shortcut_added), Toast.LENGTH_LONG).show();
    }

    private static class StationCursorLoader extends CursorLoader {

        public StationCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver().query(getUri(), getProjection(), getSelection(), getSelectionArgs(), getSortOrder());
        }

        @Override
        public void deliverResult(Cursor cursor) {
            super.deliverResult(cursor);
        }

    }
}