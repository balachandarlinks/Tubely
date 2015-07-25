package in.codeseed.tubely.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.parsers.TflWebService;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.simplexml.allstations.AllStations;
import in.codeseed.tubely.simplexml.currentstatus.LineStatus;
import in.codeseed.tubely.simplexml.currentstatus.LineStatusList;
import in.codeseed.tubely.simplexml.facilities.Root;
import in.codeseed.tubely.simplexml.facilities.Station;
import in.codeseed.tubely.simplexml.weekendstatus.Line;
import in.codeseed.tubely.simplexml.weekendstatus.WeekendLineStatusList;
import in.codeseed.tubely.util.Util;
import retrofit.RestAdapter;
import retrofit.converter.SimpleXMLConverter;

public class DownloadTubeStatusIntentService extends IntentService {

    private static final String TAG = DownloadTubeStatusIntentService.class.getSimpleName();

    private static final String ACTION_TUBESTATUS_CURRENT = "in.codeseed.tubely.service.action.TUBESTATUS_CURRENT";
    private static final String ACTION_TUBESTATUS_WEEKEND = "in.codeseed.tubely.service.action.TUBESTATUS_WEEKEND";
    private static final String ACTION_STATION_FACILITIES = "in.codeseed.tubely.service.action.STATION_FACILITIES";
    private static final String ACTION_LOAD_STATIONS_DATA = "in.codeseed.tubely.service.action.LOAD_STATIONS_DATA";

    public DownloadTubeStatusIntentService() {
        super("DownloadTubeStatusIntentService");
    }

    public static void startActionTubeStatusCurrent(Context context) {
        Intent intent = new Intent(context, DownloadTubeStatusIntentService.class);
        intent.setAction(ACTION_TUBESTATUS_CURRENT);
        context.startService(intent);
    }

    public static void startActionTubeStatusWeekend(Context context) {
        Intent intent = new Intent(context, DownloadTubeStatusIntentService.class);
        intent.setAction(ACTION_TUBESTATUS_WEEKEND);
        context.startService(intent);
    }

    public static void startActionStationFacilities(Context context){
        Intent intent = new Intent(context, DownloadTubeStatusIntentService.class);
        intent.setAction(ACTION_STATION_FACILITIES);
        context.startService(intent);
    }

