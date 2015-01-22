package com.ghostflying.portalwaitinglist.model;

import android.os.Parcel;

import java.util.Date;

/**
 * Created by Ghost on 2014/12/4.
 */
public class InvalidEvent extends SubmissionEvent {
    public InvalidEvent(String portalName, OperationResult result, Date date, String messageId, String portalImageUrl) {
        super(portalName, result, date, messageId, portalImageUrl);
    }

    protected InvalidEvent(Parcel in){
        super(in);
    }

    @Override
    public OperationType getOperationType(){
        return OperationType.INVALID;
    }
}
