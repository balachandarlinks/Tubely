package in.codeseed.tubely.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import in.codeseed.tubely.data.TubelyDBContract;
import in.codeseed.tubely.data.TubelyDBHelper;

/**
 * Created by bala on 16/10/14.
 */
public class TestProviderDB extends AndroidTestCase {

    public static final String LOG_TAG = TestProviderDB.class.getSimpleName();
    public static Uri uri;

    public static final String[] projections_current = {
            TubelyDBContract.LineStatusEntry._ID,
            TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME,
            TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_STATUS,
            TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_DESC
    };
    public static final String[] projections_weekend = {
            TubelyDBContract.LineStatusEntry._ID,
            TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME,
            TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_STATUS,
            TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_DESC
    };

    public void test1ClearDb() throws Throwable{
        mContext.deleteDatabase(TubelyDBHelper.DATABASE_NAME);
    }

    public void test2GetContentUriType() throws Throwable{
        String type = mContext.getContentResolver().getType(TubelyDBContract.LineStatusEntry.buildLineStatusUri("current"));
        assertEquals(type, TubelyDBContract.LineStatusEntry.CONTENT_TYPE);
    }

    public void test3InsertCurrentData() throws Exception{
        ContentValues values = new ContentValues();
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME, "Piccadilly");
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_STATUS, "Current Status");
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_DESC, "Current Desc");

        uri = mContext.getContentResolver().insert(TubelyDBContract.LineStatusEntry.CONTENT_URI, values);

        Cursor cursor = mContext.getContentResolver().query(uri,
                projections_current,
                null,
                null,
                null);

        assertTrue(cursor.getCount() == 1);

        cursor.close();

    }

    public void test4InsertWeekendData() throws Throwable{
        ContentValues values = new ContentValues();
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME, "Piccadilly");
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_STATUS, "Weekend Status");
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_WEEKEND_DESC, "Weekend Desc");

        uri = mContext.getContentResolver().insert(TubelyDBContract.LineStatusEntry.CONTENT_URI, values);

        Cursor cursor = mContext.getContentResolver().query(uri,
                projections_current,
                null,
                null,
                null);

        assertTrue(cursor.getCount() == 1);

        cursor.close();

    }


    /*public void test4UpdateData() throws Exception{
        ContentValues values = new ContentValues();
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_STATUS, "Good Service");
        values.put(TubelyDBContract.LineStatusEntry.COLUMN_CURRENT_DESC, "");

        String[] selectionArgs = {"Piccadilly"};

        int rowsAffected = mContext.getContentResolver().update(TubelyDBContract.LineStatusEntry.CONTENT_URI, values, null, selectionArgs );

        assertEquals(1 , rowsAffected);

    }*/
}
