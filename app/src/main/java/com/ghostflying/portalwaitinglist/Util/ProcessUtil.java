package com.ghostflying.portalwaitinglist.Util;

import com.ghostflying.portalwaitinglist.SettingUtil;
import com.ghostflying.portalwaitinglist.data.EditEvent;
import com.ghostflying.portalwaitinglist.data.InvalidEvent;
import com.ghostflying.portalwaitinglist.data.Message;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.data.PortalEvent;
import com.ghostflying.portalwaitinglist.data.SubmissionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ghost on 2014/12/4.
 */
public class ProcessUtil {
    private static final String REGEX_PORTAL_SUBMISSION = "(?<=Ingress Portal Submitted:).+";
    private static final String REGEX_PORTAL_EDIT = "(?<=Ingress Portal Edits Submitted:).+";
    private static final String REGEX_INVALID_REPORT = "(?<=Invalid Ingress Portal Report:).+";
    private static final String REGEX_PORTAL_SUBMISSION_PASSED = "(?<=Ingress Portal Live:).+";
    private static final String REGEX_PORTAL_SUBMISSION_REJECTED = "(?<=Ingress Portal Rejected:).+";
    private static final String REGEX_PORTAL_SUBMISSION_DUPLICATE = "(?<=Ingress Portal Duplicate:).+";
    private static final String REGEX_PORTAL_EDIT_PASSED = "(?<=Ingress Portal Data Edit Accepted:).+";
    private static final String REGEX_PORTAL_EDIT_REJECTED = "(?<=Ingress Portal Data Edit Reviewed:).+";
    private Pattern patternSubmission;
    private Pattern patternEdit;
    private Pattern patternInvalid;
    private Pattern patternSubmissionPassed;
    private Pattern patternSubmissionRejected;
    private Pattern patternSubmissionDuplicate;
    private Pattern patternEditPassed;
    private Pattern patternEditRejected;

    /**
     * Convert messages to events.
     * @param messages the origin messages.
     * @return the converted events.
     */
    public ArrayList<PortalEvent> analysisMessages(ArrayList<Message> messages){
        ArrayList<PortalEvent> portalEvents = new ArrayList<PortalEvent>();


        for (Message message : messages){
            PortalEvent portalEvent = analysisMessage(message);
            if (portalEvent != null)
                portalEvents.add(portalEvent);
        }
        return portalEvents;
    }

