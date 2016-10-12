package com.sjsu.securephone.theftdetector;

import android.app.Activity;
import android.content.Intent;
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
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        Log.d(TAG, "onCreate()");
    }

    public void onEnter(View view) {
        Log.d(TAG, "onEnter()");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", true);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

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
}
