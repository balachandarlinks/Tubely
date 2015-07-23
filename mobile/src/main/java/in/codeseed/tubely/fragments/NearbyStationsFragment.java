package in.codeseed.tubely.fragments;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import in.codeseed.tubely.R;
import in.codeseed.tubely.activities.StationActivity;
import in.codeseed.tubely.customviews.LinesBatchLayout;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.pojos.NearByStation;
import in.codeseed.tubely.simplexml.allstations.Station;
import in.codeseed.tubely.util.Util;

public class NearbyStationsFragment extends Fragment {//implements GooglePlayServicesClient.ConnectionCallbacks,
    //GooglePlayServicesClient.OnConnectionFailedListener {


    private final static String TAG = NearbyStationsFragment.class.getSimpleName();
    private static String MAP_URL = "http://maps.google.com/maps?saddr=CURLAT,CURLONG&daddr=DESLAT,DESLONG&mode=walking";
    private static LayoutInflater layoutInflater;
    private RelativeLayout stationsLoader;
    private ImageView stationsLoaderImageView;
    private TextView stationsLoaderTextView;
    private LinearLayout nearbyStationsLayout;
    private SharedPreferences preferences;

    //private LocationClient locationClient;
    private Location currentLocation;
    private ObjectAnimator mStationsLoaderImageViewanimator;

    public NearbyStationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby_stations, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        injectViews(view);
        mStationsLoaderImageViewanimator = ObjectAnimator.ofFloat(stationsLoaderImageView, "rotation", 360);
        mStationsLoaderImageViewanimator.setRepeatCount(ObjectAnimator.INFINITE);
        mStationsLoaderImageViewanimator.setDuration(600);
        mStationsLoaderImageViewanimator.setInterpolator(new BounceInterpolator());

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        //locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);
        stationsLoaderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAsyncStationsUpdate();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(locationClient.isConnected())
            startAsyncStationsUpdate();*/
    }

    public void injectViews(View rootView){
        layoutInflater = LayoutInflater.from(getActivity().getApplicationContext());
        stationsLoader = (RelativeLayout) rootView.findViewById(R.id.nearby_stations_loader);
        stationsLoaderImageView = (ImageView) stationsLoader.findViewById(R.id.reload_nearby_stations_imageview);
        stationsLoaderTextView = (TextView) stationsLoader.findViewById(R.id.reload_nearby_stations_textview);
        nearbyStationsLayout = (LinearLayout) rootView.findViewById(R.id.nearby_stations);
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
        //locationClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        //locationClient.disconnect();
        super.onStop();
    }

/*    @Override
    public void onConnected(Bundle bundle) {
       // currentLocation = locationClient.getLastLocation();
        //startAsyncStationsUpdate();

        Log.d(TAG, "Location Connected!");
    }*/

    public void startAsyncStationsUpdate(){

        if(currentLocation != null)
            new UpdateNearbyStations().execute();
        else
            setLocationError();
    }

    public void setLocationError(){
        stationsLoaderTextView.setText("Not able to get your current Location. Check your GPS.");
    }

/*    @Override
    public void onDisconnected() {
        Log.d(TAG, "Location Disconnected!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Location Connection Failed!");
    }*/

    private void directToGooglMap(NearByStation station){
        //Location currentLocation = locationClient.getLastLocation();
        /*if(currentLocation != null){
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
                Toast.makeText(getActivity().getApplicationContext(), "Kindly install a browser or Google maps applicaton for directions!", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(getActivity().getApplicationContext(),"Your current location is not available! Check your GPS.", Toast.LENGTH_SHORT).show();
        }*/
    }

    private class UpdateNearbyStations extends AsyncTask<Void, Void, Void>{

        private Cursor allStationsCursor;
        private Location currentStationLocation;
        private ArrayList<NearByStation> nearbyStations = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            nearbyStationsLayout.removeAllViews();
            stationsLoader.setVisibility(View.VISIBLE);
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
                String nearByStationsRadious = preferences.getString(Util.SHARED_PREF_NEARBY_STATIONS_RADIOUS, "1000");
                nearbyStationsLayout.setVisibility(View.INVISIBLE);
                stationsLoader.setVisibility(View.VISIBLE);
                stationsLoaderTextView.setText("No Nearby Stations in " + nearByStationsRadious + " m radius! \n Open Settings to change the radius.");
                if(Util.NEARBY_FRAGMENTS_VISIBLE)
                    Toast.makeText(getActivity().getApplicationContext(), "No nearby stations!", Toast.LENGTH_SHORT).show();
            }else {
                nearbyStationsLayout.setVisibility(View.VISIBLE);
                stationsLoader.setVisibility(View.INVISIBLE);
                nearbyStationsLayout.removeAllViews();
                for (NearByStation station : nearbyStations) {
                    LinearLayout nearbyStationCard = (LinearLayout) layoutInflater.inflate(R.layout.card_nearby_station, null);

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

                    nearbyStationsLayout.addView(nearbyStationCard);
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

                    float distance = currentLocation.distanceTo(currentStationLocation);

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
            String radious = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(Util.SHARED_PREF_NEARBY_STATIONS_RADIOUS, "1000");
            return Integer.parseInt(radious);
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
