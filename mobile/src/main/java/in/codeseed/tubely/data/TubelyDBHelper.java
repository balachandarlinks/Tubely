package in.codeseed.tubely.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.codeseed.tubely.R;
import in.codeseed.tubely.data.TubelyDBContract.LineStatusEntry;
import in.codeseed.tubely.data.TubelyDBContract.StationTable;
/**
 * Created by bala on 16/10/14.
 */
public class TubelyDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "tubely.db";
    public static final int DATABASE_VERSION = 2;
    private static Context mContext;

    public TubelyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_LINE_STATUS_TABLE = "CREATE TABLE " + LineStatusEntry.TABLE_NAME + " (" +
                LineStatusEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                //LineStatus Table Columns
                LineStatusEntry.COLUMN_LINE_NAME + " TEXT NOT NULL," +
                LineStatusEntry.COLUMN_CURRENT_STATUS + " TEXT," +
                LineStatusEntry.COLUMN_CURRENT_DESC + " TEXT," +
                LineStatusEntry.COLUMN_WEEKEND_STATUS + " TEXT," +
                LineStatusEntry.COLUMN_WEEKEND_DESC + " TEXT," +

                //Make line_name unique
                "UNIQUE(" + LineStatusEntry.COLUMN_LINE_NAME + ") ON CONFLICT REPLACE );";

                sqLiteDatabase.execSQL(CREATE_LINE_STATUS_TABLE);

        final String CREATE_STATION_TABLE = "CREATE TABLE " + StationTable.TABLE_NAME + " (" +
                StationTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                StationTable.COLUMN_STATION_NAME + " TEXT NOT NULL," +
                StationTable.COLUMN_LAT + " TEXT," +
                StationTable.COLUMN_LONG + " TEXT," +
                StationTable.COLUMN_ADDRESS + " TEXT," +
                StationTable.COLUMN_PHONE + " TEXT," +
                StationTable.COLUMN_LINES + " TEXT," +
                StationTable.COLUMN_ZONES + " TEXT," +
                StationTable.COLUMN_FACILITIES + " TEXT," +

                "UNIQUE(" + StationTable.COLUMN_STATION_NAME + ") ON CONFLICT REPLACE );";


                sqLiteDatabase.execSQL(CREATE_STATION_TABLE);

                initializeLineStatusTable(sqLiteDatabase);
    }

    public void initializeLineStatusTable(SQLiteDatabase sqLiteDatabase){
        String[] tubeLines = mContext.getResources().getStringArray(R.array.tubeLines);
        for(String tubeName : tubeLines){
            ContentValues value = new ContentValues();
            value.put(LineStatusEntry.COLUMN_LINE_NAME, tubeName);
            value.put(LineStatusEntry.COLUMN_CURRENT_STATUS, "");
            value.put(LineStatusEntry.COLUMN_CURRENT_DESC, "");
            value.put(LineStatusEntry.COLUMN_WEEKEND_STATUS, "");
            value.put(LineStatusEntry.COLUMN_WEEKEND_DESC, "");
            sqLiteDatabase.insert(LineStatusEntry.TABLE_NAME, null, value);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LineStatusEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StationTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
