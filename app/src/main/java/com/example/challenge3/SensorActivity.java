package com.example.challenge3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;

import weka.core.Attribute;
import weka.core.Instances;


public class SensorActivity extends FragmentActivity implements SensorEventListener, View.OnClickListener {

    // sending log output TAG
    private static final String TAG = "MyActivity";

    // Front-End components
    TextView introText1, info_text;
    ImageButton history_tab, again;
    LinearLayout linearLayout;
    ListView history;

    //History
    String[] history_arrayl;

    // Accelerometer, Gyroscope, Linear_acceleration, Magnetometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor linear_acceleration;
    private Sensor magnetometer;

    // 20,000 microseconds = 50Hz
    private final int dt = 20000;

    //List with magnitudes of acceleration
    ArrayList<Double> accel_mag = new ArrayList<Double>();

    // Cvs file
    public ArrayList<Attribute> attributes;
    private final String[] attrList = {"Wrist_Ax", "Wrist_Ay", "Wrist_Az", "Wrist_Lx", "Wrist_Ly", "Wrist_Lz", "Wrist_Gx", "Wrist_Gy", "Wrist_Gz", "Wrist_Mx", "Wrist_My", "Wrist_Mz", "Activity"};
    private final String[] activities = {"walking", "standing", "jogging", "sitting","biking","upstairs","downstairs"};
    Instances liveData = null;


    private Instances createInstances(String name, String[] attList, String[] activityList, int capacity){
        attributes = new ArrayList<>();
        ArrayList<String> activityTemp = new ArrayList<String>();
        for(int i=0; i<attList.length-1;i++){
            attributes.add(new Attribute(attList[i]));
        }
        for(int i=0; i < activityList.length; i++){
            activityTemp.add(activityList[i]);
        }
        attributes.add(new Attribute(attList[attList.length-1], activityTemp));

        return new Instances(name, attributes, capacity);
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize front-end
        introText1 = findViewById(R.id.introText1);
        history_tab = findViewById(R.id.history_tab);
        again = findViewById(R.id.again);
        linearLayout = findViewById(R.id.layout);
        info_text = findViewById(R.id.info_text);

        history =  findViewById(R.id.history);
        history_tab.setOnClickListener(this);


        // Check the user has the right permissions enabled
        checkPermissions((Activity) this);

        // Open Weka model
        try {
            Classifier classifier = (Classifier) weka.core.SerializationHelper.read("RandomTree.model");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start reading values from the sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linear_acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener((SensorEventListener) SensorActivity.this, accelerometer, dt);
        sensorManager.registerListener((SensorEventListener) SensorActivity.this, gyroscope, dt);
        sensorManager.registerListener((SensorEventListener) SensorActivity.this, linear_acceleration, dt);
        sensorManager.registerListener((SensorEventListener) SensorActivity.this, magnetometer, dt);

        liveData = createInstances("test", attrList, activities, 50);
    }

    @Override
    public void onClick(View view) {

        // When this button gets clicked, you will move into the history tab
//        if (view.equals(history_tab)){
//
//        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {

        // Getting the accelerometer values
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float Ax = event.values[0];
            float Ay = event.values[1];
            float Az = event.values[2];

            // Calculating the magnitude of the acceleration
            accel_mag.add(Math.sqrt(Ax * Ax + Ay * Ay + Az * Az) - 9.81);

            double mag = averageAccelerometer(accel_mag);
        }

        // Getting the gyroscope values
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float Gx = event.values[0];
            float Gy = event.values[1];
            float Gz = event.values[2];
        }

        // Getting the linear acceleration values
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float Lx = event.values[0];
            float Ly = event.values[1];
            float Lz = event.values[2];
        }

        // Getting the magnetometer values
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float Mx = event.values[0];
            float My = event.values[1];
            float Mz = event.values[2];
        }



    }

    // calculate average of an arraylist of any size
    private float averageAccelerometer(ArrayList<Double> input) {
        float temp = 0;
        for (int i=0; i<input.size();i++) { // sum all elements
            temp += input.get(i);
        }
        return temp/input.size(); // divide summed up elements by the number of elements
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
