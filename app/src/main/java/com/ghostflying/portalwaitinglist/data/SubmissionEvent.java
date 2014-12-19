package com.ghostflying.portalwaitinglist.data;

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

    public String getPortalImageUrl(){
        return portalImageUrl;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SUBMISSION;
    }
}
