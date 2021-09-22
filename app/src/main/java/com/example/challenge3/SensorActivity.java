package com.example.challenge3;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.rules.DecisionTable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


public class SensorActivity extends FragmentActivity {

    // sending log output TAG
    private static final String TAG = "MyActivity";

    // Front-End components
    TextView introText1, info_text;
    ImageButton startButton, again;
    LinearLayout linearLayout;

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
            Classifier classifier = (Classifier) weka.core.SerializationHelper.read("BayesNet.model");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


};
