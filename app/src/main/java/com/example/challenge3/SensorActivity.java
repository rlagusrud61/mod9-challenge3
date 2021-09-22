package com.example.challenge3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class SensorActivity extends FragmentActivity  {

    // sending log output TAG
    private static final String TAG = "MyActivity";

    // Front-End components
    public TextView introText1, info_text;
    public ImageButton startButton, again;
    public LinearLayout linearLayout;
    public SensorManager sensorManager;

    @Override
    public final void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize front-end
        introText1 = findViewById(R.id.introText1);
        startButton = findViewById(R.id.startButton);
        again = findViewById(R.id.again);
        linearLayout = findViewById(R.id.layout);
        info_text = findViewById(R.id.info_text);

        // Check the user has the right permissions enabled
        checkPermissions((Activity) this);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorData sensorData = new SensorData(sensorManager);
        // Open Weka model
        try {
            Classifier classifier = (Classifier) weka.core.SerializationHelper.read(getAssets().open("../RandomTree.model"));
            classifier.classifyInstance(sensorData);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean checkPermissions(Activity activity){

        // Permissions from reading and loading accelerometer data into file
        Boolean readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (readPermission & writePermission){
            return true;
        }
        ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        return false;

    }

};
