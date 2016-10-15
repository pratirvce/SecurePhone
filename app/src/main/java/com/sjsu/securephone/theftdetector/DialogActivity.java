package com.sjsu.securephone.theftdetector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DialogActivity extends AppCompatActivity {

    private static final String TAG = "DialogActivity";
    private Context context;
    private SharedPreferences sharedPref;
    EditText editTextPassword;

    static HashMap<String, String> recorArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        recorArray = new HashMap<String, String>();

        Log.d(TAG, "onCreate()");
    }

    public void onEnter(View view) {
        Log.d(TAG, "onEnter()");

        String password = editTextPassword.getText().toString();

        if(!password.isEmpty() && isPasswordValid(password)){
            String storedPassword = sharedPref.getString(getString(R.string.preferences_password), "null");

            if( storedPassword.equals(password)){
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", true);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
            else{
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",false);
                setResult(Activity.RESULT_CANCELED,returnIntent);
                sendNotification();
                finish();
            }
        }
    }

    public void sendNotification() {
        String email = sharedPref.getString("pref_email", "");
        String notification = "Unauthorized attempted at powering device off.";
        recorArray.put(email, notification);

        Set<?> set = recorArray.entrySet();
        Iterator<?> i = set.iterator();

        while (i.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry me = (Map.Entry) i.next();
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(email, null, notification, null, null);
                System.out.println("message sent");
            } catch (Exception e) {
                System.out.println("sending failed!");
                e.printStackTrace();
            }
        }
    }

    public void onCancel(View view) {
        Log.d(TAG, "onCancel()");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",false);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
