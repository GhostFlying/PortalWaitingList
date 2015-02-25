package com.ghostflying.portalwaitinglist.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ghostflying.portalwaitinglist.dao.DataProvider;
import com.ghostflying.portalwaitinglist.dao.DbHelper;
import com.ghostflying.portalwaitinglist.dao.dbinfo.PortalEventDbInfo;
import com.ghostflying.portalwaitinglist.model.EditEvent;
import com.ghostflying.portalwaitinglist.model.InvalidEvent;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.model.SubmissionEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ghostflying on 1/19/15.
 */
public class PortalEventHelper extends BaseHelper {
    @Override
    protected String getTableName() {
        return PortalEventDbInfo.TABLE_NAME;
    }

    public PortalEventHelper(Context context){
        super(context);
    }

    public Uri insert(PortalEvent portalEvent){
        ContentValues values = getContentValues(portalEvent);
        return insert(values);
    }

    private ContentValues getContentValues(PortalEvent portalEvent) {
        ContentValues values = new ContentValues();
        values.put(PortalEventDbInfo.COLUMN_NAME_PORTAL_NAME, portalEvent.getPortalName());
        values.put(PortalEventDbInfo.COLUMN_NAME_OPERATION_TYPE, portalEvent.getOperationType().ordinal());
        values.put(PortalEventDbInfo.COLUMN_NAME_OPERATION_RESULT, portalEvent.getOperationResult().ordinal());
        values.put(PortalEventDbInfo.COLUMN_NAME_DATE, portalEvent.getDate().getTime());
        values.put(PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID, portalEvent.getMessageId());
        if (portalEvent instanceof SubmissionEvent){
            values.put(PortalEventDbInfo.COLUMN_NAME_IMAGE_URL, ((SubmissionEvent) portalEvent).getPortalImageUrl());
        }
        else {
            if (portalEvent.getOperationResult() != PortalEvent.OperationResult.PROPOSED){
                values.put(PortalEventDbInfo.COLUMN_NAME_ADDRESS, portalEvent.getPortalAddress());
                values.put(PortalEventDbInfo.COLUMN_NAME_ADDRESS_URL, portalEvent.getPortalAddressUrl());
            }
        }
        return values;
    }

    public int bulkInsert(List<PortalEvent> portalEvents){
        if (portalEvents.size() == 0)
            return 0;
        ArrayList<ContentValues> valuesList = new ArrayList<>();
        for (PortalEvent eachEvent : portalEvents){
            valuesList.add(getContentValues(eachEvent));
        }
        return bulkInsert(valuesList.toArray(new ContentValues[valuesList.size()]));
    }

    /**
     * Get all events after id.
     * @param id the id to filter
     * @return  the cursor.
     */
    public Cursor getAll(long id){
        return query(
                null,
                PortalEventDbInfo._ID + "> ?",
                new String[]{Long.toString(id)},
                PortalEventDbInfo.COLUMN_NAME_DATE + " ASC"
        );
    }

    public long getLastEventTimestamp(){
        synchronized (DataProvider.class){
            DbHelper mHelper = new DbHelper(mContext);
            SQLiteDatabase mDatabase = mHelper.getReadableDatabase();
            Cursor lastEvent = mDatabase.rawQuery(
                    "SELECT MAX(" + PortalEventDbInfo.COLUMN_NAME_DATE
                            + ") AS MAX FROM " + PortalEventDbInfo.TABLE_NAME,
                    null);
            try{
                lastEvent.moveToFirst();
                return lastEvent.getLong(0);
            }
            finally {
                lastEvent.close();
                mDatabase.close();
            }
        }
    }

    public boolean isExist(String messageId){
        Cursor mCursor = query(
                new String[]{PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID},
                PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID + " = ?",
                new String[]{messageId},
                null
        );
        try{
            return mCursor.moveToFirst();
        }
        finally {
            mCursor.close();
        }
    }

    /**
     * Get events with the same portal name with the specified event.
     * The specified event is the first one.
     * @param messageId the messageId of the specified event.
     * @return  the list of events.
     */
    public List<PortalEvent> getEventsByName(String messageId){
        List<PortalEvent> events = new ArrayList<>();
        Cursor specifiedCursor = query(
                null,
                PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID + " = ?",
                new String[]{messageId},
                null
        );
        try{
            events.addAll(fromCursor(specifiedCursor));
        }
        finally {
            specifiedCursor.close();
        }

        if (events.size() == 1){
            String portalName = events.get(0).getPortalName();
            Cursor sameNameCursor = query(
                    null,
                    PortalEventDbInfo.COLUMN_NAME_PORTAL_NAME + " = ? AND " +
                    PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID + " != ?",
                    new String[]{portalName, messageId},
                    PortalEventDbInfo.COLUMN_NAME_DATE + " ASC"
            );
            try{
                events.addAll(fromCursor(sameNameCursor));
            }
            finally {
                sameNameCursor.close();
            }
            return events;
        }
        // if there are no events in the specified Id, return empty list.
        else
            return new ArrayList<PortalEvent>();
    }

    public List<PortalEvent> fromCursor(Cursor cursor){
        List<PortalEvent> portalEvents = new ArrayList<>();
        int portalNameIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_PORTAL_NAME);
        int operationTypeIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_OPERATION_TYPE);
        int operationResultIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_OPERATION_RESULT);
        int messageIdIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_MESSAGE_ID);
        int dateIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_DATE);
        int imageUrlIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_IMAGE_URL);
        int addressIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_ADDRESS);
        int addressUrlIndex = cursor.getColumnIndex(PortalEventDbInfo.COLUMN_NAME_ADDRESS_URL);
        while(cursor.moveToNext()){
            PortalEvent event;
            String name = cursor.getString(portalNameIndex);
            PortalEvent.OperationType operationType = PortalEvent.OperationType.values()[cursor.getInt(operationTypeIndex)];
            PortalEvent.OperationResult operationResult = PortalEvent.OperationResult.values()[cursor.getInt(operationResultIndex)];
            Date date = new Date(cursor.getLong(dateIndex));
            String messageId = cursor.getString(messageIdIndex);
            if (operationType == PortalEvent.OperationType.SUBMISSION){
                String imageUrl = cursor.getString(imageUrlIndex);
                if (operationResult == PortalEvent.OperationResult.ACCEPTED){
                    String address = cursor.getString(addressIndex);
                    String addressUrl = cursor.getString(addressUrlIndex);
                    event = new SubmissionEvent(name,
                            operationResult,
                            date,
                            messageId,
                            imageUrl,
                            address,
                            addressUrl);
                }
                else {
                    event = new SubmissionEvent(name, operationResult, date, messageId, imageUrl);
                }
            }
            else if (operationType == PortalEvent.OperationType.INVALID){
                String imageUrl = cursor.getString(imageUrlIndex);
                event = new InvalidEvent(name, operationResult, date, messageId, imageUrl);
            }
            else {
                if (operationResult != PortalEvent.OperationResult.PROPOSED){
                    String address = cursor.getString(addressIndex);
                    String addressUrl = cursor.getString(addressUrlIndex);
                    event = new EditEvent(name, operationResult, date, messageId, address, addressUrl);
                }
                else {
                    event = new EditEvent(name, operationResult, date, messageId);
                }
            }
            portalEvents.add(event);
        }
        return portalEvents;
    }
}
