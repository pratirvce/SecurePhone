package com.sjsu.securephone.theftdetector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends Activity {


    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private static final String TAG = "LoginActivity";
    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();

        editor.putString(getString(R.string.preferences_device_id), deviceId);
        editor.commit();

        Log.d(TAG, "onCreate()");
    }

    public void onSignIn(View view){
        Log.d(TAG, "onSignIn");
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if( ((!email.isEmpty()) && (!password.isEmpty())) && (isEmailValid(email) && isPasswordValid(password))){
            editor.putString(getString(R.string.preferences_email_key), email);
            editor.putString(getString(R.string.preferences_password), password);
            editor.putBoolean(getString(R.string.preference_logged_on_key), true);
            editor.commit();

            if( (deviceId != null) && (!deviceId.isEmpty()) ){
                Firebase.setAndroidContext(this);
                Firebase ref = new Firebase(Config.FIREBASE_URL);
                ref.child(deviceId).child("email").setValue(email);
                ref.child(deviceId).child("password").setValue(password);

            }



            Log.d(TAG, "Sign in is complete");
            finish();
        }

    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

