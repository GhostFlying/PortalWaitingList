package com.ghostflying.portalwaitinglist.data;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ghostflying on 11/22/14.
 * <br>
 * The data structure of portal detail, the class used to create adapter finally.
 */
public class PortalDetail implements Comparable<PortalDetail>{
    private static final long LONG_TIME_THRESHOLD = 3600L * 24 * 7 * 1000;
    private static final long LONG_TIME_NO_RESPONSE_THRESHOLD = 3600L * 24 * 1000 * 365;
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
     * Check if the portal edit/submit reviewed for a long time or has no response for a long time.
     * @return  true if it is reviewed before a long time or has no response for a long time, otherwise false.
     */
    public boolean isReviewedOrNoResponseForLongTime(){
        if ((isReviewed() && (new Date().getTime() - getLastUpdated().getTime()) > LONG_TIME_THRESHOLD)
                || (new Date().getTime() - getLastUpdated().getTime()) > LONG_TIME_NO_RESPONSE_THRESHOLD)
            return true;
        else
            return false;
    }

    /**
     * Check if the portal edit/submit accepted by NIA.
     * If there is any accept event for the portal, it will be deal as accepted.
     * @return  true if accepted, otherwise false.
     */
    public boolean isAccepted(){
        for (PortalEvent eachEvent : events){
            if (eachEvent.getOperationResult() == PortalEvent.OperationResult.PASSED)
                return true;
        }
        return false;
    }

    /**
     * Check if the portal edit/submit rejected by NIA.
     * @return  true if rejected, otherwise false.
     */
    public boolean isRejected(){
        return events.get(events.size() - 1).getOperationResult() == PortalEvent.OperationResult.REJECTED;
    }
}
