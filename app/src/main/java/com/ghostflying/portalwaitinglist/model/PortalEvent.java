package com.ghostflying.portalwaitinglist.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * The data structure of each event of portal.
 */
public abstract class PortalEvent implements Comparable<PortalEvent>, Parcelable{
    String portalName;
    OperationResult operationResult;
    Date date;
    String portalAddress;
    String portalAddressUrl;
    // The message's id related to this event, store to avoid duplicate when update.
    String messageId;

    public PortalEvent(String portalName,
                       OperationResult operationResult,
                       Date date,
                       String messageId){
        this.portalName = portalName;
        this.operationResult = operationResult;
        this.date = date;
        this.messageId = messageId;
    }

    protected PortalEvent(Parcel in){
        portalName = in.readString();
        operationResult = OperationResult.values()[in.readInt()];
        date = new Date(in.readLong());
        portalAddress = (String)in.readValue(String.class.getClassLoader());
        portalAddressUrl = (String)in.readValue(String.class.getClassLoader());
        messageId = in.readString();
    }

    public String getPortalName() {
        return portalName;
    }

    public abstract OperationType getOperationType();

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public Date getDate() {
        return date;
    }

    public String getMessageId(){
        return messageId;
    }

    public String getPortalAddress(){
        return portalAddress;
    }

    public String getPortalAddressUrl(){
        return portalAddressUrl;
    }

    @Override
    public int compareTo(PortalEvent another) {
        return this.date.compareTo(another.getDate());
    }

    public enum OperationType{
        SUBMISSION, EDIT, INVALID
    }

    public enum OperationResult{
        PROPOSED, ACCEPTED, REJECTED, DUPLICATE, TOO_CLOSE
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(getOperationType().ordinal());
        out.writeString(portalName);
        out.writeInt(operationResult.ordinal());
        out.writeLong(date.getTime());
        out.writeValue(portalAddress);
        out.writeValue(portalAddressUrl);
        out.writeString(messageId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PortalEvent> CREATOR
            = new Creator<PortalEvent>() {
        @Override
        public PortalEvent createFromParcel(Parcel source) {
            OperationType type = OperationType.values()[source.readInt()];
            switch (type){
                case EDIT:
                    return new EditEvent(source);
                case INVALID:
                    return new InvalidEvent(source);
                case SUBMISSION:
                default:
                    return new SubmissionEvent(source);
            }
        }

        @Override
        public PortalEvent[] newArray(int size) {
            return new PortalEvent[0];
        }
    };
}
