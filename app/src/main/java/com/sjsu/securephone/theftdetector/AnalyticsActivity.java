package com.sjsu.securephone.theftdetector;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by Group 7 on 10/11/2016.
 */

public class AnalyticsActivity extends Application {
    private static final String PROPERTY_ID = "UA-71837612-6";
    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        E_COMMERCE_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = analytics.newTracker(PROPERTY_ID);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }


}

