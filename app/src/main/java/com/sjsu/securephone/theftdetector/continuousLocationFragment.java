package com.sjsu.securephone.theftdetector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.analytics.Tracker;
import com.sjsu.securephone.theftdetector.Utils.Const;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

/**
 * Created by Group 7 on 10/11/2016.
 */

public class continuousLocationFragment extends Fragment {



    protected Button mStartUpdatesButton, mStopUpdatesButton, mExportDatabase;
    private boolean mIsServiceStarted = false;
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String ACTION_STOP = "STOP_ACTION";
    public static final String ACTION_FROM_NOTIFICATION = "isFromNotification";
    private String action;
    private int notifID;


    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;
    Button btntoggleNetworkUpdates;
    Button btntoggleGPSUpdates;
    Button btntoggleBestUpdates;

    private Tracker mTracker;
    private static final String ARG_TEXT = "text";

    private static final String TAG = "LocationFragment";

    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    String deviceId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.continuous_location_fragment, container, false);

        mStartUpdatesButton = (Button) view.findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) view.findViewById(R.id.stop_updates_button);

        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Network Location Services Started", Toast.LENGTH_LONG).show();
                startUpdatesButtonHandler(v);
            }
        });

        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Network Location Services Stopped", Toast.LENGTH_LONG).show();
                stopUpdatesButtonHandler(v);

            }
        });


        return view;
    }


    public void startUpdatesButtonHandler(View view) {
        if (!mIsServiceStarted) {
            mIsServiceStarted = true;
            setButtonsEnabledState();
            OnGoingLocationNotification(getActivity());
            getActivity().startService(new Intent(new Intent(getActivity(), LocationUpdateService.class)));
        }
    }


    private void setButtonsEnabledState() {
        if (mIsServiceStarted) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }


    public void stopUpdatesButtonHandler(View view) {
        if (mIsServiceStarted) {
            mIsServiceStarted = false;
            setButtonsEnabledState();
            cancelNotification(getActivity(), notifID);
            getActivity().stopService(new Intent(new Intent(new Intent(getActivity(), LocationUpdateService.class))));
        }
    }

    public void exportDatabaseToSdCard(View view) {
        Const.ExportDatabase(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().getIntent().getAction() != null) {
            action = getActivity().getIntent().getAction();
            notifID = getActivity().getIntent().getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            if (action.equalsIgnoreCase(ACTION_FROM_NOTIFICATION)) {
                mIsServiceStarted = true;
                setButtonsEnabledState();

            }
        }
    }


    public static void OnGoingLocationNotification(Context mcontext) {
        int mNotificationId;

        mNotificationId = (int) System.currentTimeMillis();

        Intent mstopReceive = new Intent(mcontext, NotificationHandler.class);
        mstopReceive.putExtra(EXTRA_NOTIFICATION_ID, mNotificationId);
        mstopReceive.setAction(ACTION_STOP);
        PendingIntent pendingIntentStopService = PendingIntent.getBroadcast(mcontext, (int) System.currentTimeMillis(), mstopReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mcontext)
                        .setSound(alarmSound)
                        .setContentTitle("Location Service")
                        .addAction(R.drawable.ic_cancel, "Stop Service", pendingIntentStopService)
                        .setOngoing(true).setContentText("Running...");
        mBuilder.setAutoCancel(false);

        Intent resultIntent = new Intent(mcontext, continuousLocationFragment.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setAction(ACTION_FROM_NOTIFICATION);
        resultIntent.putExtra(EXTRA_NOTIFICATION_ID, mNotificationId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mcontext, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(mNotificationId);

        Notification mNotification = mBuilder.build();
        mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(mNotificationId, mNotification);

    }

    private void cancelNotification(Context mContext, int mnotinotifId) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mnotinotifId);
    }
}


