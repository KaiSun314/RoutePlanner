package com.SunDragon.RoutePlanner;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Pair;

import com.SunDragon.RoutePlanner.SearchHistoryContract.SearchHistoryEntry;

import java.util.ArrayList;

public class SearchHistoryAsyncQueryHandler extends AsyncQueryHandler {

    public static final int SUBMIT_QUERY_TOKEN = 0;
    public static final int SEARCH_SUGGESTIONS_TOKEN = 1;

    public SearchHistoryAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        MapsActivity mActivity = (MapsActivity) ((Pair) cookie).first;

        ArrayList<String> placeDescriptions = new ArrayList<>();
        ArrayList<String> placeIds = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            placeDescriptions.add(cursor.getString(
                    cursor.getColumnIndex(SearchHistoryEntry.SEARCH_QUERY)));
            placeIds.add(cursor.getString(
                    cursor.getColumnIndex(SearchHistoryEntry.PLACE_ID)));
        }

        if (placeDescriptions.size() < 5) {
            int idx = 0;
            String query = (String) ((Pair) cookie).second;
            while (idx < query.length() && query.charAt(idx) == ' ') {
                idx++;
            }
            query = query.substring(idx);
            int[] pos = new int[0];
            if (!query.equals("")) {
                pos = SuffixTreeHelper.getSearchHistoryResults(query, 0, MapsActivity.mRoot);
            }
            for (int i=0; i < pos.length && placeDescriptions.size() < 5; i++) {
                if (pos[i] == SuffixTreeHelper.INF) break;
                if (placeDescriptions.contains(MapsActivity.mSearchHistoryRegex[
                        MapsActivity.mCharToPos[pos[i]]])) continue;
                placeDescriptions.add(
                        MapsActivity.mSearchHistoryRegex[MapsActivity.mCharToPos[pos[i]]]);
                String placeId = MapsActivity.mPlaceIdRegex[MapsActivity.mCharToPos[pos[i]]];
                placeIds.add((placeId.equals(DatabaseToFileTask.NULL) ? null : placeId));
            }
        }

        mActivity.mFragmentAdd.mSearchHistoryAdapter.setPlaceDescription(placeDescriptions,
                mActivity.mFragmentAdd.mSearchHistoryExpanded);
        mActivity.mFragmentAdd.mSearchHistoryAdapter.setPlaceIds(placeIds);
    }
}
