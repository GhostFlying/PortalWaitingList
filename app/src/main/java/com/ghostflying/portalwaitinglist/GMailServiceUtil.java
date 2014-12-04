package com.ghostflying.portalwaitinglist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ghostflying.portalwaitinglist.data.Message;
import com.ghostflying.portalwaitinglist.data.MessageList;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.data.PortalEvent;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * An util class for query and process data.
 */
public class GMailServiceUtil {
    private static final long ONE_DAY_MILLISECONDS = 3600L * 24 * 1000;
    private static final String QUERY_STRING = "from:(super-ops@google.com OR ingress-support@google.com) subject:(Ingress Portal)";
    private static final String QUERY_MESSAGE_FORMAT = "?format=metadata";
    private static final String QUERY_MESSAGE_METADATA_DATE = "&metadataHeaders=date";
    private static final String QUERY_MESSAGE_METADATA_SUBJECT = "&metadataHeaders=subject";
    //private static final String[] QUERY_MESSAGE_METADATA = {QUERY_MESSAGE_METADATA_SUBJECT, QUERY_MESSAGE_METADATA_DATE};
    private static final String REGEX_PORTAL_SUBMIT = "(?<=Ingress Portal Submitted:).+";
    private static final String REGEX_PORTAL_EDIT = "(?<=Ingress Portal Edits Submitted:).+|" + "(?<=Invalid Ingress Portal Report:).+";
    private static final String REGEX_PORTAL_SUBMIT_PASSED = "(?<=Ingress Portal Live:).+";
    private static final String REGEX_PORTAL_SUBMIT_REJECTED = "(?<=Ingress Portal Rejected:).+|" + "(?<=Ingress Portal Duplicate:).+";
    private static final String REGEX_PORTAL_EDIT_PASSED = "(?<=Ingress Portal Data Edit Accepted:).+";
    private static final String REGEX_PORTAL_EDIT_REJECTED = "(?<=Ingress Portal Data Edit Reviewed:).+";
    private static final String REGEX_EACH_JSON_IN_BATCH = "\\{.+\\}";
    private static final String REGEX_FIND_BOUNDARY = "(?<=boundary=).+";
    private static final String DEFAULT_AFTER_STR = "1995/07/08";
    private static final String PART_REQUEST_BASE_PATH = "GET /gmail/v1/users/me/messages/";
    private static final String BATCH_REQUEST_URL = "https://www.googleapis.com/batch";
    private static final MediaType HTTP = MediaType.parse("application/http");
    private static GMailServiceUtil instance;
    private String token;
    private Pattern patternSubmit;
    private Pattern patternEdit;
    private Pattern patternSubmitPassed;
    private Pattern patternSubmitRejected;
    private Pattern patternEditPassed;
    private Pattern patternEditRejected;
    private Pattern patternEachResponse;
    private Pattern patternBoundary;
    private Gson gson;
    private GMailService gmailService;

    /**
     * Private constructor
     */
    private GMailServiceUtil(){

    }

