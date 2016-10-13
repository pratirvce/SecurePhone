package com.sjsu.securephone.theftdetector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.Firebase;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Group 7 on 10/11/2016.
 */

public class LocationFragment extends Fragment {
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    private Tracker mTracker;
    private static final String ARG_TEXT = "text";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.location_info, container, false);
        /*Google Analytics: send screen Name*/

        locationManager = (LocationManager) getView().getSystemService(Context.LOCATION_SERVICE);

        longitudeValueBest = (TextView) getView().findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) getView().findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = (TextView) getView().findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) getView().findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = (TextView) getView().findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = (TextView) getView().findViewById(R.id.latitudeValueNetwork);
    }
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerGPS);
            button.setText(R.string.resume);
        }
        else {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 10, locationListenerGPS);
            button.setText(R.string.pause);
        }
    }

    public void toggleBestUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerBest);
            button.setText(R.string.resume);
        }
        else {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            if(provider != null) {
                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListenerBest);
                button.setText(R.string.pause);
                Toast.makeText(this, "Best Provider is " + provider, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void toggleNetworkUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerNetwork);
            button.setText(R.string.resume);
        }
        else {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 60 * 1000, 10, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();
            button.setText(R.string.pause);
        }
    }


    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueBest.setText(longitudeBest + "");
                    latitudeValueBest.setText(latitudeBest + "");
                    //Creating firebase object
                    Firebase ref = new Firebase(Config.FIREBASE_URL);
                    //Storing values to firebase
                    ref.child("currLoc/Longitude").setValue(longitudeBest);
                    ref.child("currLoc/Latitude").setValue(latitudeBest);
                    Toast.makeText(MainActivity.this, "Best Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }
        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueNetwork.setText(longitudeNetwork + "");
                    latitudeValueNetwork.setText(latitudeNetwork + "");
                    //Creating firebase object
                    Firebase ref = new Firebase(Config.FIREBASE_URL);
                    //Storing values to firebase
                    ref.child("currLoc/Longitude").setValue(longitudeNetwork);
                    ref.child("currLoc/Latitude").setValue(latitudeNetwork);
                    Toast.makeText(MainActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            //Creating firebase object
            Firebase ref = new Firebase(Config.FIREBASE_URL);
            //Storing values to firebase
            ref.child("currLoc/Longitude").setValue(longitudeGPS);
            ref.child("currLoc/Latitude").setValue(latitudeGPS);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueGPS.setText(longitudeGPS + "");
                    latitudeValueGPS.setText(latitudeGPS + "");
                    //Creating firebase object
                    Firebase ref = new Firebase(Config.FIREBASE_URL);
                    //Storing values to firebase
                    ref.child("Longitude").setValue(longitudeGPS);
                    ref.child("Latitude").setValue(latitudeGPS);
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

}

