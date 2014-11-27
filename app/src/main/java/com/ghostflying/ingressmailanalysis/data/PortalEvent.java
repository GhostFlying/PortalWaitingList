package com.ghostflying.ingressmailanalysis.data;

import java.util.Date;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * The data structure of each event of portal.
 */
public class PortalEvent implements Comparable<PortalEvent>{
    String portalName;
    OperationType operationType;
    OperationResult operationResult;
    Date date;
    // The message's id related to this event, store to avoid duplicate when update.
    String messageId;

    public PortalEvent(String portalName, OperationType operationType,
                       OperationResult operationResult, Date date, String messageId){
        this.portalName = portalName;
        this.operationType = operationType;
        this.operationResult = operationResult;
        this.date = date;
        this.messageId = messageId;
    }

    public String getPortalName() {
        return portalName;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public OperationResult getOperationResult() {
        return operationResult;
    }

    public Date getDate() {
        return date;
    }

    public String getMessageId(){
        return messageId;
    }

    @Override
    public int compareTo(PortalEvent another) {
        return this.date.compareTo(another.getDate());
    }

    public enum OperationType{
        SUBMIT, EDIT
    }

    public enum OperationResult{
        PROPOSED, PASSED, REJECTED
    }
}
