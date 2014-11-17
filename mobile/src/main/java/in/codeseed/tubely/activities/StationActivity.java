package in.codeseed.tubely.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.codeseed.tubely.R;
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

public class StationActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final String TAG = StationActivity.class.getSimpleName();
    private String MAP_URL = "http://maps.google.com/maps?saddr=CURLAT,CURLONG&daddr=DESLAT,DESLONG&mode=transit";

    private GoogleMap mMap;
    private TextView stationPhone, stationAddress, zoneTextView, lineTextView, platformRefreshTextView;
    private CardView trainPredictionCard;
    private ImageView platformRefresh;
    private LinearLayout trainPredictionLinearLayout;
    private String stationName;
    private String stationCode;
    private String stationLine;
    private String address;
    private String phone;
    private double latitude, longitude;
    private String zone;
    private String line;
    private LocationClient mLocationClient;
    private boolean directionEnabled = false;
    private boolean stationLocationEnabled = false;
    private double currentLatitude, currentLongitude;

    private ObjectAnimator refreshAnimator;
    private ObjectAnimator platformCardAnimator;
    private ValueAnimator platformCardValueAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        stationPhone = (TextView) findViewById(R.id.station_phone);
        stationAddress = (TextView) findViewById(R.id.station_address);
        zoneTextView = (TextView) findViewById(R.id.zones_textview);
        lineTextView = (TextView) findViewById(R.id.lines_textview);
        platformRefreshTextView = (TextView) findViewById(R.id.platform_refresh_textview);

        trainPredictionLinearLayout = (LinearLayout) findViewById(R.id.train_prediction_linear_layout);
        platformRefresh = (ImageView) findViewById(R.id.platform_refresh);

        refreshAnimator = ObjectAnimator.ofFloat(platformRefresh, "rotation", 360);
        refreshAnimator.setDuration(600);
        refreshAnimator.setInterpolator(new LinearInterpolator());
        refreshAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        platformCardAnimator = ObjectAnimator.ofFloat(trainPredictionLinearLayout, "alpha", 0.0f, 1.0f);
        platformCardAnimator.setDuration(500);
        platformCardAnimator.setInterpolator(new LinearInterpolator());

        stationPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDialer(v);
            }
        });

        stationName = getIntent().getStringExtra("station");
        stationCode = getIntent().getStringExtra("code");
        stationLine = getIntent().getStringExtra("line");

        mLocationClient = new LocationClient(this, this, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(stationName);

        LoaderManager lm = getSupportLoaderManager();
        lm.initLoader(1, null, this);

        lm.getLoader(1).forceLoad();

        loadTrainPrediction();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        directionEnabled = false;
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Toast.makeText(getApplicationContext(), "Got search intent", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_station, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        if(cursor.moveToFirst()){
            String lat = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LAT));
            String longt = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LONG));

            if(!lat.equalsIgnoreCase("") && !longt.equalsIgnoreCase("")) {
                try {
                    latitude = Double.parseDouble(lat);
                    longitude = Double.parseDouble(longt);
                    stationLocationEnabled = true;
                }catch (Exception e){
                    //Log.d(TAG, e.getMessage());
                }
            }

            phone = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_PHONE));
            address = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_ADDRESS));

            zone = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_ZONES));
            zone = zone.replace(Util.STATION_TABLE_SPLITTER, " ,");

            line = cursor.getString(cursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LINES));
            line = line.replace(Util.STATION_TABLE_SPLITTER, " ,");

            if(!phone.equalsIgnoreCase(""))
                stationPhone.setText(phone);

            if(!address.equalsIgnoreCase(""))
                stationAddress.setText(address);

            if(!zone.equalsIgnoreCase(""))
                zoneTextView.setText(zone);

            if(!line.equalsIgnoreCase(""))
                lineTextView.setText(line);

            setupMap(latitude, longitude);
        }else{
            //Log.d(TAG, "Cursor Empty");
            stationLocationEnabled = false;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void setupMap(double latitude, double longitude) {
        if(stationLocationEnabled) {
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
        }else{
            Toast.makeText(getApplicationContext(),"Station Location Not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadTrainsInPlatforms(View view){
        loadTrainPrediction();
    }

    boolean isNetworkConnected(){
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void loadTrainPrediction(){

        if(!isNetworkConnected()){
            platformRefreshTextView.setText("No Internet connection!");
            return;
        }

        refreshAnimator.start();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://cloud.tfl.gov.uk")
                .setConverter(new SimpleXMLConverter())
                .build();

        restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

        TflWebService tflWebService = restAdapter.create(TflWebService.class);
        tflWebService.getTrainPrediction(stationLine.substring(0,1).toUpperCase(), stationCode , new Callback<TrainPrediction>() {
            @Override
            public void success(TrainPrediction trainPrediction, Response response) {
                //Log.d(TAG, "Train Prediction Success : " + response.getStatus() + "---" + response.getReason() );

                refreshAnimator.cancel();
                platformRefresh.setVisibility(View.GONE);

                if(trainPrediction == null || null == trainPrediction.getPlatforms() || trainPrediction.getPlatforms().isEmpty()){
                    platformRefreshTextView.setText("Sorry. No trains are available at this moment!");
                    return;
                }

                platformRefreshTextView.setVisibility(View.GONE);

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                for(TrainPlatform platform : trainPrediction.getPlatforms()){
                    View platformHeader = inflater.inflate(R.layout.card_platform_name, null, false);
                    ((TextView)platformHeader.findViewById(R.id.platform_name)).setText(platform.getDirection());
                    trainPredictionLinearLayout.addView(platformHeader);

                    if(null != platform.getTrainList()) {
                        for (Train train : platform.getTrainList()) {
                            View trainView = inflater.inflate(R.layout.card_platform_train, null, false);
                            ((TextView) trainView.findViewById(R.id.destination)).setText(train.getDestination());
                            ((TextView) trainView.findViewById(R.id.current_location)).setText(train.getLocation());

                            //Calculate Seconds to Mins
                            int seconds = Integer.parseInt(train.getSeconds());
                            int minutes = seconds / 60;

                            String timeValue = minutes + " mins ";

                            ((TextView) trainView.findViewById(R.id.time)).setText(timeValue);
                            trainPredictionLinearLayout.addView(trainView);

                            TextView line = new TextView(getApplicationContext());
                            line.setHeight(1);
                            line.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            line.setAlpha(0.5f);

                            trainPredictionLinearLayout.addView(line);
                        }
                    }else {
                        View trainView = inflater.inflate(R.layout.card_platform_train, null, false);
                        ((TextView) trainView.findViewById(R.id.destination)).setText("No trains for this platform.");
                        ((TextView) trainView.findViewById(R.id.time)).setText("");

                        trainPredictionLinearLayout.addView(trainView);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                //Log.d(TAG, "Train Prediction Failure"  + error.getMessage());
                platformRefreshTextView.setText("Failed to load. Tap to refresh!");
                refreshAnimator.cancel();
                Toast.makeText(getApplicationContext(), "Failed to load!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ValueAnimator getPlatformCardValueAnimator(int start, int end){
        ValueAnimator anim = ValueAnimator.ofInt(start, end);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                trainPredictionLinearLayout.getLayoutParams().height = val;
                trainPredictionLinearLayout.requestLayout();
                //ViewGroup.LayoutParams layoutParams = trainPredictionLinearLayout.getLayoutParams();
                //layoutParams.height = val;
                //trainPredictionLinearLayout.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(1000);
        return anim;
    }

    private void callDialer(View view){
        if(phone.equalsIgnoreCase("")){
            Toast.makeText(getApplicationContext(), "Phone number not available!", Toast.LENGTH_SHORT).show();
        }else {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));

            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
            }else{
                Toast.makeText(getApplicationContext(), "Kindly install a dialer to make calls!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void directToGoogleMap(View view){
        if(directionEnabled && stationLocationEnabled){
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
            }else{
                Toast.makeText(getApplicationContext(), "Kindly install a browser or Google maps applicaton for directions!", Toast.LENGTH_LONG).show();
            }

        }else{
            if(!stationLocationEnabled)
                Toast.makeText(getApplicationContext(),"Station Location Not available", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Current Location Not Available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location currentLocation = mLocationClient.getLastLocation();
        if(currentLocation != null) {
            currentLatitude = currentLocation.getLatitude();
            currentLongitude = currentLocation.getLongitude();
            directionEnabled = true;
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private static class StationCursorLoader extends CursorLoader{

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
