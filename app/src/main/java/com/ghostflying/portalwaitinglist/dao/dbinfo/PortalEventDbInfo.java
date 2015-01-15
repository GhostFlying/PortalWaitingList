package com.ghostflying.portalwaitinglist.dao.dbinfo;

import android.provider.BaseColumns;

/**
 * Created by ghostflying on 1/14/15.
 */
public class PortalEventDbInfo implements BaseColumns {
    public static final int ID = 0;
    public static final String TABLE_NAME = "event";
    public static final String COLUMN_NAME_PORTAL_NAME = "portalname";
    public static final String COLUMN_NAME_OPERATION_TYPE = "operationtype";
    public static final String COLUMN_NAME_OPERATION_RESULT = "operationresult";
    public static final String COLUMN_NAME_MESSAGE_ID = "messageid";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_IMAGE_URL = "imageurl";
    public static final String COLUMN_NAME_ADDRESS = "address";
    public static final String COLUMN_NAME_ADDRESS_URL = "addressurl";
}
