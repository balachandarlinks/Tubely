package in.codeseed.tubely.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by bala on 16/10/14.
 */
public class TubelyContentProvider extends ContentProvider {


    public static final int LINE_STATUS_TYPE = 100;
    public static final int LINE_STATUS_ID = 101;
    public static final int LINE_STATUS = 102;

    public static final int STATION_NAME = 200;
    public static final int STATION_ID = 201;
    public static final int STATION = 202;
    public static final int LINE_STATIONS = 203;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private TubelyDBHelper mOpenHelper;

    public static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TubelyDBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TubelyDBContract.PATH_LINESTATUS + "/*", LINE_STATUS_TYPE);
        matcher.addURI(authority, TubelyDBContract.PATH_LINESTATUS + "/#", LINE_STATUS_ID);
        matcher.addURI(authority, TubelyDBContract.PATH_LINESTATUS, LINE_STATUS);

        matcher.addURI(authority, TubelyDBContract.PATH_STATION + "/NAME" + "/*", STATION_NAME);
        matcher.addURI(authority, TubelyDBContract.PATH_STATION + "/#", STATION_ID );
        matcher.addURI(authority, TubelyDBContract.PATH_STATION, STATION);
        matcher.addURI(authority, TubelyDBContract.PATH_STATION + "/LINESTATIONS" + "/*", LINE_STATIONS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TubelyDBHelper(getContext(), TubelyDBHelper.DATABASE_NAME, null, TubelyDBHelper.DATABASE_VERSION);
        return  true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        Log.d("QUERY", uri + "");
        switch (uriMatcher.match(uri)){
            case LINE_STATUS_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.LineStatusEntry.TABLE_NAME,
                        projection,
                        TubelyDBContract.LineStatusEntry._ID  + "=" + TubelyDBContract.LineStatusEntry.getIdFromLineStatusUri(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                        );
                break;
            case LINE_STATUS_TYPE:
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.LineStatusEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case STATION:
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.StationTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case STATION_NAME :
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.StationTable.TABLE_NAME,
                        projection,
                        TubelyDBContract.StationTable.COLUMN_STATION_NAME + "=" + '"' + TubelyDBContract.StationTable.getStationFromStationUri(uri) + '"',
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case STATION_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.StationTable.TABLE_NAME,
                        projection,
                        TubelyDBContract.StationTable._ID + "=" + TubelyDBContract.StationTable.getIdFromStationUri(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case LINE_STATIONS:
                Log.d("QUERY", "Inside LineStations");
                retCursor = mOpenHelper.getReadableDatabase().query(TubelyDBContract.StationTable.TABLE_NAME,
                        new String[]{TubelyDBContract.StationTable._ID, TubelyDBContract.StationTable.COLUMN_STATION_NAME},
                        TubelyDBContract.StationTable.COLUMN_LINES + " LIKE " + '"' + "%" + TubelyDBContract.StationTable.getLineNameFromLineStationsUri(uri) + "%" + '"',
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Uri didn't match " + uri );
        }

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case LINE_STATUS_TYPE:
                return TubelyDBContract.LineStatusEntry.CONTENT_TYPE;
            case LINE_STATUS_ID:
                return TubelyDBContract.LineStatusEntry.CONTENT_ITEM_TYPE;
            case STATION_ID:
                return TubelyDBContract.StationTable.CONTENT_ITEM_TYPE;
            case STATION_NAME:
                return TubelyDBContract.StationTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("uri didn't match" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase sDb = mOpenHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        final Uri returnUri;
        long _id;
        switch (match){
            case LINE_STATUS:
                _id = sDb.insert(TubelyDBContract.LineStatusEntry.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = TubelyDBContract.LineStatusEntry.buildLineStatusUriWithId(_id);
                }else{
                    throw new SQLException("Cannot insert Data " + uri);
                }
                break;

            case STATION:
                _id = sDb.insert(TubelyDBContract.StationTable.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = TubelyDBContract.StationTable.buildStationUriWithId(_id);
                }else{
                    throw new SQLException("Cannot insert Data " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid Uri " + uri);
        }
        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase sDb = mOpenHelper.getWritableDatabase();
        int rowsAffected;
        switch (uriMatcher.match(uri)){
            case LINE_STATUS:
                rowsAffected = sDb.update(TubelyDBContract.LineStatusEntry.TABLE_NAME,
                        contentValues,
                        TubelyDBContract.LineStatusEntry.COLUMN_LINE_NAME + " = ?",
                        selectionArgs);
                break;
            case STATION_NAME:
                rowsAffected = sDb.update(TubelyDBContract.StationTable.TABLE_NAME,
                        contentValues,
                        TubelyDBContract.StationTable.COLUMN_STATION_NAME + " = ?",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update data " + uri);
        }
        return rowsAffected;
    }
}
