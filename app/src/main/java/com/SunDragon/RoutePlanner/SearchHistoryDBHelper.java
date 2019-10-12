package com.SunDragon.RoutePlanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.SunDragon.RoutePlanner.SearchHistoryContract.SearchHistoryEntry;

public class SearchHistoryDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SearchHistory.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE_v1
            = "CREATE TABLE " + SearchHistoryEntry.TABLE_NAME + " ("
            + SearchHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchHistoryEntry.TIMESTAMP + " INTEGER, "
            + SearchHistoryEntry.SEARCH_QUERY + " TEXT NOT NULL, "
            + SearchHistoryEntry.PLACE_ID + " TEXT)";

    public SearchHistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_v1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
