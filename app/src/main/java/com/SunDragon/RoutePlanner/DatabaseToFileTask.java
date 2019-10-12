package com.SunDragon.RoutePlanner;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.io.FileOutputStream;

public class DatabaseToFileTask extends AsyncTask<Void, Void, Void> {

    private static final int MAX_DATABASE_ROWS = 1000;
    public static final String NULL = "null";

    private MapsActivity mActivity;

    public DatabaseToFileTask(MapsActivity activity) {
        mActivity = activity;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Cursor cursor = mActivity.getContentResolver().query(
                SearchHistoryContract.SearchHistoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.getCount() >= MAX_DATABASE_ROWS) {
                StringBuilder searchHistoryStringBuilder = new StringBuilder();
                StringBuilder placeIdStringBuilder = new StringBuilder();
                while (cursor.moveToNext()) {
                    searchHistoryStringBuilder.append(
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                    SearchHistoryContract.SearchHistoryEntry.SEARCH_QUERY)));
                    searchHistoryStringBuilder.append("\n");

                    String placeId = cursor.getString(cursor.getColumnIndexOrThrow(
                            SearchHistoryContract.SearchHistoryEntry.PLACE_ID));
                    placeIdStringBuilder.append((placeId == null ? NULL : placeId));
                    placeIdStringBuilder.append("\n");
                }

                for (int i=0; i<MapsActivity.mSearchHistoryRegex.length; i++) {
                    if (searchHistoryStringBuilder.toString().length() +
                            MapsActivity.mSearchHistoryRegex[i].length() > MapsActivity.MAX_CHAR
                            && placeIdStringBuilder.toString().length() +
                            MapsActivity.mPlaceIdRegex[i].length() > MapsActivity.MAX_CHAR) break;
                    searchHistoryStringBuilder.append(MapsActivity.mSearchHistoryRegex[i] + "\n");
                    placeIdStringBuilder.append(MapsActivity.mPlaceIdRegex[i] + "\n");
                }

                FileOutputStream searchHistoryOutputStream;
                FileOutputStream placeIdOutputStream;
                try {
                    searchHistoryOutputStream = mActivity.openFileOutput(
                            MapsActivity.SEARCH_HISTORY_FILENAME, Context.MODE_PRIVATE);
                    searchHistoryOutputStream.write(searchHistoryStringBuilder.toString().getBytes());
                    searchHistoryOutputStream.close();

                    placeIdOutputStream = mActivity.openFileOutput(
                            MapsActivity.PLACE_ID_FILENAME, Context.MODE_PRIVATE);
                    placeIdOutputStream.write(placeIdStringBuilder.toString().getBytes());
                    placeIdOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MapsActivity.mSearchHistory = searchHistoryStringBuilder;
                MapsActivity.mSearchHistoryRegex = searchHistoryStringBuilder.toString().split("\n");

                String searchHistory = MapsActivity.mSearchHistory.toString();
                for (int i=0, cnt=0; i<searchHistory.length(); i++) {
                    MapsActivity.mCharToPos[i] = cnt;
                    if (searchHistory.charAt(i) == '\n') {
                        cnt++;
                    }
                }

                MapsActivity.mPlaceId = placeIdStringBuilder;
                MapsActivity.mPlaceIdRegex = placeIdStringBuilder.toString().split("\n");
                MapsActivity.mRoot = SuffixTreeHelper.constructSuffixTree(MapsActivity.mSearchHistory.toString());

                mActivity.getContentResolver().delete(
                        SearchHistoryContract.SearchHistoryEntry.CONTENT_URI,
                        null,
                        null
                );
            }

            cursor.close();
        }

        return null;
    }
}