    /**
     * Convert message to event.
     * @param message the origin message.
     * @return the converted event.
     */
    private PortalEvent analysisMessage(Message message){
        //Initial pattern
        if (patternSubmission == null){
            patternSubmission = Pattern.compile(REGEX_PORTAL_SUBMISSION);
            patternEdit = Pattern.compile(REGEX_PORTAL_EDIT);
            patternInvalid = Pattern.compile(REGEX_INVALID_REPORT);
            patternSubmissionPassed = Pattern.compile(REGEX_PORTAL_SUBMISSION_PASSED);
            patternSubmissionRejected = Pattern.compile(REGEX_PORTAL_SUBMISSION_REJECTED);
            patternSubmissionDuplicate = Pattern.compile(REGEX_PORTAL_SUBMISSION_DUPLICATE);
            patternEditPassed = Pattern.compile(REGEX_PORTAL_EDIT_PASSED);
            patternEditRejected = Pattern.compile(REGEX_PORTAL_EDIT_REJECTED);
        }

        Matcher matcher;

        // Try to match regex.
        matcher = patternSubmission.matcher(message.getSubject());
        if (matcher.find()){
            return new SubmissionEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.PROPOSED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        matcher = patternEdit.matcher(message.getSubject());
        if (matcher.find()){
            return new EditEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.PROPOSED, message.getDate(), message.getId());
        }

        matcher = patternInvalid.matcher(message.getSubject());
        if (matcher.find()){
            return new InvalidEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.PROPOSED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        matcher = patternSubmissionPassed.matcher(message.getSubject());
        if (matcher.find()){
            return new SubmissionEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.PASSED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        matcher = patternSubmissionRejected.matcher(message.getSubject());
        if (matcher.find()){
            return new SubmissionEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.REJECTED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        matcher = patternSubmissionDuplicate.matcher(message.getSubject());
        if (matcher.find()){
            return new SubmissionEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.DUPLICATE,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        matcher = patternEditPassed.matcher(message.getSubject());
        if (matcher.find()){
            return new EditEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.PASSED,
                    message.getDate(), message.getId(),
                    getPortalAddress(message.getMessageHtml()),
                    getPortalAddressUrl(message.getMessageHtml()));
        }

        matcher = patternEditRejected.matcher(message.getSubject());
        if (matcher.find()){
            return new EditEvent(matcher.group().trim(),
                    PortalEvent.OperationResult.REJECTED,
                    message.getDate(), message.getId(),
                    getPortalAddress(message.getMessageHtml()),
                    getPortalAddressUrl(message.getMessageHtml()));
        }

        return null;
    }

    private String getImageUrl(String html){
        //TODO complete the html parse
        return null;
    }

    private String getPortalAddress(String html){
        //TODO complete the html parse
        return null;
    }

    private String getPortalAddressUrl(String html){
        //TODO complete the html parse
        return null;
    }

    /**
     * Merge new events to exist portal detail or create new one.
     * @param origin exist portal detail list.
     * @param events events need to be merged.
     * @return the merged portal detail list.
     */
    public ArrayList<PortalDetail> mergeEvents(ArrayList<PortalDetail> origin, ArrayList<PortalEvent> events){
        for (PortalEvent event : events){
            //TODO add save to sqlite
            PortalDetail existDetail = findExistDetail(origin, event.getPortalName());
            if (existDetail == null){
                PortalDetail newDetail = new PortalDetail(event);
                origin.add(newDetail);
            }
            else {
                existDetail.addEvent(event);
                // sort the event list.
                Collections.sort(existDetail.getEvents());
            }
        }
        return origin;
    }

    /**
     * Find the exist portal detail has the same name with new event name
     * @param details   exist portal detail list.
     * @param eventName new event name.
     * @return  the portal detail if exists, or null if it does not exist.
     */
    private PortalDetail findExistDetail(ArrayList<PortalDetail> details, String eventName){
        //TODO change to check image or address
        for (PortalDetail detail : details){
            if (detail.getName().equals(eventName))
                return detail;
        }
        return null;
    }


    /**
     * To sort and filter origin and store result to edit.
     * @param filterMethod  the filter method to use.
     * @param sortOrder     the sort order to use.
     * @param origin        the origin list.
     * @param edit          the result list.
     */
    public void filterAndSort(SettingUtil.FilterMethod filterMethod,
                              SettingUtil.SortOrder sortOrder,
                              ArrayList<PortalDetail> origin,
                              ArrayList<PortalDetail> edit){
        filterPortalDetails(filterMethod, origin, edit);
        sortPortalDetails(sortOrder, edit);
    }

    /**
     * Filter the list of portal details.
     * @param filterMethod  the filter method to use.
     * @param origin        the origin list to be filter.
     * @param filtered      the filtered list.
     */
    private void filterPortalDetails(SettingUtil.FilterMethod filterMethod,
                                     ArrayList<PortalDetail> origin,
                                     ArrayList<PortalDetail> filtered){
        DoCheck doCheck;

        switch (filterMethod){
            case WAITING:
                doCheck = new DoWaitingCheck();
                break;
            case ACCEPTED:
                doCheck = new DoAcceptedCheck();
                break;
            case REJECTED:
                doCheck = new DoRejectedCheck();
                break;
            case EVERYTHING:
            default:
                doCheck = new DoEveryThingCheck();
        }

        filtered.clear();

        for (PortalDetail detail : origin){
            if (doCheck.checkFilterMethod(detail))
                filtered.add(detail);
        }
    }

    /**
     * Sort the list of portal details.
     * @param sortOrder     the order set.
     * @param portalDetails the sorted list.
     */
    private void sortPortalDetails(SettingUtil.SortOrder sortOrder, ArrayList<PortalDetail> portalDetails){
        switch (sortOrder){
            case DATE_ASC:
                Collections.sort(portalDetails);
                break;
            case DATE_DESC:
                Collections.sort(portalDetails, Collections.reverseOrder());
                break;
            case SMART_ORDER:
                Collections.sort(portalDetails, new Comparator<PortalDetail>() {
                    @Override
                    public int compare(PortalDetail lhs, PortalDetail rhs) {
                        if (!(lhs.isReviewedOrNoResponseForLongTime() ^ rhs.isReviewedOrNoResponseForLongTime()))
                            return lhs.compareTo(rhs);
                        else{
                            if (lhs.isReviewedOrNoResponseForLongTime())
                                return 1;
                            else
                                return -1;
                        }
                    }
                });
        }
    }


    /**
     * Get the accepted count and
     * @param details   the total details.
     * @return          the accepted count and the rejected count. the item 0 is the accepted count,
     *                  the item 1 is the rejected count.
     */
    public int[] getCounts(ArrayList<PortalDetail> details) {
        int[] counts = new int[2];
        counts[0] = 0;
        counts[1] = 0;
        for (PortalDetail detail : details){
            if (detail.isAccepted())
                counts[0]++;
            else if (detail.isRejected())
                counts[1]++;
        }
        return counts;
    }

    private abstract class DoCheck {
        public abstract boolean checkFilterMethod(PortalDetail detail);
    }

    /**
     * a list of classes to do check in filter
     */
    private class DoEveryThingCheck extends DoCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return true;
        }
    }

    private class DoAcceptedCheck extends DoCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.isAccepted();
        }
    }

    private class DoRejectedCheck extends DoCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.isRejected();
        }
    }

    private class DoWaitingCheck extends DoCheck{

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return !detail.isReviewed();
        }
    }
}
