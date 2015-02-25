package com.ghostflying.portalwaitinglist.model;

import java.util.ArrayList;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * The data structure of the message list received.
 * Some data does not need is commented.
 */
public class MessageList {
    public ArrayList<MessageId> getMessages() {
        if (messages == null)
            messages = new ArrayList<MessageId>();
        return messages;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    ArrayList<MessageId> messages;
    String nextPageToken;
    //long resultSizeEstimate;

    /**
     * Return if there is already next page in the query.
     * @return  true if there is next page, otherwise false.
     */
    public boolean hasNextPage(){
        return (nextPageToken != null);
    }

    /**
     * The data in messages, only contain id and threadId
     */
    public class MessageId {
        public String getId() {
            return id;
        }

        String id;
        //String threadId;
    }
}
