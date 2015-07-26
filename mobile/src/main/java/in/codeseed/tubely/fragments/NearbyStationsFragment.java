package in.codeseed.tubely.fragments;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.codeseed.tubely.R;
import in.codeseed.tubely.activities.StationActivity;
import in.codeseed.tubely.customviews.LinesBatchLayout;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.pojos.NearByStation;
import in.codeseed.tubely.simplexml.allstations.Station;
import in.codeseed.tubely.util.Util;

public class NearbyStationsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = NearbyStationsFragment.class.getSimpleName();
    private static String MAP_URL = "http://maps.google.com/maps?saddr=CURLAT,CURLONG&daddr=DESLAT,DESLONG&mode=walking";
    private static LayoutInflater mLayoutInflater;

    @Bind(R.id.nearby_stations_root_layout) FrameLayout mNearbyStationsRootLayout;
    @Bind(R.id.nearby_stations_loader) RelativeLayout mStationsLoader;
    @Bind(R.id.reload_nearby_stations_imageview) ImageView mStationsLoaderImageView;
    @Bind(R.id.reload_nearby_stations_textview) TextView mStationsLoaderTextView;
    @Bind(R.id.nearby_stations) LinearLayout mNearbyStationsLayout;

    private SharedPreferences mPreferences;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private ObjectAnimator mStationsLoaderImageViewanimator;
    private Util mUtil;

    public NearbyStationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUtil = Util.getInstance(getActivity().getApplicationContext());
        mLayoutInflater = LayoutInflater.from(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby_stations, container, false);
        ButterKnife.bind(this, rootView);

        if (mUtil.checkPlayServices(getActivity()))
            buildGoogleApiClient();

        return rootView;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStationsLoaderImageViewanimator = ObjectAnimator.ofFloat(mStationsLoaderImageView, "rotation", 360);
        mStationsLoaderImageViewanimator.setRepeatCount(ObjectAnimator.INFINITE);
        mStationsLoaderImageViewanimator.setDuration(600);
        mStationsLoaderImageViewanimator.setInterpolator(new BounceInterpolator());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }

    @OnClick(R.id.reload_nearby_stations_imageview)
    public void reloadNearbyStations(){
        startAsyncStationsUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            startAsyncStationsUpdate();
        else
            setLocationError();
    }

    public void callDetailedStationActivityIntent(String stationName){

        String stationCode = "", stationLine = "";

        for(Station station : Util.getAllStations().getStations()){
            if(station.getName().equalsIgnoreCase(stationName)){
                stationCode = station.getCode();
                stationLine = station.getLine();
            }
        }

        Intent detailedStationIntent = new Intent(getActivity().getApplicationContext(), StationActivity.class);
        detailedStationIntent.putExtra("station", stationName);
        detailedStationIntent.putExtra("code", stationCode);
        detailedStationIntent.putExtra("line", stationLine);
        startActivity(detailedStationIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startAsyncStationsUpdate();

        Log.d(TAG, "Location Connected!");
    }

    public void startAsyncStationsUpdate(){

        if(mCurrentLocation != null)
            new UpdateNearbyStations().execute();
        else
            setLocationError();
    }

    public void setLocationError(){
        mStationsLoaderTextView.setText("Not able to get your current Location. Check your GPS.");
    }

    private void directToGooglMap(NearByStation station){
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            String curLat, curLong, desLat, desLong;
            curLat = String.valueOf(currentLocation.getLatitude());
            curLong = String.valueOf(currentLocation.getLongitude());
            desLat = String.valueOf(station.getLatitude());
            desLong = String.valueOf(station.getLongitude());
            MAP_URL = MAP_URL.replace("CURLAT", curLat);
            MAP_URL = MAP_URL.replace("CURLONG", curLong);
            MAP_URL = MAP_URL.replace("DESLAT", desLat);
            MAP_URL = MAP_URL.replace("DESLONG", desLong);

            //Log.d(TAG, MAP_URL);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            mapIntent.setData(Uri.parse(MAP_URL));
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }else{
                Snackbar.make(mNearbyStationsRootLayout, "Kindly install a browser or Google maps applicaton for directions", Snackbar.LENGTH_SHORT).show();
            }

        }else{
            Snackbar.make(mNearbyStationsRootLayout, "Your current location is not available! Check your GPS", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class UpdateNearbyStations extends AsyncTask<Void, Void, Void>{

        private Cursor allStationsCursor;
        private Location currentStationLocation;
        private ArrayList<NearByStation> nearbyStations = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            mNearbyStationsLayout.removeAllViews();
            mStationsLoader.setVisibility(View.VISIBLE);
            mStationsLoaderImageViewanimator.start();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getAllStationsCursor();
                getNearByStations(allStationsCursor);
            }catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mStationsLoaderImageViewanimator.cancel();
            nearbyStations = sortNearByStations(nearbyStations);
            if(nearbyStations.isEmpty()){
                String nearByStationsRadious = mPreferences.getString(Util.SHARED_PREF_NEARBY_STATIONS_RADIOUS_IN_MILES, "1");
                mNearbyStationsLayout.setVisibility(View.INVISIBLE);
                mStationsLoader.setVisibility(View.VISIBLE);
                mStationsLoaderTextView.setText("No Nearby Stations in " + nearByStationsRadious + " miles radius! \n Open Settings to change the radius.");
                Snackbar.make(mNearbyStationsRootLayout, "No nearby tube stations found", Snackbar.LENGTH_SHORT).show();
            }else {
                mNearbyStationsLayout.setVisibility(View.VISIBLE);
                mStationsLoader.setVisibility(View.INVISIBLE);
                mNearbyStationsLayout.removeAllViews();
                for (NearByStation station : nearbyStations) {
                    LinearLayout nearbyStationCard = (LinearLayout) mLayoutInflater.inflate(R.layout.card_nearby_station, null);

                    LinearLayout googleMapDirectionButton = (LinearLayout) nearbyStationCard.findViewById(R.id.direction_walk);
                    googleMapDirectionButton.setTag(station);
                    googleMapDirectionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            directToGooglMap((NearByStation)v.getTag());
                        }
                    });

                    LinearLayout nearbyStation = (LinearLayout) nearbyStationCard.findViewById(R.id.nearby_station);
                    nearbyStation.setTag(station.getName());
                    nearbyStation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callDetailedStationActivityIntent((String)v.getTag());
                        }
                    });

                    TextView stationName = (TextView) nearbyStationCard.findViewById(R.id.nearby_station_name);
                    TextView stationDistance = (TextView) nearbyStationCard.findViewById(R.id.nearby_station_distance);
                    LinesBatchLayout linesBatchLayout = (LinesBatchLayout) nearbyStationCard.findViewById(R.id.lines_batch_layout);


                    stationName.setText(station.getName());
                    String[] lines = station.getLines().split(Util.STATION_TABLE_SPLITTER);
                    linesBatchLayout.setLines(lines);
                    stationDistance.setText("~" + String.valueOf((int) station.getDistanceFromCurrentLocation()) + " m");

                    mNearbyStationsLayout.addView(nearbyStationCard);
                }
            }

        }

        private void getAllStationsCursor(){
            allStationsCursor = getActivity().getApplicationContext()
                    .getContentResolver().query(TubelyDBContract.StationTable.CONTENT_URI, null, null, null, null);
        }

        private void getNearByStations(Cursor allStationsCursor){
            if(allStationsCursor.moveToFirst()){
                do{

                    currentStationLocation = new Location("");
                    currentStationLocation.setLatitude(allStationsCursor.getDouble(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LAT)));
                    currentStationLocation.setLongitude(allStationsCursor.getDouble(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LONG)));

                    float distance = mCurrentLocation.distanceTo(currentStationLocation);

                    if(distance < nearbyStationsRadious()){

                        String stationName = allStationsCursor.getString(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_STATION_NAME));
                        String stationLine = allStationsCursor.getString(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LINES));
                        String latitude = allStationsCursor.getString(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LAT));
                        String longitude = allStationsCursor.getString(allStationsCursor.getColumnIndex(TubelyDBContract.StationTable.COLUMN_LONG));

                        NearByStation nearByStation = new NearByStation(stationName, stationLine, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
                        nearbyStations.add(nearByStation);
                    }

                }while (allStationsCursor.moveToNext());
            }else{
                Log.d(TAG, "Empty All Stations Cursor");
            }
        }

        public int nearbyStationsRadious(){
            String radious = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(Util.SHARED_PREF_NEARBY_STATIONS_RADIOUS_IN_MILES, "1");
            return Integer.parseInt(radious) * 1609;
        }

        private ArrayList<NearByStation> sortNearByStations(ArrayList<NearByStation> nearByStations){
            Collections.sort(nearByStations, new Comparator<NearByStation>() {
                @Override
                public int compare(NearByStation lhs, NearByStation rhs) {
                    return lhs.getDistanceFromCurrentLocation() < rhs.getDistanceFromCurrentLocation() ? -1 : (lhs.getDistanceFromCurrentLocation() > rhs.getDistanceFromCurrentLocation() ? 1 : 0);
                }
            });

            return nearByStations;
        }
    }
}