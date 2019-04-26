package com.example.side_channel_attack;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;

//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//
////import com.google.android.gms.vision.Frame;
////import com.google.android.gms.vision.text.TextBlock;
////import com.google.android.gms.vision.text.TextRecognizer;
//import com.google.android.gms.vision.Frame;
//import com.google.android.gms.vision.text.TextBlock;
//import com.google.android.gms.vision.text.TextRecognizer;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.gax.core.FixedCredentialsProvider;
//import com.google.cloud.vision.v1.Image;
//import com.google.cloud.vision.v1.ImageAnnotatorClient;
//import com.google.cloud.vision.v1.ImageAnnotatorSettings;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
//import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
//import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
//import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;


public class MainActivity extends AppCompatActivity {

    private FileObserver observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, DetectorService.class));


    }







}
