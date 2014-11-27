package com.ghostflying.ingressmailanalysis;

import android.provider.BaseColumns;

/**
 * Created by ghostflying on 11/23/14.
 * <br>
 * Define database scheme for Portal Event
 */
public final class PortalEventContract {
    public PortalEventContract(){}

    public static abstract class PortalEvent implements BaseColumns{
        public static final String TABLE_NAME = "event";
        public static final String COLUMN_NAME_PORTAL_NAME = "portalname";
        public static final String COLUMN_NAME_OPERATION_TYPE = "operationtype";
        public static final String COLUMN_NAME_OPERATION_RESULT = "operationresult";
        public static final String COLUMN_NAME_MESSAGE_ID = "messageid";
        public static final String COLUMN_NAME_DATE = "date";
    }
}
