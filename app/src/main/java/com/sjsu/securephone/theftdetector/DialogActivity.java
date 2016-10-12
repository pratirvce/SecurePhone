package com.sjsu.securephone.theftdetector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DialogActivity extends AppCompatActivity {

    private static final String TAG = "DialogActivity";
    private Context context;
    private SharedPreferences sharedPref;
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        // initialize the view
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        // initialize SharedPreferences
        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

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
                Log.e(TAG, "User entered incorrect password");
                finish();
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
}
