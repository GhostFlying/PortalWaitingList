package com.ghostflying.portalwaitinglist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ghostflying on 11/23/14.
 * <br>
 * DB Helper Class, used for create or update.
 */
public class PortalEventDbHelper extends SQLiteOpenHelper {
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "PortalEvent.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_EVENTS = "CREATE TABLE " + PortalEventContract.PortalEvent.TABLE_NAME
            + " (" + PortalEventContract.PortalEvent._ID + INTEGER_TYPE + " PRIMARY KEY, "
            + PortalEventContract.PortalEvent.COLUMN_NAME_PORTAL_NAME + TEXT_TYPE + COMMA_SEP
            + PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_TYPE + INTEGER_TYPE + COMMA_SEP
            + PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_RESULT + INTEGER_TYPE + COMMA_SEP
            + PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID + TEXT_TYPE + COMMA_SEP
            + PortalEventContract.PortalEvent.COLUMN_NAME_DATE + TEXT_TYPE + " )";
    private static final String SQL_DELETE_EVENTS = "DROP TABLE IF EXISTS " + PortalEventContract.PortalEvent.TABLE_NAME;

    public PortalEventDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EVENTS);
        onCreate(db);
    }
}
