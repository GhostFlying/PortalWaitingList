package com.ghostflying.portalwaitinglist;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ghostflying on 12/13/14.
 */
public class MyApp extends Application {
    public synchronized Tracker getTracker(){
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        return analytics.newTracker(R.xml.exception_tracker);
    }
}
