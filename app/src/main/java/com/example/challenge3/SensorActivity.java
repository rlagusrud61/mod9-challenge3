package com.example.challenge3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    float Fc = 50;
    float RC = (float) (1/(2*Math.PI*Fc));

    // Front-End components
    TextView introText;
    ImageButton activities, history;

    //History
    boolean inHistory = false;
    boolean atHomeScreen = true;

    String current_state;

    private final String[] activity = {"walking", "standing", "jogging", "sitting","biking","upstairs","downstairs"};

    MediaPlayer biking, upstairs, downstairs, jogging, sitting, standing, walking;

    // Accelerometer, Gyroscope, Linear_acceleration, Magnetometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor linear_acceleration;
    private Sensor magnetometer;
    float Ax,Ay,Az,Lx,Ly,Lz,Mx,My,Mz,Gx,Gy,Gz;
    ArrayList<Attribute> fvWekaAttributes;
    boolean aFirst = true;
    boolean gFirst = true;
    boolean lFirst = true;
    boolean mFirst = true;

    public static int NUMBER_OF_ATTRIBUTES = 13;
    public static int NUMBER_OF_ATTRIBUTES_WITHOUT_CLASS = 12;
    // 20,000 microseconds = 50Hz
    private final int dt = 20000;
    private final int NUMBER_OF_READINGS = 150;

    boolean accUpdated = false;
    boolean gyroUpdated = false;
    boolean linearaccUpdated = false;
    boolean magnetoUpdated = false;
    boolean running = true;

    Instances instances;
    Instance instance;

    HashMap<Integer,Integer> readings;
    Classifier cls;

    public float lowPass(float x,  float lastY, float RC, int dt, boolean first){ //simple digital RC filter
        float Dt = (float) dt/1000000;
        float alpha = Dt/(RC+Dt);
        //Log.d("filter", "alpha: " + alpha);
        float y;

        if(first){ // on first cycle there wont be a y-1 so multiply x with alpha
            return alpha*x;
        }
        else {
            y = alpha * x + (1 - alpha) * lastY; //filter equation
            return y;
        }
    }

    // Recycler View Initialisation
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    List<Model> activityList;
    Adapter adapter;


    @Override
    public final void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize front-end and set button listeners
        introText = findViewById(R.id.introText);
        activities = findViewById(R.id.activities);
        history = findViewById(R.id.history);
        activities.setOnClickListener(this);
        history.setOnClickListener(this);

        biking = MediaPlayer.create(this,R.raw.biking);
        upstairs = MediaPlayer.create(this,R.raw.going_upstairs);
        downstairs = MediaPlayer.create(this,R.raw.going_downstairs);
        jogging = MediaPlayer.create(this,R.raw.jogging);
        sitting = MediaPlayer.create(this,R.raw.sitting);
        standing = MediaPlayer.create(this,R.raw.standing);
        walking = MediaPlayer.create(this,R.raw.walking);

        // Recycler View
        initData();
        initRecyclerView();


        // Check the user has the right permissions enabled
