package com.ghostflying.portalwaitinglist.model;

import android.os.Parcel;

import java.util.Date;

/**
 * Created by Ghost on 2014/12/4.
 * <br>
 * SubClass for submission event.
 */
public class SubmissionEvent extends PortalEvent {
    String portalImageUrl;

    public SubmissionEvent(String portalName,
                           OperationResult result,
                           Date date,
                           String messageId,
                           String portalImageUrl){
        super(portalName, result, date, messageId);
        this.portalImageUrl = portalImageUrl;
    }

    public SubmissionEvent(String portalName,
                           OperationResult result,
                           Date date,
                           String messageId,
                           String portalImageUrl,
                           String portalAddress,
                           String portalAddressUrl){
        super(portalName, result, date, messageId);
        this.portalImageUrl = portalImageUrl;
        this.portalAddress = portalAddress;
        this.portalAddressUrl = portalAddressUrl;
    }

    protected SubmissionEvent(Parcel in){
        super(in);
        portalImageUrl = in.readString();
    }

    public String getPortalImageUrl(){
        return portalImageUrl;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SUBMISSION;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(portalImageUrl);
    }
}
