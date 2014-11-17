package in.codeseed.tubely.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import in.codeseed.tubely.data.TubelyDBContract.LineStatusEntry;
import in.codeseed.tubely.data.TubelyDBHelper;

/**
 * Created by bala on 16/10/14.
 */
public class TestDB extends AndroidTestCase {

    public static final String LOG_TAG = TestDB.class.getSimpleName();

    public void test1CreateDb() throws Throwable{
        mContext.deleteDatabase(TubelyDBHelper.DATABASE_NAME);
        SQLiteDatabase sqDB = new TubelyDBHelper(mContext, TubelyDBHelper.DATABASE_NAME, null, TubelyDBHelper.DATABASE_VERSION).getWritableDatabase();
        assertEquals(true, sqDB.isOpen());
        sqDB.close();
    }

    public void test2InsertData() throws Exception{
        ContentValues values = new ContentValues();
        values.put(LineStatusEntry.COLUMN_LINE_NAME, "Piccadilly");
        values.put(LineStatusEntry.COLUMN_CURRENT_STATUS, "Good Service");
        values.put(LineStatusEntry.COLUMN_CURRENT_DESC, "Good Service");
        values.put(LineStatusEntry.COLUMN_WEEKEND_STATUS, "Good Service");
        values.put(LineStatusEntry.COLUMN_WEEKEND_DESC, "Good Service");
        SQLiteDatabase sqDB = new TubelyDBHelper(mContext, TubelyDBHelper.DATABASE_NAME, null, TubelyDBHelper.DATABASE_VERSION).getWritableDatabase();
        long row = sqDB.insert(LineStatusEntry.TABLE_NAME, null, values);
        assertTrue(row != -1);
    }

    public void test3DuplicateInsertData() throws Exception{
        ContentValues values = new ContentValues();
        values.put(LineStatusEntry.COLUMN_LINE_NAME, "Piccadilly");
        values.put(LineStatusEntry.COLUMN_CURRENT_STATUS, "Bad Service");
        values.put(LineStatusEntry.COLUMN_CURRENT_DESC, "Good Service");
        values.put(LineStatusEntry.COLUMN_WEEKEND_STATUS, "Good Service");
        values.put(LineStatusEntry.COLUMN_WEEKEND_DESC, "Good Service");
        SQLiteDatabase sqDB = new TubelyDBHelper(mContext, TubelyDBHelper.DATABASE_NAME, null, TubelyDBHelper.DATABASE_VERSION).getWritableDatabase();
        long row = sqDB.insert(LineStatusEntry.TABLE_NAME, null, values);
        assertTrue(row != -1);
    }

    public void test4ReadData() throws Exception{
        String[] columns = {
                LineStatusEntry._ID,
                LineStatusEntry.COLUMN_LINE_NAME,
                LineStatusEntry.COLUMN_CURRENT_STATUS,
                LineStatusEntry.COLUMN_CURRENT_DESC,
                LineStatusEntry.COLUMN_WEEKEND_STATUS,
                LineStatusEntry.COLUMN_WEEKEND_DESC
        };
        SQLiteDatabase sqDB = new TubelyDBHelper(mContext, TubelyDBHelper.DATABASE_NAME, null, TubelyDBHelper.DATABASE_VERSION).getReadableDatabase();
        Cursor cursor = sqDB.query(LineStatusEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int current_status_index = cursor.getColumnIndex(LineStatusEntry.COLUMN_CURRENT_STATUS);
            String current_status = cursor.getString(current_status_index);

            assertEquals(current_status, "Good Service");
        }else{
            fail("No values returned!");
        }
    }
}
