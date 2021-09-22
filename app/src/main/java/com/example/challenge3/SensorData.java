package com.example.challenge3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

import weka.core.DenseInstance;
import weka.core.Instance;

public class SensorData  implements SensorEventListener {

    float Ax,Ay,Az, Gx, Gy,Gz,Lx,Ly,Lz,Mx,My,Mz;

    //List with magnitudes of acceleration
    ArrayList<Double> accel_mag = new ArrayList<Double>();
    private Sensor accelerometerHandler;
    private Sensor gyroscopeHandler;
    private Sensor linear_accelerationHandler;
    private Sensor magnetometerHandler;

    // 20,000 microseconds == 50Hz
    private final int dt = 20000;

    SensorData(SensorManager sensorManager){
        // Start reading values from the sensors
        accelerometerHandler = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeHandler = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linear_accelerationHandler = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometerHandler = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener((SensorEventListener) SensorData.this, accelerometerHandler, dt);
        sensorManager.registerListener((SensorEventListener) SensorData.this, gyroscopeHandler, dt);
        sensorManager.registerListener((SensorEventListener) SensorData.this, linear_accelerationHandler, dt);
        sensorManager.registerListener((SensorEventListener) SensorData.this, magnetometerHandler, dt);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Getting the accelerometer values
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

             Ax = event.values[0];
             Ay = event.values[1];
             Az = event.values[2];

            // Calculating the magnitude of the acceleration
            accel_mag.add(Math.sqrt(Ax * Ax + Ay * Ay + Az * Az) - 9.81);

            double mag = averageAccelerometer(accel_mag);
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
}
