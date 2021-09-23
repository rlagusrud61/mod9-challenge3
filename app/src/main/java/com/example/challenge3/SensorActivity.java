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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import weka.core.Attribute;
import weka.filters.unsupervised.attribute.Add;
import weka.gui.beans.ClassifierBeanInfo;


public class SensorActivity extends FragmentActivity implements SensorEventListener, View.OnClickListener {

    // sending log output TAG
    private static final String TAG = "MyActivity";

    // Front-End components
    TextView introText1, info_text;
    ImageButton history_tab, again;
    LinearLayout linearLayout;
    ListView history;


    // Accelerometer, Gyroscope, Linear_acceleration, Magnetometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor linear_acceleration;
    private Sensor magnetometer;
    float Ax,Ay,Az,Lx,Ly,Lz,Mx,My,Mz,Gx,Gy,Gz;
    ArrayList<Attribute> fvWekaAttributes ;

    Instances instances;
    Instance instance;

    public static int NUMBER_OF_ATTRIBUTES = 13;
    // 20,000 microseconds = 50Hz
    private final int dt = 20000;
    private final int NUMBER_OF_READINGS = 150;

    Classifier cls;
    HashMap<Integer,Integer> readings;

    private final String[] activities = {"walking", "standing", "jogging", "sitting","biking","upstairs","downstairs"};

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
//        checkPermissions((Activity) this);

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

        createTrainingSet();
        initiateReadings();

        instances = new Instances("Bruh", fvWekaAttributes, 5);
        instances.setClassIndex(NUMBER_OF_ATTRIBUTES-1);
        try {
            cls = (Classifier) weka.core.SerializationHelper.read(getAssets().open("bayesNetNiels.model"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void initiateReadings(){
        readings = new HashMap<>();
        for (int i = 0 ; i < 7 ; i ++){
            readings.put(i, 0);
        }
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
        Log.d(TAG, "onSensorchanged");
        // Getting the accelerometer values
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            Ax = event.values[0];
            Ay = event.values[1];
            Az = event.values[2];
            Log.d(TAG, "Ax : " + Ax + " // Ay : " + Ay + " // Az : " + Az);

        }

        // Getting the gyroscope values
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Gx = event.values[0];
            Gy = event.values[1];
            Gz = event.values[2];
        }

        // Getting the linear acceleration values
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            Lx = event.values[0];
            Ly = event.values[1];
            Lz = event.values[2];
        }

        // Getting the magnetometer values
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            Mx = event.values[0];
            My = event.values[1];
            Mz = event.values[2];
        }
        classifyInstance();
        getPredictedActivity();
    }


    public int getActivityWithMostOccurrence(){
        // Get the activity with the most occurrence
        int maxPrediction = 0;
        for (Map.Entry<Integer, Integer> entry : readings.entrySet()){
            if (entry.getValue().compareTo(entry.getValue()) > 0)
            {
                // might produce nullpointer
                maxPrediction = entry.getKey();
            }
        }return maxPrediction;
    }
    public void getPredictedActivity(){
        int sum = 0;
        for (int v: readings.values()){
            sum += v;
        }

        //if it reaches 150 readings
        if (sum == NUMBER_OF_READINGS){
            int prediction = getActivityWithMostOccurrence();
            introText1.setText("You are most likely " + activities[prediction]);
            Log.d(TAG, "You are most likely " + activities[prediction]);
            readings.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void createTrainingSet(){

        Log.d(TAG, "Created training set...");
        // Create the attributes
        Attribute Wrist_Ax = new Attribute("Wrist_Ax");
        Attribute Wrist_Ay = new Attribute("Wrist_Ay");
        Attribute Wrist_Az = new Attribute("Wrist_Az");
        Attribute Wrist_Lx = new Attribute("Wrist_Lx");
        Attribute Wrist_Ly = new Attribute("Wrist_Ly");
        Attribute Wrist_Lz = new Attribute("Wrist_Lz");
        Attribute Wrist_Gx = new Attribute("Wrist_Gx");
        Attribute Wrist_Gy = new Attribute("Wrist_Gy");
        Attribute Wrist_Gz = new Attribute("Wrist_Gz");
        Attribute Wrist_Mx = new Attribute("Wrist_Mx");
        Attribute Wrist_My = new Attribute("Wrist_My");
        Attribute Wrist_Mz = new Attribute("Wrist_Mz");
        Attribute Activity = new Attribute("Activity");

        // Creating a vector with 13 positions.
        fvWekaAttributes = new ArrayList<>(NUMBER_OF_ATTRIBUTES);

        // For each position, we add an attribute
        fvWekaAttributes.add(Wrist_Ax);
        fvWekaAttributes.add(Wrist_Ay);
        fvWekaAttributes.add(Wrist_Az);
        fvWekaAttributes.add(Wrist_Lx);
        fvWekaAttributes.add(Wrist_Ly);
        fvWekaAttributes.add(Wrist_Lz);
        fvWekaAttributes.add(Wrist_Gx);
        fvWekaAttributes.add(Wrist_Gy);
        fvWekaAttributes.add(Wrist_Gz);
        fvWekaAttributes.add(Wrist_Mx);
        fvWekaAttributes.add(Wrist_My);
        fvWekaAttributes.add(Wrist_Mz);
        fvWekaAttributes.add(Activity);


    }
    public void classifyInstance(){
        Log.d(TAG, "Classifying instance...");
        int prediction = 0;
        try {

            double[] attrValues = new double[12];
            attrValues[0] = Ax;
            attrValues[1] = Ay;
            attrValues[2] = Az;
            attrValues[3] = Lx;
            attrValues[4] = Ly;
            attrValues[5] = Lz;
            attrValues[6] = Gx;
            attrValues[7] = Gy;
            attrValues[8] = Gz;
            attrValues[9] = Mx;
            attrValues[10] = My;
            attrValues[11] = Mz;

            //Fill in the training set with one instance
            instance = new DenseInstance(1, attrValues);
            instance.setDataset(instances);


            // Get the prediction in int
            prediction = (int)cls.classifyInstance(instance);

            // increase the count in the hashmap after classifying as this <activity>
            // might have nullpointer whoops
            int count = readings.containsKey(prediction) ? readings.get(prediction) : 0;
            readings.put(prediction, count +1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


};
