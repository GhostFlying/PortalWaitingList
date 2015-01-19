package com.ghostflying.portalwaitinglist.dao.datahelper;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.ghostflying.portalwaitinglist.dao.dbinfo.PortalEventDbInfo;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.model.SubmissionEvent;

import java.util.ArrayList;
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
}
