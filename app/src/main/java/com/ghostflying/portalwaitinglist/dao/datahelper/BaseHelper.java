package com.ghostflying.portalwaitinglist.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ghostflying.portalwaitinglist.dao.DataProvider;

/**
 * Created by ghostflying on 1/19/15.
 */
public abstract class BaseHelper {
    Context mContext;
    Uri mUri;

    public BaseHelper(Context context){
        mContext = context;
        mUri = Uri.parse("content://" + DataProvider.AUTHORITY + "/" + getTableName());
    }

    protected abstract String getTableName();

    protected Cursor query(Uri uri,
                           String[] projection,
                           String selection,
                           String[] selectionArgs,
                           String sortOrder){
        return mContext.getContentResolver().query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    protected Cursor query(String[] projection,
                           String selection,
                           String[] selectionArgs,
                           String sortOrder){
        return mContext.getContentResolver().query(
                mUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    protected Uri insert(ContentValues values){
        return mContext.getContentResolver().insert(mUri, values);
    }

    protected int bulkInsert(ContentValues[] values){
        return mContext.getContentResolver().bulkInsert(mUri, values);
    }

    public void notifyChange(){
        mContext.getContentResolver().notifyChange(mUri, null);
    }
}
