package in.codeseed.tubely.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by bala on 16/10/14.
 */
public class TubelyDBContract {


    public static final String CONTENT_AUTHORITY = "in.codeseed.tubely";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LINESTATUS = "linestatus";

    public static final String PATH_STATION = "station";

    public static final class LineStatusEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINESTATUS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir" + CONTENT_AUTHORITY + "/"
                + PATH_LINESTATUS;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item" + CONTENT_AUTHORITY + "/"
                        + PATH_LINESTATUS;

        public static final String TABLE_NAME = "linestatus";

        public static final String COLUMN_LINE_NAME = "line_name";
        public static final String COLUMN_CURRENT_STATUS = "current_status";
        public static final String COLUMN_CURRENT_DESC = "current_desc";
        public static final String COLUMN_WEEKEND_STATUS = "weekend_status";
        public static final String COLUMN_WEEKEND_DESC = "weekend_desc";

        public static final String[] projection_current = {
            _ID,
            COLUMN_LINE_NAME,
            COLUMN_CURRENT_STATUS,
            COLUMN_CURRENT_DESC
        };

        public static final String[] projection_weekend = {
            _ID,
            COLUMN_LINE_NAME,
            COLUMN_WEEKEND_STATUS,
            COLUMN_WEEKEND_DESC
        };

        public static Uri buildLineStatusUriWithId(long _id){
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static Uri buildLineStatusUri(String type){
            return CONTENT_URI.buildUpon().appendPath(type).build();
        }

        public static String getTypeFromLineStatusUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static long getIdFromLineStatusUri(Uri uri){
            return ContentUris.parseId(uri);
        }

    }

    public static final class StationTable implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir" + CONTENT_AUTHORITY + "/"
                        + PATH_STATION;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item" + CONTENT_AUTHORITY + "/"
                        + PATH_STATION;

        public static final String TABLE_NAME = "station";

        public static final String COLUMN_STATION_NAME = "station_name";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LONG = "long";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_LINES = "lines";
        public static final String COLUMN_FACILITIES = "facilities";
        public static final String COLUMN_ZONES = "zones";

        public static Uri buildStationUriWithId(long _id){
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static long getIdFromStationUri(Uri uri){
            return ContentUris.parseId(uri);
        }


        public static Uri buildStationUriWithName(String name){
            return CONTENT_URI.buildUpon().appendPath("NAME").appendPath(name).build();
        }

        public static String getStationFromStationUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static Uri buildLineStationsUriWithLine(String line){
            return CONTENT_URI.buildUpon().appendPath("LINESTATIONS").appendPath(line).build();
        }

        public static String getLineNameFromLineStationsUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

    }
}