    /**
     * Set the token before query.
     * @param token the token to query.
     */
    public void setToken(final String token){
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "Bearer " + token);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.googleapis.com/gmail/v1")
                .setRequestInterceptor(requestInterceptor)
                .build();
        gmailService = restAdapter.create(GMailService.class);
        this.token = token;
    }

    /**
     * Get an instance of GMailServiceUtil.
     * @return the instance.
     */
    public static GMailServiceUtil getInstance(){
        if (instance == null){
            instance = new GMailServiceUtil();
        }
        return instance;
    }

    /**
     * Get all messages related to portal submit.
     * @param dbHelper the SQLiteHelper to create a database instance.
     * @return list of Message
     */
    public ArrayList<Message> getPortalMessages(PortalEventDbHelper dbHelper) throws IOException{
        ArrayList<Message> messages = new ArrayList<Message>();

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor lastEvent = database.rawQuery(
                "SELECT MAX(" + PortalEventContract.PortalEvent.COLUMN_NAME_DATE
                        + ") AS MAX FROM " + PortalEventContract.PortalEvent.TABLE_NAME,
                null);
        lastEvent.moveToFirst();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        //minus a day to avoid the effect of time zone or other problem.
        String afterStr;
        if (lastEvent.getLong(0) != 0){
            afterStr = dateFormat.format(new Date(lastEvent.getLong(0) - ONE_DAY_MILLISECONDS));
        }
        else {
            afterStr = DEFAULT_AFTER_STR;
        }

        lastEvent.close();

        ArrayList<MessageList.MessageId> messageIds = getAllPortalMessageIds("after:" + afterStr);

        // remove all exist messageId
        removeDuplicate(messageIds, database);

        database.close();

        // Query and add Message to list.
        if (messageIds.size() > 0){
            for (int i = 0; i < (messageIds.size()/100 + 1); i++){
                int count = (messageIds.size() > (i + 1) * 100) ? 100 : messageIds.size() - 100 * i;
                messages.addAll(getBatchMessages(messageIds, i * 100, count));
            }
        }

        return messages;
    }

    /**
     * Get messages by batch request
     * @param messageIds    the id list.
     * @param start          start index.
     * @param count          count.
     * @return                the parsed messages.
     * @throws IOException    the response has error.
     */
    private ArrayList<Message> getBatchMessages(ArrayList<MessageList.MessageId> messageIds,
                                                int start, int count) throws IOException{
        OkHttpClient client = new OkHttpClient();
        ArrayList<Message> messages = new ArrayList<Message>();
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.MIXED);

        for (int i = start; i < start + count; i++){
            byte[] request = (PART_REQUEST_BASE_PATH
                    + messageIds.get(i).getId()
                    + QUERY_MESSAGE_FORMAT
                    + QUERY_MESSAGE_METADATA_DATE
                    + QUERY_MESSAGE_METADATA_SUBJECT
                    + "\n").getBytes();
            RequestBody partRequest = RequestBody.create(HTTP, request);
            builder.addPart(partRequest);
        }

        Request request = new Request.Builder()
                .url(BATCH_REQUEST_URL)
                .post(builder.build())
                .header("Authorization", "Bearer " + token)
                .build();
        Response response = client.newCall(request).execute();
        String contentType = response.header("Content-Type");
        String responseBody = response.body().string();
        if (patternBoundary == null){
            patternBoundary = Pattern.compile(REGEX_FIND_BOUNDARY);
        }
        Matcher matcher = patternBoundary.matcher(contentType);
        if (matcher.find()){
            String boundary = matcher.group();
            String[] eachResponses = responseBody.split("--" + boundary);

            for (int i = 1; i < count + 1; i++){
                messages.add(parseEachInBatch(eachResponses[i]));
            }
            return messages;
        }
        else
            throw new IOException("Can not found the boundary, maybe request failed.");
    }

    private Message parseEachInBatch(String eachStr) throws IOException{
        // Initial the gson and pattern.
        if (patternEachResponse == null){
            patternEachResponse = Pattern.compile(REGEX_EACH_JSON_IN_BATCH, Pattern.DOTALL);
            gson = new Gson();
        }

        Matcher matcher = patternEachResponse.matcher(eachStr);
        if (matcher.find())
            return gson.fromJson(matcher.group(), Message.class);
        else
            throw new IOException("Parse response " + eachStr + " error, maybe the fetch failed.");
    }

    /**
     * Get all messageIds related portal submit.
     * @param afterStr the string about date to reduce the query response.
     * @return the list of messageId needed.
     */
    private ArrayList<MessageList.MessageId> getAllPortalMessageIds(String afterStr){
        ArrayList<MessageList.MessageId> messageIds = new ArrayList<MessageList.MessageId>();
        MessageList messageList;
        String pageToken = null;
        do{
            messageList = gmailService.getMessages(QUERY_STRING + afterStr, pageToken);
            messageIds.addAll(messageList.getMessages());
            pageToken = messageList.getNextPageToken();
        }while (messageList.hasNextPage());
        return messageIds;
    }

    /**
     * Remove the fetched messageId
     * @param ids the messageId list.
     * @param database the SQLite instance to query.
     */
    private void removeDuplicate(ArrayList<MessageList.MessageId> ids, SQLiteDatabase database){
        for (MessageList.MessageId id : ids.toArray(new MessageList.MessageId[ids.size()])){
            Cursor existEvent = database.query(
                    PortalEventContract.PortalEvent.TABLE_NAME,
                    new String[]{PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID},
                    PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID + " = ?",
                    new String[]{id.getId()},
                    null,
                    null,
                    null,
                    "1"
            );
            if (existEvent.moveToFirst())
                ids.remove(id);
            existEvent.close();
        }
    }

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
        if (patternSubmit == null){
            patternSubmit = Pattern.compile(REGEX_PORTAL_SUBMIT);
            patternEdit = Pattern.compile(REGEX_PORTAL_EDIT);
            patternSubmitPassed = Pattern.compile(REGEX_PORTAL_SUBMIT_PASSED);
            patternSubmitRejected = Pattern.compile(REGEX_PORTAL_SUBMIT_REJECTED);
            patternEditPassed = Pattern.compile(REGEX_PORTAL_EDIT_PASSED);
            patternEditRejected = Pattern.compile(REGEX_PORTAL_EDIT_REJECTED);
        }

        Matcher matcher;

        // Try to match regex.
        matcher = patternSubmit.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.SUBMIT,
                    PortalEvent.OperationResult.PROPOSED, message.getDate(), message.getId());
        }

        matcher = patternEdit.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.EDIT,
                    PortalEvent.OperationResult.PROPOSED, message.getDate(), message.getId());
        }

        matcher = patternSubmitPassed.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.SUBMIT,
                    PortalEvent.OperationResult.PASSED, message.getDate(), message.getId());
        }

        matcher = patternSubmitRejected.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.SUBMIT,
                    PortalEvent.OperationResult.REJECTED, message.getDate(), message.getId());
        }

        matcher = patternEditPassed.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.EDIT,
                    PortalEvent.OperationResult.PASSED, message.getDate(), message.getId());
        }

        matcher = patternEditRejected.matcher(message.getSubject());
        if (matcher.find()){
            return new PortalEvent(matcher.group().trim(), PortalEvent.OperationType.EDIT,
                    PortalEvent.OperationResult.REJECTED, message.getDate(), message.getId());
        }

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