//        checkPermissions((Activity) this);

        // Start reading values from the sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linear_acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    private void initRecyclerView() {
        Log.d(TAG,"initRecyclerView");
        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Adapter(activityList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        Log.d(TAG,"initData");
        activityList = new ArrayList<>();
        activityList.add(new Model(R.drawable.biking, "biking", "17:00 pm", "_______________________________________"));
        activityList.add(new Model(R.drawable.biking, "biking", "17:00 pm", "_______________________________________"));
        activityList.add(new Model(R.drawable.biking, "biking", "17:00 pm", "_______________________________________"));
        activityList.add(new Model(R.drawable.biking, "biking", "17:00 pm", "_______________________________________"));

    }

    public void initiateReadings(){
        if (running) {
            readings = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                readings.put(i, 0);
            }
        }
    }

    @Override
    public void onClick(View view) {

        // When this button gets clicked, you will move into the history tab
       if (view.equals(activities) && atHomeScreen){
           introText.setText("You are");
           atHomeScreen = false;
           createTrainingSet();
           initiateReadings();
           running = true;

           instances = new Instances("Bruh", fvWekaAttributes, 5);
           instances.setClassIndex(NUMBER_OF_ATTRIBUTES-1);
           try {
               cls = (Classifier) weka.core.SerializationHelper.read(getAssets().open("randomForestRightPocket.model"));
           } catch (Exception e) {
               e.printStackTrace();
           }

           sensorManager.registerListener((SensorEventListener) SensorActivity.this, accelerometer, dt);
           sensorManager.registerListener((SensorEventListener) SensorActivity.this, gyroscope, dt);
           sensorManager.registerListener((SensorEventListener) SensorActivity.this, linear_acceleration, dt);
           sensorManager.registerListener((SensorEventListener) SensorActivity.this, magnetometer, dt);

       }

       if (view.equals(history)){

           if (inHistory){
               inHistory = false;
               history.setImageResource(R.drawable.history);
               introText.setText("Press the image to start");
               activities.setImageResource(R.drawable.play);
               activities.setVisibility(View.VISIBLE);
               introText.setVisibility(View.VISIBLE);
               recyclerView.setVisibility(View.GONE);
               atHomeScreen = true;

           } else {
               running = false;
               inHistory = true;
               activities.setVisibility(View.GONE);
               introText.setVisibility(View.GONE);
               history.setImageResource(R.drawable.home_button);
               recyclerView.setVisibility(View.VISIBLE);
           }
       }
    }

    public void ChangePictureAndSound(){

        switch(current_state)
        {
            case "walking":
                activities.setImageResource(R.drawable.walking);
                walking.start();
                break;
            case "standing":
                activities.setImageResource(R.drawable.standing);
                standing.start();
                break;
            case "jogging":
                activities.setImageResource(R.drawable.jogging);
                jogging.start();
                break;
            case "sitting":
                activities.setImageResource(R.drawable.sitting);
                sitting.start();
                break;
            case "biking":
                activities.setImageResource(R.drawable.biking);
                biking.start();
                break;
            case "upstairs":
                activities.setImageResource(R.drawable.upstairs);
                upstairs.start();
                break;
            case "downstairs":
                activities.setImageResource(R.drawable.downstairs);
                downstairs.start();
                break;
            default:
                System.out.println("No activity has been recorded");
        }

    }


    @Override
    public final void onSensorChanged(SensorEvent event) {

        if (running) {
            // Getting the accelerometer values
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (aFirst) {
                    Ax = lowPass(event.values[0], 0, RC, dt, true);
                    Ay = lowPass(event.values[1], 0, RC, dt, true);
                    Az = lowPass(event.values[2], 0, RC, dt, true);
                    aFirst = false;

                } else {
                    float x = event.values[0];
                    Ax = lowPass(event.values[0], Ax, RC, dt, false);
                    Ay = lowPass(event.values[1], Ay, RC, dt, false);
                    Az = lowPass(event.values[2], Az, RC, dt, false);
                    //Log.d("filter", "x: "+ x + " y: " + Ax);
                }
                accUpdated = true;

            }

            // Getting the gyroscope values
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if (gFirst) {
                    Gx = lowPass(event.values[0], 0, RC, dt, true);
                    Gy = lowPass(event.values[1], 0, RC, dt, true);
                    Gz = lowPass(event.values[2], 0, RC, dt, true);
                    gFirst = false;
                } else {
                    Gx = lowPass(event.values[0], Gx, RC, dt, false);
                    Gy = lowPass(event.values[1], Gy, RC, dt, false);
                    Gz = lowPass(event.values[2], Gz, RC, dt, false);
                }
                gyroUpdated = true;
            }

            // Getting the linear acceleration values
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                if (lFirst) {
                    Lx = lowPass(event.values[0], 0, RC, dt, true);
                    Ly = lowPass(event.values[1], 0, RC, dt, true);
                    Lz = lowPass(event.values[2], 0, RC, dt, true);
                    lFirst = false;
                } else {
                    Lx = lowPass(event.values[0], Lx, RC, dt, false);
                    Ly = lowPass(event.values[1], Ly, RC, dt, false);
                    Lz = lowPass(event.values[2], Lz, RC, dt, false);
                }
                linearaccUpdated = true;
            }

            // Getting the magnetometer values
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                if (mFirst) {
                    Mx = lowPass(event.values[0], 0, RC, dt, true);
                    My = lowPass(event.values[1], 0, RC, dt, true);
                    Mz = lowPass(event.values[2], 0, RC, dt, true);
                    mFirst = false;
                    //Log.d("filter", "first time m: " + Mz + " original: " + event.values[2]);
                } else {
                    Mx = lowPass(event.values[0], Mx, RC, dt, false);
                    My = lowPass(event.values[1], My, RC, dt, false);
                    Mz = lowPass(event.values[2], Mz, RC, dt, false);
                    //Log.d("filter", "m: " + Mz + " original: " + event.values[2]);
                }
                magnetoUpdated = true;
            }

            if (accUpdated && magnetoUpdated && linearaccUpdated && gyroUpdated) {
                classifyInstance();
                accUpdated = false;
                magnetoUpdated = false;
                linearaccUpdated = false;
                gyroUpdated = false;
            }
            getPredictedActivity();
        }
}


    public int getActivityWithMostOccurrence(){
        // Get the activity with the most occurrence
        int maxPredictionValue = 0;
        int maxPredictionKey = 0;
        for (Map.Entry<Integer, Integer> entry : readings.entrySet()){
            if (maxPredictionValue < entry.getValue())
            {
                Log.d(TAG, "Entry key  : " + entry.getKey() + " value :" + entry.getValue());
                Log.d(TAG, "New maxPrediction = " + maxPredictionValue);
                maxPredictionValue = entry.getValue();
                maxPredictionKey = entry.getKey();
            }
        }return maxPredictionKey;
    }

    public void getPredictedActivity(){
        int sum = 0;
        for (int v: readings.values()){
            sum += v;
        }

        //if it reaches 150 readings
        if (sum == NUMBER_OF_READINGS){
            Log.d(TAG, readings.toString());
            int prediction = getActivityWithMostOccurrence();
            if (current_state != activity[prediction]) {
                current_state = activity[prediction];
                ChangePictureAndSound();
                introText.setText("You are most likely " + current_state);
                Log.d(TAG, "You are most likely " + current_state);
            }
            readings.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void createTrainingSet(){

        if (running) {

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

            ArrayList<String> acList = new ArrayList<>();
            for (int i = 0; i < activity.length; i++) {
                acList.add(activity[i]);
            }
            Attribute Activity = new Attribute("Activity", acList);

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
    }
    public void classifyInstance(){
        int prediction = 0;
        try {

            double[] attrValues = new double[NUMBER_OF_ATTRIBUTES-1];
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
            readings.put(prediction, count + 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

};
