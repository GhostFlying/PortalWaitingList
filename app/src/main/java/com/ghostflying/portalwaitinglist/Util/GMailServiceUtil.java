package com.ghostflying.portalwaitinglist.Util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ghostflying.portalwaitinglist.GMailService;
import com.ghostflying.portalwaitinglist.PortalEventContract;
import com.ghostflying.portalwaitinglist.PortalEventDbHelper;
import com.ghostflying.portalwaitinglist.data.Message;
import com.ghostflying.portalwaitinglist.data.MessageList;
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
import java.util.Date;

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
    private static final String QUERY_MESSAGE_FORMAT = "?format=full";
    private static final String QUERY_MESSAGE_METADATA_DATE = "&metadataHeaders=date";
    private static final String QUERY_MESSAGE_METADATA_SUBJECT = "&metadataHeaders=subject";
    private static final String DEFAULT_AFTER_STR = "1995/07/08";
    private static final String PART_REQUEST_BASE_PATH = "GET /gmail/v1/users/me/messages/";
    private static final String BATCH_REQUEST_URL = "https://www.googleapis.com/batch";
    private static final MediaType HTTP = MediaType.parse("application/http");
    private static GMailServiceUtil instance;
    private Gson gson;
    private GMailService gmailService;
    private String token;

    /**
     * Private constructor.
     * @param token the token to query.
     */
    private GMailServiceUtil(final String token){
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
    public static GMailServiceUtil getInstance(final String token){
        if (instance == null){
            instance = new GMailServiceUtil(token);
        }
        return instance;
    }

    /**
     * Get all messages related to portal submit.
     * @param dbHelper the SQLiteHelper to create a database instance.
     * @return list of Message
     */
    public ArrayList<Message> getPortalMessages(PortalEventDbHelper dbHelper) throws IOException{
        ArrayList<Message> messages = new ArrayList<>();

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
            int page = (messageIds.size() - 1) / 100 + 1;
            // reverse it to get a list in asc order by date.
            for (int i = page; i > 0; i--){
                int count = i == page ? messageIds.size() - 100 * (i -1) : 100;
                messages.addAll(getBatchMessages(messageIds, (i - 1) * 100, count));
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
        ArrayList<Message> messages = new ArrayList<>();
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.MIXED);

        // reverse the id to get a list i asc order by date.
        for (int i = start + count - 1; i > start - 1; i--){
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
        if (RegexUtil.getInstance().isFound(RegexUtil.FIND_BOUNDARY, contentType)){
            String boundary = RegexUtil.getInstance().getMatchedStr();
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
        if (gson == null){
            gson = new Gson();
        }

        if (RegexUtil.getInstance().isFound(RegexUtil.EACH_JSON_IN_BATCH, eachStr))
            return gson.fromJson(RegexUtil.getInstance().getMatchedStr(), Message.class);
        else
            throw new IOException("Parse response " + eachStr + " error, maybe the fetch failed.");
    }

    /**
     * Get all messageIds related portal submit.
     * @param afterStr the string about date to reduce the query response.
     * @return the list of messageId needed.
     */
    private ArrayList<MessageList.MessageId> getAllPortalMessageIds(String afterStr){
        ArrayList<MessageList.MessageId> messageIds = new ArrayList<>();
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
}
