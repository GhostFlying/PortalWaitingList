package com.ghostflying.portalwaitinglist.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * The data structure of each event of portal.
 */
public abstract class PortalEvent implements Comparable<PortalEvent>, Serializable{
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
        PROPOSED, ACCEPTED, REJECTED, DUPLICATE
    }
}
