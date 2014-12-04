package com.ghostflying.portalwaitinglist.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ghostflying on 11/20/14.
 * <br>
 * The data structure of each mail, this only contain the data needed.
 */
public class Message {
    private static final String SUBJECT_NAME = "Subject";
    private static final String SUBJECT_DATE = "Date";

    Payload payload;
    String id;

    public String getId(){
        return id;
    }

    public String getSubject(){
        for(Header header : payload.headers){
            if (header.name.equals(SUBJECT_NAME))
                return header.value;
        }
        return null;
    }

    public Date getDate(){
        for(Header header : payload.headers){
            if (header.name.equals(SUBJECT_DATE)){
                Date date = null;
                try{
                    date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).parse(header.value);
                }
                catch (ParseException e){
                    e.printStackTrace();
                }
                return date;
            }
        }
        return null;
    }

    public String getMessageHtml(){
        return null;
    }

    private class Payload{
        Header[] headers;
    }

    private class Header{
        String name;
        String value;
    }
}
