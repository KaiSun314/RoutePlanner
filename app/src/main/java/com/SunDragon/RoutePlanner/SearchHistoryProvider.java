package com.SunDragon.RoutePlanner;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.SunDragon.RoutePlanner.SearchHistoryContract.SearchHistoryEntry;

public class SearchHistoryProvider extends ContentProvider {

    private SearchHistoryDBHelper mSearchHistoryDBHelper;

    @Override
    public boolean onCreate() {
        mSearchHistoryDBHelper = new SearchHistoryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        final SQLiteDatabase db = mSearchHistoryDBHelper.getReadableDatabase();
        if (selectionArgs == null) {
            return db.query(
                    SearchHistoryEntry.TABLE_NAME,
                    SearchHistoryEntry.PROJECTION_SEARCH_HISTORY,
                    null,
                    null,
                    null,
                    null,
                    SearchHistoryEntry.TIMESTAMP + " DESC"
            );
        }
        if (selectionArgs[0].length() == 0) {
            return null;
        }
        return db.query(
                SearchHistoryEntry.TABLE_NAME,
                SearchHistoryEntry.PROJECTION_SEARCH_HISTORY,
                SearchHistoryEntry.SEARCH_QUERY + " LIKE ?",
                new String[] { "%" + selectionArgs[0] + "%" },
                null,
                null,
                SearchHistoryEntry.TIMESTAMP + " DESC",
                MapsActivity.NUM_RESULTS_RETURNED);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues values) {
        final SQLiteDatabase db = mSearchHistoryDBHelper.getWritableDatabase();
        db.delete(
                SearchHistoryEntry.TABLE_NAME,
                SearchHistoryEntry.SEARCH_QUERY + "=?",
                new String[] { values.getAsString(SearchHistoryEntry.SEARCH_QUERY) });
        long _id = db.insert(
                SearchHistoryEntry.TABLE_NAME,
                null,
                values);

        if (_id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return SearchHistoryEntry.CONTENT_URI;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mSearchHistoryDBHelper.getWritableDatabase();
        return db.delete(
                SearchHistoryEntry.TABLE_NAME,
                null,
                null);
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
