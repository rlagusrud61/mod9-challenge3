package com.example.challenge3;

import android.Manifest;
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
import weka.classifiers.Classifier;



public class SensorActivity extends FragmentActivity implements SensorEventListener {

    // sending log output TAG
    private static final String TAG = "MyActivity";

    // Front-End components
    TextView introText1, info_text;
    ImageButton startButton, again;
    LinearLayout linearLayout;

    // Accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;

    //List with magnitudes of acceleration
    ArrayList<Double> accel_mag = new ArrayList<Double>();

    // Cvs file



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

        // Open Weka model
        try {
            Classifier classifier = (Classifier) weka.core.SerializationHelper.read("RandomTree.model");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start reading values from the accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) SensorActivity.this, accelerometer, 200000);


    }

    @Override
    public final void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // Getting the accelerometer values
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculating the magnitude of the acceleration
            accel_mag.add(Math.sqrt(x * x + y * y + z * z) - 9.81);

            double mag = averageAccelerometer(accel_mag);

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