    public static void startActionLoadStationsData(Context context){
        Intent intent = new Intent(context, DownloadTubeStatusIntentService.class);
        intent.setAction(ACTION_LOAD_STATIONS_DATA);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_TUBESTATUS_CURRENT:
                    updateCurrentTubeStatusAction();

                    break;
                case ACTION_TUBESTATUS_WEEKEND:
                    updateWeekendTubeStatusAction();

                    break;
                case ACTION_STATION_FACILITIES:
                    updateStationDetails();

                    break;
                case ACTION_LOAD_STATIONS_DATA:
                    loadStationsDataFromXML();
                    break;
            }
        }
    }

    private void updateCurrentTubeStatusAction() {

        List<Tube> tubeList = new ArrayList<>();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new SimpleXMLConverter())
                .setEndpoint("http://cloud.tfl.gov.uk")
                .build();

        //restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

        TflWebService tflWebService = restAdapter.create(TflWebService.class);
        try {
            LineStatusList lineStatusList = tflWebService.getCurrentLineStatusList();
            for(LineStatus lineStatus : lineStatusList.getLineStatuses()){
                Tube tube = new Tube();
                tube.setName(lineStatus.getName());
                tube.setStatus(lineStatus.getDescription());
                tube.setExtraInformation(lineStatus.getExtraInformation());
                tubeList.add(tube);
            }
            //Log.d("Station Name", "Current Line Status Fetched");
        }catch (Exception e){
            Log.d("Current Line Status Retro Exception", String.valueOf(e.getMessage()));
        }

        if(!tubeList.isEmpty())
            bulkUpdateLineStatus(tubeList);

    }

    private void updateWeekendTubeStatusAction() {

        List<Tube> tubeList = new ArrayList<>();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new SimpleXMLConverter())
                .setEndpoint("https://data.tfl.gov.uk")
                .build();

        //restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

        TflWebService tflWebService = restAdapter.create(TflWebService.class);
        try {
            WeekendLineStatusList weekendLineStatusList = tflWebService.getWeekendLineStatusList();
            for(Line line : weekendLineStatusList.getLines()){
                Tube tube = new Tube();
                tube.setName(line.getName());
                tube.setStatus(line.getStatus());
                tube.setExtraInformation(line.getExtraInformation());
                tubeList.add(tube);
            }
            Log.d("Weekend Station Name", "Weekend Line Status Fetched");
        }catch (Exception e){
            Log.d("Weekend Line Status Retro Exception", String.valueOf(e.getMessage()));
        }

        if(!tubeList.isEmpty())
            bulkUpdateLineWeekendStatus(tubeList);
    }

    private void updateStationDetails() {

        Serializer serializer = new Persister();
        try {
            InputStream stationsInputStream = getResources().getAssets().open("stations_facilities.xml");
            Root root = serializer.read(Root.class, stationsInputStream);
            if(root != null && root.getStations() != null)
                bulkInsertStationFacilties(root.getStations());
        }catch(Exception exception){
            Log.d(TAG, exception.getMessage());
        }

    }

    void loadStationsDataFromXML(){

        Serializer serializer = new Persister();
        try {
            InputStream allStationsInputStream = getResources().getAssets().open("all_stations.xml");
            Util.setAllStations(serializer.read(AllStations.class, allStationsInputStream));
        }catch(Exception exception){
            Log.d(TAG, exception.getMessage());
        }
    }

    void bulkUpdateLineStatus(List<Tube> tubeList){

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for(Tube tube: tubeList){
            ContentValues value = new ContentValues();
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME, tube.getName());
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_STATUS, tube.getStatus());
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_DESC, tube.getExtraInformation());
            ops.add(ContentProviderOperation.newUpdate(TubelyDBContract.LineStatusEntry.CONTENT_URI)
                    .withValues(value)
                    .withSelection("", new String[]{tube.getName()})
                    .build());
        }
        try {
            getContentResolver().applyBatch(TubelyDBContract.CONTENT_AUTHORITY, ops);
            getContentResolver().notifyChange(TubelyDBContract.LineStatusEntry.CONTENT_URI.buildUpon().appendPath("current").build(), null);
            updateLastUpdatedTime(Util.SHARED_PREF_TUBESTATUS_CURRENT);
            Log.d(TAG, "DB Updated with Current Tube Status!");
        }catch(RemoteException | OperationApplicationException exception){
            Log.d(TAG, exception.getMessage());
        }
    }

    void bulkUpdateLineWeekendStatus(List<Tube> tubeList){

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for(Tube tube: tubeList){
            ContentValues value = new ContentValues();
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME, tube.getName());
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_STATUS, tube.getStatus());
            value.put(TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_DESC, tube.getExtraInformation());
            ops.add(ContentProviderOperation.newUpdate(TubelyDBContract.LineStatusEntry.CONTENT_URI)
                    .withValues(value)
                    .withSelection("", new String[]{tube.getName()})
                    .build());
        }
        try {

            getContentResolver().applyBatch(TubelyDBContract.CONTENT_AUTHORITY, ops);
            getContentResolver().notifyChange(TubelyDBContract.LineStatusEntry.CONTENT_URI.buildUpon().appendPath("weekend").build(), null);
            updateLastUpdatedTime(Util.SHARED_PREF_TUBESTATUS_WEEKEND);
            Log.d(TAG, "DB Updated with Weekend Tube Status!");
        }catch(RemoteException remoteException){
            Log.e(TAG, remoteException.getMessage());
        }catch(OperationApplicationException operationApplicationException){
            Log.d(TAG, operationApplicationException.getMessage());
        }
    }

    void bulkInsertStationFacilties(List<Station> stations){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for(Station station : stations){
            ContentValues values = new ContentValues();
            values.put(TubelyDBContract.StationTable.COLUMN_STATION_NAME, station.getName());

            String[] latLong = station.getPlacemark().getPoint().getCoordinates().split(",");
            values.put(TubelyDBContract.StationTable.COLUMN_LAT, latLong[1]);
            values.put(TubelyDBContract.StationTable.COLUMN_LONG, latLong[0]);

            String servingLines = "";
            for(String line : station.getServingLines()){
                if(servingLines.equalsIgnoreCase(""))
                    servingLines = line;
                else
                    servingLines = servingLines + Util.STATION_TABLE_SPLITTER + line;
            }
            values.put(TubelyDBContract.StationTable.COLUMN_LINES, servingLines);

            String zones = "";
            for(String zone : station.getZones()){
                if(zones.equalsIgnoreCase(""))
                    zones = zone;
                else
                    zones = zones + Util.STATION_TABLE_SPLITTER + zone;
            }
            values.put(TubelyDBContract.StationTable.COLUMN_ZONES, zones);

            values.put(TubelyDBContract.StationTable.COLUMN_ADDRESS, station.getContactDetails().getAddress());
            values.put(TubelyDBContract.StationTable.COLUMN_PHONE, station.getContactDetails().getPhone());
            ops.add(ContentProviderOperation.newInsert(TubelyDBContract.StationTable.CONTENT_URI)
                    .withValues(values)
                    .build());

            SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            preferenceEditor.putInt(Util.SHARED_PREF_DB_UPDATE_CODE, getResources().getInteger(R.integer.db_code));
            preferenceEditor.apply();
        }
        try{
            getContentResolver().applyBatch(TubelyDBContract.CONTENT_AUTHORITY, ops);
            //updateLastUpdatedTime(Util.SHARED_PREF_STATION_FACILITIES);
            Log.d(TAG, "Station Facilties Updated in DB");
        }catch(RemoteException | OperationApplicationException exception){
            Log.d(TAG, exception.getMessage());
        }
    }

    void updateLastUpdatedTime(String key){

        SharedPreferences.Editor preferenceEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        preferenceEditor.putLong(key, new Date().getTime());
        preferenceEditor.apply();
    }


}

