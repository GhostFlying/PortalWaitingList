package com.ghostflying.portalwaitinglist.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ghostflying.portalwaitinglist.dao.dbinfo.PortalEventDbInfo;

/**
 * Created by ghostflying on 1/14/15.
 */
public class DbHelper extends SQLiteOpenHelper{
    static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "PortalEvent.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String NOT_NULL = " NOT NULL";
    private static final String SQL_CREATE_EVENTS = "CREATE TABLE " + PortalEventDbInfo.TABLE_NAME
            + " (" + PortalEventDbInfo._ID + INTEGER_TYPE + " PRIMARY KEY, "
            + PortalEventDbInfo.COLUMN_NAME_PORTAL_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_OPERATION_TYPE + INTEGER_TYPE + NOT_NULL + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_OPERATION_RESULT + INTEGER_TYPE + NOT_NULL + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_DATE + TEXT_TYPE + NOT_NULL + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_IMAGE_URL + TEXT_TYPE + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP
            + PortalEventDbInfo.COLUMN_NAME_ADDRESS_URL + TEXT_TYPE + " )";
    private static final String SQL_DELETE_EVENTS = "DROP TABLE IF EXISTS " + PortalEventDbInfo.TABLE_NAME;

    public DbHelper(Context context){
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
