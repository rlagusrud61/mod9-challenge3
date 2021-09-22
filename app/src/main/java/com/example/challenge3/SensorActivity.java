package com.example.challenge3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.rules.DecisionTable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


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

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

};
