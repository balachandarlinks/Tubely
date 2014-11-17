package in.codeseed.tubely.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import in.codeseed.tubely.data.TubelyDBContract;

/**
 * Created by bala on 23/10/14.
 */

public class WeekendTubeStatusLoader extends CursorLoader {

    private static final String TAG = WeekendTubeStatusLoader.class.getSimpleName();

    private WeekendTubeStatusObserver contentObserver;

    public WeekendTubeStatusLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        contentObserver = new WeekendTubeStatusObserver(new Handler());
    }

    @Override
    protected void onStartLoading() {
        getContext().getContentResolver().registerContentObserver(TubelyDBContract.LineStatusEntry.CONTENT_URI.buildUpon().appendPath("weekend").build(),
                false,
                contentObserver);
    }


    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(getUri(), getProjection(), getSelection(), getSelectionArgs(), getSortOrder());
        return cursor;
    }

    @Override
    public void deliverResult(Cursor cursor) {
        // Deliver the result cursor to the loaderManger callback.
        super.deliverResult(cursor);
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        getContext().getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    private class WeekendTubeStatusObserver extends ContentObserver{

        private final String TAG = WeekendTubeStatusObserver.class.getSimpleName();

        public WeekendTubeStatusObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onContentChanged();
            Log.d(TAG, "ContentChangeObserved at " + uri);
        }

    }

}
