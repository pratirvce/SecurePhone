package com.sjsu.securephone.theftdetector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.List;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Group 7 on 10/11/2016.
 */

public class MainActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks{

    private boolean doubleBackToExitPressedOnce=false;

    private static final int RC_READ_SMS = 101;

    private static final String TAG = "MainActivity";
    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);
        readSMSPerm();

        // initialize SharedPreferences
        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // check if user has logged on before
//        boolean loggedOn = sharedPref.getBoolean(getString(R.string.preference_logged_on_key), false);
//        if(!loggedOn){
//            //start the login activity
//            Intent loginActivity = new Intent(this, LoginActivity.class);
//            startActivity(loginActivity);
//        }

        // used for the allowPower flag
        // by default, when app is started, the flag will always be false
        editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.preference_allow_power_key), false);
        editor.commit();

    }

    @Override
    protected void onResume(){
        super.onResume();
        // check if user has logged on before
        boolean loggedOn = sharedPref.getBoolean(getString(R.string.preference_logged_on_key), false);
        if(!loggedOn){
            //start the login activity
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SIMDetectorFragment(), "SIM Info");
        //adapter.addFrag(new LocationFragment(), "Location Info");
        adapter.addFrag(new continuousLocationFragment(), "Location Info");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
            return true;
        }
       /* if(id==R.id.action_share){
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = getString(R.string.share_text);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));


        } */
        return super.onOptionsItemSelected(item);
    }
    public void readSMSPerm() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            sendAndOpenIntent();
        } else {
            // Ask for both permissions
            Log.d("else", "part");
            EasyPermissions.requestPermissions(this, "Allow Sim Info read?",
                    RC_READ_SMS, perms);
        }
    }

    public void sendAndOpenIntent() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
// EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("", "onPermissionsGranted:" + requestCode + ":" + perms.size());
        sendAndOpenIntent();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        Toast.makeText(getApplicationContext(), "Permission is Compulsory to Proceed", Toast.LENGTH_SHORT).show();
    }

    //*********************************************************************************************
    //  START CODE: Theft Detection
    //*********************************************************************************************

    //  detects long press of power button, dismisses power menu, launches password prompt activity
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                // get the allowPower flag
                boolean allowPower = sharedPref.getBoolean(getString(R.string.preference_allow_power_key), false);
                Log.d(TAG, "dispatchKeyEvent, allowPower: " + Boolean.toString(allowPower));

                if(!allowPower) {
                    Log.d(TAG, "dispatchKeyEvent KeyEvent.KEYCODE_POWER");

                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    sendBroadcast(closeDialog);

                    Intent dialogIntent = new Intent(this, DialogActivity.class);
                    startActivityForResult(dialogIntent, 1);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    // decides what to do based on password input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if(resultCode == Activity.RESULT_OK){
                boolean result = data.getBooleanExtra("result", false);

                if(result) {
                    // update SharedPreferences so that the flag is true now
                    editor.putBoolean(getString(R.string.preference_allow_power_key), true);
                    editor.commit();

                    // start a counter service which will change the flag back to false after 30 seconds
                    startService(new Intent(this, FlagCounterService.class));

                    // password is entered correctly so prompt user to power off again
                    // display Dialog with this message
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage("Password is correct. Please hold the power button again.");
                    alertDialogBuilder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "User must try to power off again.");
                        }
                    });
                    alertDialogBuilder.create();
                    alertDialogBuilder.show();

                    // log tracking information in firebase
                    Long tsLong = System.currentTimeMillis()/1000;
                    String deviceId = sharedPref.getString(getString(R.string.preferences_device_id), "null");

                    Firebase.setAndroidContext(this);
                    Firebase ref = new Firebase(Config.FIREBASE_URL);
                    ref.child(deviceId).child("tracking_correct_pw").child(tsLong.toString()).setValue("location1");

                    /*
                    Log.d(TAG, "Password entered is correct. Continue to power dialog.");

                    // try to simulate the power button long press so the power dialog can be displayed
                    KeyEvent kdown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER);
                    KeyEvent test = new KeyEvent(2805465, uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 2);

                    super.dispatchKeyEvent(test);
                    onKeyLongPress(KeyEvent.KEYCODE_POWER, kdown);
                    */
                }
                else if(!result){
                    Log.d(TAG, "User cannot power off.");

                    Long tsLong = System.currentTimeMillis()/1000;
                    String deviceId = sharedPref.getString(getString(R.string.preferences_device_id), "null");

                    Firebase.setAndroidContext(this);
                    Firebase ref = new Firebase(Config.FIREBASE_URL);
                    ref.child(deviceId).child("tracking_cancelled").child(tsLong.toString()).setValue("locationx");
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                // log tracking information in firebase
                Long tsLong = System.currentTimeMillis()/1000;
                String deviceId = sharedPref.getString(getString(R.string.preferences_device_id), "null");

                Firebase.setAndroidContext(this);
                Firebase ref = new Firebase(Config.FIREBASE_URL);
                ref.child(deviceId).child("tracking_incorrect_pw").child(tsLong.toString()).setValue("location1");

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Password is incorrect!");
                alertDialogBuilder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            }
        }
    }
    //*********************************************************************************************
    //  END CODE: Theft Detection
    //*********************************************************************************************

}
