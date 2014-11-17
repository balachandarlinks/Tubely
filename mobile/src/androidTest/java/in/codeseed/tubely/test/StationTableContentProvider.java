package in.codeseed.tubely.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.data.TubelyDBHelper;

/**
 * Created by bala on 30/10/14.
 */
public class StationTableContentProvider extends AndroidTestCase {

    public void test1DBClear() throws Exception{
        mContext.deleteDatabase(TubelyDBHelper.DATABASE_NAME);
    }

    public void test2StationNameContentType() throws Exception{
        String type = mContext.getContentResolver().getType(TubelyDBContract.StationTable.buildStationUriWithName("Acton Town"));
        assertEquals(TubelyDBContract.StationTable.CONTENT_ITEM_TYPE, type);
    }

    public void test2GetStationNameFromUri() throws Exception{
        Uri uri = TubelyDBContract.StationTable.buildStationUriWithName("Acton Town");
        String station = TubelyDBContract.StationTable.getStationFromStationUri(uri);
        assertEquals("Acton Town", station);
    }

    public void test4InsertData() throws Exception{
        ContentValues values = new ContentValues();
        values.put(TubelyDBContract.StationTable.COLUMN_STATION_NAME, "Acton Town");
        values.put(TubelyDBContract.StationTable.COLUMN_ADDRESS, "address");
        values.put(TubelyDBContract.StationTable.COLUMN_PHONE, "9008174697");
        values.put(TubelyDBContract.StationTable.COLUMN_ZONES,"3,4");
        values.put(TubelyDBContract.StationTable.COLUMN_FACILITIES, "0,1");
        values.put(TubelyDBContract.StationTable.COLUMN_LINES,"Piccadilly");
        values.put(TubelyDBContract.StationTable.COLUMN_LAT,"-0.0000111");
        values.put(TubelyDBContract.StationTable.COLUMN_LONG, "0.21221");

        Uri uri = mContext.getContentResolver().insert(TubelyDBContract.StationTable.CONTENT_URI, values);

        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        assertEquals(1, cursor.getCount());
    }

    public void test5GetStationsForALine() throws Exception{
        Cursor cursor = mContext.getContentResolver().query(TubelyDBContract.StationTable.buildLineStationsUriWithLine("Piccadilly"),
                null,
                null,
                null,
                null);

        assertTrue(cursor.getCount() > 0);
    }

}
