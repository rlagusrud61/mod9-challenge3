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

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import weka.core.Attribute;


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
    float Ax,Ay,Az,Lx,Ly,Lz,Mx,My,Mz,Gx,Gy,Gz;
    ArrayList<Attribute> fvWekaAttributes = new ArrayList<>();
    Instances trainingSet;

    public static int NUMBER_OF_ATTRIBUTES = 13;
    public static int NUMBER_OF_ATTRIBUTES_WITHOUT_CLASS = 12;
    // 20,000 microseconds = 50Hz
    private final int dt = 20000;

    //List with magnitudes of acceleration
    ArrayList<Double> accel_mag = new ArrayList<Double>();
    Instances instances;


    // Cvs file
//   public ArrayList<Attribute> attributes;
//    private final String[] attrList = {"Wrist_Ax", "Wrist_Ay", "Wrist_Az", "Wrist_Lx", "Wrist_Ly", "Wrist_Lz", "Wrist_Gx", "Wrist_Gy", "Wrist_Gz", "Wrist_Mx", "Wrist_My", "Wrist_Mz", "Activity"};
    private final String[] activities = {"walking", "standing", "jogging", "sitting","biking","upstairs","downstairs"};
//    Instances liveData = null;
//
//
//    private Instances createInstances(String name, String[] attList, String[] activityList, int capacity){
//        attributes = new ArrayList<>();
//        ArrayList<String> activityTemp = new ArrayList<String>();
//        for(int i=0; i<attList.length-1;i++){
//            attributes.add(new Attribute(attList[i]));
//        }
//        for(int i=0; i < activityList.length; i++){
//            activityTemp.add(activityList[i]);
//        }
//        attributes.add(new Attribute(attList[attList.length-1], activityTemp));
//
//        return new Instances(name, attributes, capacity);
//    }

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
        run();
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

            Ax = event.values[0];
            Ay = event.values[1];
            Az = event.values[2];
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

    }

    public void run(){
        for (int i = 0 ; i < 5 ; i ++ ){
            classifyInstance();
        }
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

    public void createTrainingSet(){

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

        trainingSet = new Instances("Rel", fvWekaAttributes, 50);
        trainingSet.setClassIndex(12);

    }
    public double classifyInstance(){
        int prediction = 0;
        try {

            //Fill in the training set with one instance
            Instance instance = new DenseInstance(NUMBER_OF_ATTRIBUTES);
            // Do we need to add the timestamp?
            instance.setValue(fvWekaAttributes.get(0), Ax);
            instance.setValue(fvWekaAttributes.get(1), Ay);
            instance.setValue(fvWekaAttributes.get(2), Az);
            instance.setValue(fvWekaAttributes.get(3), Lx);
            instance.setValue(fvWekaAttributes.get(4), Ly);
            instance.setValue(fvWekaAttributes.get(5), Lz);
            instance.setValue(fvWekaAttributes.get(6), Gx);
            instance.setValue(fvWekaAttributes.get(7), Gy);
            instance.setValue(fvWekaAttributes.get(8), Gz);
            instance.setValue(fvWekaAttributes.get(9), Mx);
            instance.setValue(fvWekaAttributes.get(10), My);
            instance.setValue(fvWekaAttributes.get(11), Mz);

            trainingSet.add(instance);
            Classifier cls = (Classifier) weka.core.SerializationHelper.read(getAssets().open("RandomTree.model"));
            prediction = (int)cls.classifyInstance(trainingSet.instance(0));
            Log.d(TAG, "Prediction : " + prediction);
            Log.d(TAG, "istrainingset:" + trainingSet);
            introText1.setText(activities[prediction]);

        } catch (Exception e) {

        }
        return prediction;
    }


};
