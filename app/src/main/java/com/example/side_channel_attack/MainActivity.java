package com.example.side_channel_attack;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

//        CameraStateDetector cm = new CameraStateDetector(this);

//

//        db.collection("entries").add(data);


//
        final File cameraDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final File dir = new File(cameraDirectory.getAbsolutePath() + "/Camera");
        final File image = new File(dir.getPath() + "/" + "IMG_20190410_031536.jpg");

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        final String id = "" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits

        final DocumentReference existRef = db.collection("users").document(id);

        final Map<String, Object> entry = new HashMap<>();


        Map<String, Object> data = new HashMap<>();
        data.put("text", "7799305738247_1555181788303.jpg");
        data.put("image", "7799305738247_1555181788303.jpg");
        final FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", "https://firebasestorage.googleapis.com/v0/b/data-reciever.appspot.com/o/7799305738247_1555181788303.jpg?alt=media&token=4958936b-d9ca-4295-a518-610b8129ac55");
        parameters.put("id", id);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        parameters.put("timestamp", dateFormat.format(calendar.getTime()));
        parameters.put("tag", "car");
        System.out.println("jk");

        HttpsCallableReference searchSingle = mFunctions
                .getHttpsCallable("searchSingle");
        searchSingle.call(parameters);

        mFunctions
                .getHttpsCallable("filter")
                .call(data).addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {

                HashMap<String, ArrayList<String>> data = (HashMap<String, ArrayList<String>>) httpsCallableResult.getData();

                for (String term : data.get("webMatches")) {


                }

                for (String term : data.get("labelMatches")) {

                }

                for (String term : data.get("objectMatches")) {

                }

                for (String term : data.get("locationMatches")) {

                }

                for (String term : data.get("logoMatches")) {

                }

            }
        });


//        existRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                System.out.println("jh");
//                if (!task.getResult().exists()) {
//                    System.out.println("jht");
//
//                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//                    final String pathString = id + "_" + System.currentTimeMillis() + ".jpg";
//                    final StorageReference ref = storageRef.child(pathString);
//                    InputStream stream = null;
//
//                    try {
//                        stream = new FileInputStream(image);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                    final InputStream finalStream = stream;
//
//                    ref.putStream(finalStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//
//                        }
//                    });
//                }
//            }
//        });
//
//        BitmapFactory.Options bitMapOption = new BitmapFactory.Options();
//        bitMapOption.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(image.getPath(), bitMapOption);
//        int imageWidth = bitMapOption.outWidth;
//        int imageHeight = bitMapOption.outHeight;
//        System.out.println(imageWidth+" asd");
//        System.out.println(imageHeight+" bndasd");
//        System.out.println(Build.BRAND+ Build.DEVICE);
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
////        processImage(image,bitmap);
//
//        ClarifaiClient client = new ClarifaiBuilder("0fc188c1de3d404eaae4b8d12494bc7e")
//                .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
//                .buildSync();
//
//        client.getDefaultModels().generalModel().predict()
//                .withInputs(ClarifaiInput.forImage(image))
//                .executeAsync(new ClarifaiRequest.Callback<List<ClarifaiOutput<Concept>>>() {
//                    @Override
//                    public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
//                        System.out.println(clarifaiOutputs.size());
//                        List<Concept> data = clarifaiOutputs.get(0).data();
//                        for (Concept concept: data){
//                            System.out.println(concept.name() +" "+ concept.value());
//                        }
//                        System.out.println(data.size());
//                }
//
//                    @Override
//                    public void onClarifaiResponseUnsuccessful(int errorCode) {
//                        System.out.println(errorCode);
//
//                    }
//
//                    @Override
//                    public void onClarifaiResponseNetworkError(IOException e) {
//                        System.out.println("error1");
//                        e.printStackTrace();
//
//                    }
//                });


//        observer = new FileObserver(dir.getPath()) {
//
//            @Override
//            public void onEvent(int event, String file) {
//                if (event == FileObserver.CREATE && !file.equals(".probe")) { // check if its a "create" and not equal to .probe because thats created every time camera is launched
//
//
//
//                    }
//                System.out.println("it worked ");
//            }
//        };
//        observer.startWatching();


