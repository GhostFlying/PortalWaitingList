package com.ghostflying.ingressmailanalysis;

import com.ghostflying.ingressmailanalysis.data.Message;
import com.ghostflying.ingressmailanalysis.data.MessageList;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by ghostflying on 11/19/14.
 * <br>
 * GMail API interface, used by retrofit to achieve.
 */
public interface GMailService {
    @GET("/users/me/messages")
    public MessageList getMessages(@Query("q") String query, @Query("pageToken") String pageToken);

    @GET("/users/me/messages/{id}")
    public Message getMessage(@Path("id") String id, @Query("format")String format, @Query("metadataHeaders") String[] metadataHeaders);

}
