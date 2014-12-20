package com.ghostflying.portalwaitinglist.data;

import com.ghostflying.portalwaitinglist.Util.SettingUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ghostflying on 11/22/14.
 * <br>
 * The data structure of portal detail, the class used to create adapter finally.
 */
public class PortalDetail implements Comparable<PortalDetail>{
    public static final int PRIORITY_REVIEWED_IN_SHORT_TIME = 4;
    public static final int PRIORITY_WAITING_FOR_REVIEW = 3;
    public static final int PRIORITY_NO_RESPONSE_FOR_LONG_TIME = 2;
    public static final int PRIORITY_REVIEWED_BEFORE_SHORT_TIME = 1;

    private static final long ONE_DAY_TIME_IN_MILLISECONDS = 24 * 3600 * 1000;
    private String name;
    private ArrayList<PortalEvent> events;

    /**
     * Construct method to create a new instance of PortalDetail.
     * @param event the first event
     */
    public PortalDetail(PortalEvent event){
        events = new ArrayList<PortalEvent>();
        addEvent(event);
        name = event.portalName;
    }

    /**
     * Add event to the last position of the events.
     * @param event the new event to be added.
     * @return the size after the add action.
     */
    public int addEvent(PortalEvent event){
        events.add(event);
        return events.size();
    }

    public String getName(){
        return name;
    }

    /**
     * Get the image url if exist.
     * @return  the url if exist, otherwise null.
     */
    public String getImageUrl(){
        for(PortalEvent event : events){
            if (event instanceof SubmissionEvent)
                return ((SubmissionEvent) event).getPortalImageUrl();
        }
        return null;
    }

    /**
     * Get the address if exist.
     * @return  the address if exist, otherwise null.
     */
    public String getAddress(){
        for (PortalEvent event : events)
            if (event.getPortalAddress() != null)
                return event.getPortalAddress();
        return null;
    }

    /**
     * Get the address url if exist.
     * @return  the address if exist, otherwise null.
     */
    public String getAddressUrl(){
        for (PortalEvent event : events)
            if (event.getPortalAddressUrl() != null)
                return event.getPortalAddressUrl();
        return null;
    }

    public ArrayList<PortalEvent> getEvents(){
        return events;
    }

    /**
     * Override to achieve the desc sort by last update date.
     * @param another {@inheritDoc}
     * @return a negative int as the instance is more than another,
     *          a positive int as the instance is less than another,
     *          0 when they are equal.
     */
    @Override
    public int compareTo(PortalDetail another) {
        return this.getLastUpdated().compareTo(another.getLastUpdated());
    }

    /**
     * Get the last update date of this portal
     * @return the newest event's update date.
     */
    public Date getLastUpdated(){
        return events.get(events.size() - 1).date;
    }

    /**
     * Check if the portal edit/submit reviewed by NIA.
     * @return true if it is reviewed, otherwise false.
     */
    public boolean isReviewed(){
        return events.get(events.size() - 1).getOperationResult() != PortalEvent.OperationResult.PROPOSED;
    }

    /**
     * Check if the portal edit/submit has no response for a long time.
     * @return  true if it has no response for a long time, otherwise false.
     */
    public boolean isNoResponseForLongTime(){
        if ((!isReviewed())
                && (new Date().getTime() - getLastUpdated().getTime()) >
                ONE_DAY_TIME_IN_MILLISECONDS * SettingUtil.getLongTime())
            return true;
        else
            return false;
    }

    /**
     * Check if the portal edit/submit reviewed in a short time.
     * @return  true if it is reviewed in a short time, otherwise false.
     */
    public boolean isReviewedInShortTime(){
        return isReviewed() && isUpdatedInShortTime();
    }

    /**
     * Check if the portal edit/submit reviewed before a short time.
     * @return  true if it is reviewed before a short time, otherwise false.
     */
    public boolean isReviewedBeforeShortTime(){
        return isReviewed() && (!isUpdatedInShortTime());
    }

    private boolean isUpdatedInShortTime(){
        return (new Date().getTime() - getLastUpdated().getTime()) <
                ONE_DAY_TIME_IN_MILLISECONDS * SettingUtil.getShortTime();
    }

    /**
     * Get the priority to use in smart order.
     * @return  the order priority.
     */
    public int getOrderPrior(){
        if (isNoResponseForLongTime())
            return PRIORITY_NO_RESPONSE_FOR_LONG_TIME;
        else if (!isReviewed())
            return PRIORITY_WAITING_FOR_REVIEW;
        else if (isReviewedBeforeShortTime())
            return PRIORITY_REVIEWED_BEFORE_SHORT_TIME;
        else
            return PRIORITY_REVIEWED_IN_SHORT_TIME;
    }

    /**
     * Check if the portal edit/submit accepted by NIA.
     * If there is any accept event for the portal, it will be deal as accepted.
     * @return  true if accepted, otherwise false.
     */
    public boolean isEverAccepted(){
        for (PortalEvent eachEvent : events){
            if (eachEvent.getOperationResult() == PortalEvent.OperationResult.ACCEPTED)
                return true;
        }
        return false;
    }

    /**
     * Check if the last operation for this portal is accepted by NIA.
     * @return  true if accepted, otherwise false.
     */
    public boolean isAccepted(){
        return events.get(events.size() - 1).getOperationResult() == PortalEvent.OperationResult.ACCEPTED;
    }

    /**
     * Check if the portal edit/submit rejected by NIA.
     * @return  true if rejected, otherwise false.
     */
    public boolean isRejected(){
        return events.get(events.size() - 1).getOperationResult() == PortalEvent.OperationResult.REJECTED
                || events.get(events.size() - 1).getOperationResult() == PortalEvent.OperationResult.DUPLICATE;
    }
}
