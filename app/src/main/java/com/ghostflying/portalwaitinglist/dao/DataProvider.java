package com.ghostflying.portalwaitinglist.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ghostflying.portalwaitinglist.BuildConfig;
import com.ghostflying.portalwaitinglist.dao.dbinfo.PortalEventDbInfo;


public class DataProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    private DbHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    public DataProvider() {

    }

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH){
        {
            addURI(AUTHORITY, PortalEventDbInfo.TABLE_NAME, PortalEventDbInfo.ID);
        }
    };

    private String getTableName(Uri uri){
        switch (mUriMatcher.match(uri)){
            case PortalEventDbInfo.ID:
                return PortalEventDbInfo.TABLE_NAME;
            default:
                return "";
        }
    }

    private DbHelper getDbHelper(){
        if (mDbHelper == null)
            mDbHelper = new DbHelper(getContext());
        return mDbHelper;
    }

    private SQLiteDatabase getDb(){
        DbHelper mHelper = getDbHelper();
        if (mDatabase == null)
            mDatabase = mHelper.getWritableDatabase();
        return mDatabase;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Delete is not supported");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        synchronized (DataProvider.class){
            SQLiteDatabase mDb = getDb();
            mDb.beginTransaction();
            long rowId = 0;
            try{
                rowId = mDb.insert(getTableName(uri), null, values);
                mDb.setTransactionSuccessful();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                mDb.endTransaction();
            }
            if (rowId > 0){
                Uri returnUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnUri;
            }
            throw new SQLException("Failed to insert to " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values){
        synchronized (DataProvider.class){
            SQLiteDatabase mDb = getDb();
            mDb.beginTransaction();
            try{
                for (ContentValues each : values){
                    mDb.insertWithOnConflict(
                            getTableName(uri),
                            null,
                            each,
                            SQLiteDatabase.CONFLICT_IGNORE
                    );
                }
                mDb.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                return values.length;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                mDb.endTransaction();
            }
            throw new SQLException("Failed to insert to " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        synchronized (DataProvider.class){
            SQLiteQueryBuilder mQueryBuilder = new SQLiteQueryBuilder();
            mQueryBuilder.setTables(getTableName(uri));
            SQLiteDatabase mDb = getDb();
            Cursor mCursor = mQueryBuilder.query(
                    mDb,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            mCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return mCursor;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
