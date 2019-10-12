package com.SunDragon.RoutePlanner;

import android.net.Uri;
import android.provider.BaseColumns;

public class SearchHistoryContract {

    public static final String CONTENT_AUTHORITY = "com.SunDragon.RoutePlanner";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class SearchHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "searchHistoryTable";

        public static final String TIMESTAMP = "timestamp";
        public static final String SEARCH_QUERY = "searchQuery";
        public static final String PLACE_ID = "placeId";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME)
                .build();

        public static final String[] PROJECTION_SEARCH_HISTORY = new String[] {
                SEARCH_QUERY,
                PLACE_ID
        };
    }

}