//
//        requestUsageStatsPermission();
//       hasUsageStatsPermission(this);
//
//        AppChecker appChecker = new AppChecker();
//        appChecker.whenAny(new AppChecker.Listener() {
//            @Override
//            public void onForeground(String process) {
//                System.out.println(process);
//            }
//        }).timeout(1000).start(this);

    }

    class IntCounter {
        int value = 0;

        void increment() {
            value++;
        }

        void set(int val) {
            value = val;
        }
    }

    private void processImage(final File image, Bitmap bitmap) {
//        final FirebaseVisionImage bitmapImage = FirebaseVisionImage.fromBitmap(bitmap);
//        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
//                .getOnDeviceImageLabeler();
//        labeler.processImage(bitmapImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
//            @Override
//            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
//                final IntCounter counter = new IntCounter();
//                final ArrayList confidentWords = new ArrayList();
//
//                if (isLandmark(firebaseVisionImageLabels)) {
//                    FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
//                            .getVisionCloudLandmarkDetector();
//
//                    Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(bitmapImage)
//                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
//                                @Override
//                                public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
//
//                                    for (FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks) {
//                                        //send request
//                                        counter.increment();
//                                        confidentWords.add(landmark.getLandmark());
//                                    }
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    System.out.println(e.getMessage());
//                                }
//                            });
//
//                }
//
//                if (isHuman(firebaseVisionImageLabels)) {
//
//                }
//
//                if (isLogo(firebaseVisionImageLabels)) {
//
//
//                }
//
//
//                for (FirebaseVisionImageLabel label : firebaseVisionImageLabels) {
//                    String text = label.getText();
//                    String entityId = label.getEntityId();
//                    float confidence = label.getConfidence();
//                    System.out.println("hb " + text);
//                    System.out.println("hb " + entityId);
//                    System.out.println("hb " + confidence);
//                }
//            }
//        });


//
//        try (final ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
//        int size = (int) image.length();
//
//
//            AsyncTask.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//
//                        ImageAnnotatorSettings ias = ImageAnnotatorSettings.newBuilder()
//                                .setCredentialsProvider(
//                                        FixedCredentialsProvider.create()
//                                )
//                                .build();
//
//
//                        vision.setVisionRequestInitializer(
//                                new VisionRequestInitializer("YOUR_API_KEY"));
//
//                        FileInputStream in = new FileInputStream(image);
//                        byte[] data = IoUtils.toByteArray(in);
//                        in.close();
//                        ByteString imgBytes = ByteString.copyFrom(data);
//                        Image img = Image.newBuilder().setContent(imgBytes).build();
//                        inputImage.encodeContent(photoData);
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            });
//        ByteString imgBytes = ByteString.copyFrom(data);
//
//        // Builds the image annotation request
//        List<AnnotateImageRequest> requests = new ArrayList<>();
//        Image img = Image.newBuilder().setContent(imgBytes).build();
//        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
//        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
//                .addFeatures(feat)
//                .setImage(img)
//                .build();
//        requests.add(request);
//
//        // Performs label detection on the image file
//        BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
//        List<AnnotateImageResponse> responses = response.getResponsesList();
//
//        for (AnnotateImageResponse res : responses) {
//            if (res.hasError()) {
//                System.out.printf("Error: %s\n", res.getError().getMessage());
//                return;
//            }
//
//            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
//                annotation.getAllFields().forEach(new BiConsumer<Descriptors.FieldDescriptor, Object>() {
//                    @Override
//                    public void accept(Descriptors.FieldDescriptor k, Object v) {
//                        System.out.printf("%s : %s\n", k, v.toString());
//                    }
//                });
//            }
//        }
//    } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    private boolean isLandmark(List<FirebaseVisionImageLabel> labels) {
//        for (FirebaseVisionImageLabel label : labels) {
//            if (label.getText().toLowerCase().equals("landmark") && label.getConfidence() > 0.1){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isLogo(List<FirebaseVisionImageLabel> labels) {
//        for (FirebaseVisionImageLabel label : labels) {
//            if (label.getText().toLowerCase().equals("logo") && label.getConfidence() > 0.1
//                    || label.getText().toLowerCase().equals("text") && label.getConfidence() > 0.1){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isHuman(List<FirebaseVisionImageLabel> labels) {
//        for (FirebaseVisionImageLabel label : labels) {
//            if (label.getText().toLowerCase().equals("human") && label.getConfidence() > 0.5
//                    || label.getText().toLowerCase().equals("face") && label.getConfidence() > 0.5){
//                return true;
//            }
//        }
//        return false;
//    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    void requestUsageStatsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }


}
