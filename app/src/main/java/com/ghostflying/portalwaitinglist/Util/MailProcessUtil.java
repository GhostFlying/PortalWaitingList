package com.ghostflying.portalwaitinglist.Util;

import android.util.Base64;

import com.ghostflying.portalwaitinglist.data.EditEvent;
import com.ghostflying.portalwaitinglist.data.InvalidEvent;
import com.ghostflying.portalwaitinglist.data.Message;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.data.PortalEvent;
import com.ghostflying.portalwaitinglist.data.SubmissionEvent;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Ghost on 2014/12/4.
 */
public class MailProcessUtil {
    private static MailProcessUtil instance;
    static final String PARSE_ERROR_TEXT = "Parse error, maybe the mail is too old to contain this part.";
    int acceptedCount;
    int rejectedCount;
    int proposedCount;

    private MailProcessUtil(){}

    public static MailProcessUtil getInstance(){
        if(instance == null)
            instance = new MailProcessUtil();
        return instance;
    }

    /**
     * Convert messages to events.
     * @param messages the origin messages.
     * @return the converted events.
     */
    public ArrayList<PortalEvent> analysisMessages(ArrayList<Message> messages){
        ArrayList<PortalEvent> portalEvents = new ArrayList<>();

        acceptedCount = 0;
        rejectedCount = 0;
        proposedCount = 0;

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

        // Try to match regex.
        String subject = message.getSubject();
        RegexUtil util = RegexUtil.getInstance();
        if (util.isFound(RegexUtil.PORTAL_SUBMISSION, subject)){
            proposedCount ++;
            return new SubmissionEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.PROPOSED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_EDIT, subject)){
            proposedCount ++;
            return new EditEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.PROPOSED, message.getDate(), message.getId());
        }

        if (util.isFound(RegexUtil.INVALID_REPORT, subject)){
            proposedCount ++;
            return new InvalidEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.PROPOSED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_SUBMISSION_PASSED, subject)){
            acceptedCount ++;
            return new SubmissionEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.ACCEPTED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()),
                    getPortalAddress(message.getMessageHtml()),
                    getPortalAddressUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_SUBMISSION_REJECTED, subject)){
            rejectedCount ++;
            return new SubmissionEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.REJECTED,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_SUBMISSION_DUPLICATE, subject)){
            rejectedCount ++;
            return new SubmissionEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.DUPLICATE,
                    message.getDate(),
                    message.getId(),
                    getImageUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_EDIT_PASSED, subject)){
            acceptedCount ++;
            return new EditEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.ACCEPTED,
                    message.getDate(), message.getId(),
                    getPortalAddress(message.getMessageHtml()),
                    getPortalAddressUrl(message.getMessageHtml()));
        }

        if (util.isFound(RegexUtil.PORTAL_EDIT_REJECTED, subject)){
            rejectedCount ++;
            return new EditEvent(util.getMatchedStr().trim(),
                    PortalEvent.OperationResult.REJECTED,
                    message.getDate(), message.getId(),
                    getPortalAddress(message.getMessageHtml()),
                    getPortalAddressUrl(message.getMessageHtml()));
        }

        return null;
    }

    private String getImageUrl(String html){
        String decodeStr = decodeMailHtml(html);
        if (RegexUtil.getInstance().isFound(RegexUtil.IMG_URL, decodeStr))
            return RegexUtil.getInstance().getMatchedStr();
        return PARSE_ERROR_TEXT;
    }


    private String getPortalAddress(String html){
        String decodeStr = decodeMailHtml(html);
        if (RegexUtil.getInstance().isFound(RegexUtil.ADDRESS, decodeStr))
            return RegexUtil.getInstance().getMatchedStr();
        return PARSE_ERROR_TEXT;
    }

    private String getPortalAddressUrl(String html){
        String decodeStr = decodeMailHtml(html);
        if (RegexUtil.getInstance().isFound(RegexUtil.ADDRESS_URL, decodeStr))
            return RegexUtil.getInstance().getMatchedStr();
        return PARSE_ERROR_TEXT;
    }

    private String decodeMailHtml(String html) {
        byte[] decodeBytes = Base64.decode(html, Base64.URL_SAFE);
        return new String(decodeBytes);
    }

    /**
     * Merge new events to exist portal detail or create new one.
     * To improve performance, the method is designed to work fine only if
     * the events is newer than each one in origin and the event in events is in
     * asc order by date.
     * @param origin exist portal detail list.
     * @param events events need to be merged.
     * @return the merged portal detail list.
     */
    public ArrayList<PortalDetail> mergeEvents(ArrayList<PortalDetail> origin, ArrayList<PortalEvent> events){
        for (PortalEvent event : events){
            PortalDetail existDetail = findExistDetail(origin, event);
            if (existDetail == null){
                PortalDetail newDetail = new PortalDetail(event);
                origin.add(newDetail);
            }
            else {
                existDetail.addEvent(event);
                // sort the event list.
                // Collections.sort(existDetail.getEvents());
            }
        }
        return origin;
    }

    /**
     * Find the exist portal detail has the same name with new event name.
     * To improve performance, the method is designed to work fine
     * only if the new event is newer than any one in details.
     * @param details   exist portal detail list.
     * @param event     new event.
     * @return  the portal detail if exists, or null if it does not exist.
     */
    private PortalDetail findExistDetail(ArrayList<PortalDetail> details, PortalEvent event){
        for (PortalDetail detail : details){
            if (detail.getName().equalsIgnoreCase(event.getPortalName())){
                // if event is Edit, only check name.
                // it may lead some mistakes but have no ideas now.
                if (event instanceof EditEvent)
                    return detail;
                else {
                    String url = detail.getImageUrl();
                    // for submission event, must check the image url of the portal.
                    // this may have some mistakes for the lack of some mails, ignored.
                    if (url != null &&
                            url.equalsIgnoreCase(((SubmissionEvent)event).getPortalImageUrl())){
                        return detail;
                    }
                }
            }
        }
        return null;
    }


    /**
     * To sort and filter origin and store result to edit.
     * @param typeFilterMethod    the type filter mehod to use.
     * @param resultFilterMethod  the result filter method to use.
     * @param sortOrder     the sort order to use.
     * @param origin        the origin list.
     * @param edit          the result list.
     */
    public void filterAndSort(SettingUtil.TypeFilterMethod typeFilterMethod,
                              SettingUtil.ResultFilterMethod resultFilterMethod,
                              SettingUtil.SortOrder sortOrder,
                              ArrayList<PortalDetail> origin,
                              ArrayList<PortalDetail> edit){
        filterPortalDetails(typeFilterMethod, resultFilterMethod, origin, edit);
        sortPortalDetails(sortOrder, edit);
    }

    /**
     * Filter the list of portal details.
     * @param typeFilterMethod    the type filter method to use.
     * @param resultFilterMethod  the result filter method to use.
     * @param origin        the origin list to be filter.
     * @param filtered      the filtered list.
     */
    private void filterPortalDetails(SettingUtil.TypeFilterMethod typeFilterMethod,
                                     SettingUtil.ResultFilterMethod resultFilterMethod,
                                     ArrayList<PortalDetail> origin,
                                     ArrayList<PortalDetail> filtered){
        DoResultCheck doResultCheck;
        DoTypeCheck doTypeCheck;

        switch (resultFilterMethod){
            case WAITING:
                doResultCheck = new DoWaitingCheck();
                break;
            case ACCEPTED:
                doResultCheck = new DoAcceptedCheck();
                break;
            case REJECTED:
                doResultCheck = new DoRejectedCheck();
                break;
            case EVERYTHING:
            default:
                doResultCheck = new DoEveryThingCheck();
        }

        switch (typeFilterMethod){
            case ALL:
                doTypeCheck = new DoAllCheck();
                break;
            case SUBMISSION:
                doTypeCheck = new DoSubmissionCheck();
                break;
            case EDIT:
                doTypeCheck = new DoEditCheck();
                break;
            default:
                doTypeCheck = new DoAllCheck();
        }

        filtered.clear();

        for (PortalDetail detail : origin){
            if (doTypeCheck.checkFilterMethod(detail)
                    && doResultCheck.checkFilterMethod(detail))
                filtered.add(detail);
        }
    }

    /**
     * Sort the list of portal details.
     * @param sortOrder     the order set.
     * @param portalDetails the sorted list.
     */
    public void sortPortalDetails(SettingUtil.SortOrder sortOrder, ArrayList<PortalDetail> portalDetails){
        switch (sortOrder){
            case LAST_DATE_ASC:
                Collections.sort(portalDetails);
                break;
            case LAST_DATE_DESC:
                Collections.sort(portalDetails, Collections.reverseOrder());
                break;
            case SMART_ORDER:
                Collections.sort(portalDetails, new Comparator<PortalDetail>() {
                    @Override
                    public int compare(PortalDetail lhs, PortalDetail rhs) {
                        int lhsPri = lhs.getOrderPrior();
                        int rhsPri = rhs.getOrderPrior();
                        if (lhsPri == rhsPri){
                            // asc for waiting portal, desc for others
                            // if if inverse waiting in smart set to true
                            // all is desc
                            if (lhsPri == PortalDetail.PRIORITY_WAITING_FOR_REVIEW
                                    && !SettingUtil.getIfInverseWaitingInSmart())
                                return lhs.compareTo(rhs);
                            else
                                return rhs.compareTo(lhs);
                        }
                        // sort by the priority of portal
                        else
                            return rhsPri - lhsPri;
                    }
                });
                break;
            case ALPHABETICAL:
                // get the comparator by locale
                final Comparator comparator;
                if (SettingUtil.getForceChinese()){
                    comparator = Collator.getInstance(Locale.CHINA);
                }
                else {
                    comparator = Collator.getInstance();
                }
                Collections.sort(portalDetails, new Comparator<PortalDetail>() {
                    @Override
                    public int compare(PortalDetail lhs, PortalDetail rhs) {
                        return comparator.compare(lhs.getName(), rhs.getName());
                    }
                });
                break;
            case PROPOSED_DATE_ASC:
                Collections.sort(portalDetails, new Comparator<PortalDetail>() {
                    @Override
                    public int compare(PortalDetail lhs, PortalDetail rhs) {
                        return lhs.getLastProposedUpdated()
                                .compareTo(rhs.getLastProposedUpdated());
                    }
                });
                break;
            case PROPOSED_DATE_DESC:
                Collections.sort(portalDetails, new Comparator<PortalDetail>() {
                    @Override
                    public int compare(PortalDetail lhs, PortalDetail rhs) {
                        return rhs.getLastProposedUpdated()
                                .compareTo(lhs.getLastProposedUpdated());
                    }
                });
                break;
        }
    }


    /**
     * Get the accepted count and
     * @param details   the total details.
     * @return          the accepted count and the rejected count.
     *                  the item 0 is the accepted count,
     *                  the item 1 is the rejected count,
     *                  the item 2 is the submission count,
     *                  the item 3 is the edit count.
     */
    public int[] getCounts(ArrayList<PortalDetail> details) {
        int[] counts = new int[4];
        counts[0] = 0;
        counts[1] = 0;
        for (PortalDetail detail : details){
            if (detail.isEverAccepted())
                counts[0]++;
            else if (detail.isRejected())
                counts[1]++;
            if (detail.hasSubmission())
                counts[2]++;
            else
                counts[3]++;
        }
        return counts;
    }

    /**
     * Get the counts in last process action.
     * @return  the array of counts, int order proposed, accepted, rejected.
     */
    public int[] getEventCountsInLastProcess(){
        return new int[]{
                proposedCount,
                acceptedCount,
                rejectedCount
        };
    }


    /**
     * a list of classes to do check in filter
     */

    private abstract class DoCheck {
        public abstract boolean checkFilterMethod(PortalDetail detail);
    }

    private abstract class DoResultCheck extends DoCheck{

    }

    private abstract class DoTypeCheck extends DoCheck{

    }

    private class DoEveryThingCheck extends DoResultCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return true;
        }
    }

    private class DoAcceptedCheck extends DoResultCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.isEverAccepted();
        }
    }

    private class DoRejectedCheck extends DoResultCheck {

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.isRejected();
        }
    }

    private class DoWaitingCheck extends DoResultCheck{

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return !detail.isReviewed();
        }
    }

    private class DoAllCheck extends DoTypeCheck{

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return true;
        }
    }

    private class DoSubmissionCheck extends DoTypeCheck{

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.hasSubmission();
        }
    }

    private class DoEditCheck extends DoTypeCheck{

        @Override
        public boolean checkFilterMethod(PortalDetail detail) {
            return detail.hasEdit();
        }
    }
}
