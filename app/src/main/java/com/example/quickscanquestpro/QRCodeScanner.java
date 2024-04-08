package com.example.quickscanquestpro;
import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.navigation.NavigationBarView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * This class defines the behaviour of the QRCodeScanner and lets attendees check in by scanning QR code seamlessly
 * Tells Events about new check in
 * The attendees can also go to events details page without checking, if a promotional code is scanned
 *
 */
public class QRCodeScanner implements DatabaseService.OnEventDataLoaded {
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private Context context;
    private BarcodeScanner scanner;
    private LifecycleOwner lifecycleOwner;
    private MainActivity mainActivity;
    private Boolean processingQr = false;
    private DatabaseService databaseService = new DatabaseService();
    private String processingQrType;
    private OnQRScanned callback;
    private String customCode;
    private GeolocationService geolocationService;
    private Event locationGettingEvent;

    private ArrayList<Object> uniqueAttendees = new ArrayList<>();
    /**
     * Interface for callbacks when a QRCode is scanned that returns the scanned code instead.
     */
    public interface OnQRScanned {
        void onQRScanned(String scannedCode);
    }

    /**
     * Constructor for QRCodeScanner
     * @param context The application context used for accessing the camera.
     * @param previewView The view into which the camera preview is rendered
     */
    public QRCodeScanner(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner, MainActivity mainActivity, GeolocationService geolocationService)
    {
        this.mainActivity = mainActivity;
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.scanner = BarcodeScanning.getClient();
        this.lifecycleOwner = lifecycleOwner;
        this.geolocationService = geolocationService;

    }

    public QRCodeScanner(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner, MainActivity mainActivity, GeolocationService geolocationService, OnQRScanned callback)
    {
        this.mainActivity = mainActivity;
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.scanner = BarcodeScanning.getClient();
        this.lifecycleOwner = lifecycleOwner;
        this.geolocationService = geolocationService;
        this.callback = callback;

    }

    /**
     * This method sets ups the camera a PreviewView and an ImageAnalysis
     * use case to process frames in real-time and perform barcode scanning
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.getContext());

        cameraProviderFuture.addListener(() ->{;
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    try {
                        Log.d("QRCodeScanner", "ImageAnalysis analyzer is called");

                        if (image.getImage() == null) {
                            Log.e("QRCodeScanner", "Received image is null, skipping barcode scanning");
                            return;
                        }

                        Log.d("QRCodeScanner", "Processing image for barcode scanning");
                        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

                        // Before calling scanner.process
                        Log.d("QRCodeScanner", "Calling scanner.process(inputImage)");

                        scanner.process(inputImage)
                                .addOnSuccessListener(barcodes -> {
                                    Log.d("QRCodeScanner", "Barcodes detected: " + barcodes.size());
                                    if (!processingQr && barcodes.size() > 0) {
                                        processingQr = true;
                                        processBarcodes(barcodes);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("QRCodeScanner", "Error detecting QR Code", e))
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("QRCodeScanner", "Barcode scanning task completed successfully");
                                    } else {
                                        Log.e("QRCodeScanner", "Barcode scanning task failed", task.getException());
                                    }
                                    image.close(); // Ensure to close the image
                                });

                        // After scanner.process is called
                        Log.d("QRCodeScanner", "scanner.process(inputImage) has been called");

                    } catch (Exception e) {
                        Log.e("QRCodeScanner", "Failed to process image", e);
                        image.close(); // Ensure to close the image in case of failure
                    }
                });


                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis);
                Log.d("QRCodeScanner", "Camera started and bound to lifecycle");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Processes scanned barcodes to perform check-ins and promo codes
     * @param barcodes The list of barcodes detected in the frame.
     */
    private void processBarcodes(List<Barcode> barcodes) {
        Barcode barcode = barcodes.get(0);
        if (barcode != null) {
            String rawValue = barcode.getRawValue();
            Log.d("QRCodeScanner", "Barcode detected: " + rawValue);

            if (callback != null) {
                // this was called with the intent of returning the scanned value and not actually processing it (for reusing a qr code)
                callback.onQRScanned(rawValue);
                shutdown();
                return;
            }

            // check if it is the admin access qr code
            if (rawValue.equals("QuickScanQuestProADMIN")){
                // make sure user is not null before giving admin access
                if(mainActivity.getUser()!=null){
                    // set admin to true in the database
                    databaseService.enableAdmin(mainActivity.getUser().getUserId());
                    mainActivity.getUser().setAdmin(true);
                    Toast.makeText(mainActivity.getApplicationContext(), "Congratulations, you are now an Admin!!", Toast.LENGTH_SHORT).show();
                    // transition to AdminProfileFragment
                    mainActivity.transitionFragment(new ProfileFragment(), "AdminProfileFragment");
                } else {
                    Toast.makeText(mainActivity.getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                    Log.e("QRCodeScanner", "User not logged in");
                }
            } else if (rawValue.startsWith("c") || rawValue.startsWith("p")) {
                // Extracting the prefix and ID from the QR code
                processingQrType = rawValue.substring(0, 1); // "c" for check-in, "p" for promo
                String eventId = rawValue.substring(1);

                // request the event from the database service / check if this event exists in the database
                databaseService.getEvent(eventId, this);
                // this returns now because when the data is retrieved or fails to retrieve the data, it will call onEventLoaded
                // to continue the processing
                return;
            } else {
                // lets look for a custom qr code
                Log.e("QRCodeScanner", "QR Code may be custom: " + rawValue);
                customCode = rawValue;
                databaseService.getEventWithCustomQR(rawValue, this);
                // this returns now because when the data is retrieved or fails to retrieve the data, it will call onEventLoaded
                // to continue the processing
                return;
            }
        }
        // if there is an error, then this will wait 4 seconds before allowing processing of a QR code again to stop toasts from stacking
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                processingQr = false;
            }
        }, 4000);
    }

    /**
     * Shuts down the executor service used for running image analysis to release resources
     */
    public void shutdown() {
        cameraExecutor.shutdown();
    }

    /**
     * This runs when the processed QRcode returns from the database and either checks in the user or shows them the details page
     * Depending on if the event code was a promo or checkin code
     * @param event Event returned from DatabaseService, can be null if not found.
     */
    @Override
    public void onEventLoaded(Event event) {
        if (event == null) {
            Toast.makeText(mainActivity.getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
            // if there is an error, then this will wait 4 seconds before allowing processing of a QR code again to stop toasts from stacking
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    processingQr = false;
                }
            }, 4000);
        } else {
            // Check if user is null before attempting to use getUserId()
            User currentUser = mainActivity.getUser();
            if (currentUser == null) {
                Toast.makeText(mainActivity.getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                Log.e("QRCodeScanner", "User not logged in");
                // if there is an error, then this will wait 4 seconds before allowing processing of a QR code again to stop toasts from stacking
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        processingQr = false;
                    }
                }, 4000);
                return;
            }
            if (processingQrType == null){
                // we were checking a custom qr code, so lets find out if it was a promo or a checkin code
                if (Objects.equals(event.getCustomCheckin(), customCode)) {
                    processingQrType = "c";
                } else {
                    processingQrType = "p";
                }
            }

            if (processingQrType.equals("c")){
                if (currentUser.isGeolocation()) {
                    locationGettingEvent = event;
                    // will eventually call geolocationRequestComplete() with result
                    geolocationService.getLocation();
                    return;
                } else {
                    databaseService.recordCheckIn(event.getId(), currentUser.getUserId(), "");
                    databaseService.updateLastCheckIn(currentUser.getUserId(), event.getId());
                    event.checkIn();
                    Toast.makeText(mainActivity.getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mainActivity.getApplicationContext(), "Promotion code scanned!", Toast.LENGTH_SHORT).show();
            }

            databaseService.getEvent(event.getId(), new DatabaseService.OnEventDataLoaded() {
                @Override
                public void onEventLoaded(Event event) {
                    if (event != null) {
                        // get all unique attendees
                        for(CheckIn info : event.getCheckIns())
                        {
                            if(!uniqueAttendees.contains(info.getUserId()))
                            {
                                uniqueAttendees.add(info.getUserId());
                            }
                        }
                        Log.d("CheckIn", "Unique attendees: " + uniqueAttendees.size());

                        if(event.getCheckIns().size() == 1) {
                            if (uniqueAttendees.size() == 1) {
                                sendAlert(event.getOrganizerId(), "Congratulations, one attendee has checked in");
                            }
                        }

                        // transition to the new event
                        mainActivity.transitionFragment(new EventDetailsFragment(event), "EventDetailsFragment");
                        NavigationBarView navBarView = mainActivity.findViewById(R.id.bottom_navigation);
                        // Sets navbar selection to the event dashboard
                        MenuItem item = navBarView.getMenu().findItem(R.id.navigation_dashboard);
                        item.setChecked(true);

                        shutdown();
                    }
                }
            });
        }
    }


    /**
     * called by homeview fragment with the result of the location request
     * @param success true if retrieved, false if not
     * @param result the location as a "lat,long" string or the error message if success false
     */
    public void geolocationRequestComplete(boolean success, String result) {
        User currentUser = mainActivity.getUser();

        // all done with location request yayy
        if (success) {
            Toast.makeText(mainActivity.getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity.getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            result = "";
            Toast.makeText(mainActivity.getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
        }

        databaseService.recordCheckIn(locationGettingEvent.getId(), currentUser.getUserId(), result);
        databaseService.updateLastCheckIn(currentUser.getUserId(), locationGettingEvent.getId());
        locationGettingEvent.checkIn();

        // transition to the new event
        mainActivity.transitionFragment(new EventDetailsFragment(locationGettingEvent), "EventDetailsFragment");
        NavigationBarView navBarView = mainActivity.findViewById(R.id.bottom_navigation);
        // Sets navbar selection to the event dashboard
        MenuItem item = navBarView.getMenu().findItem(R.id.navigation_dashboard);
        item.setChecked(true);

        shutdown();

    }


    public void sendAlert(String userId, String messageBody) {

        // get specific user details form databaseService and use onUserLoaded to send notification
        databaseService.getSpecificUserDetails(userId, new DatabaseService.OnUserDataLoaded() {
            @Override
            public void onUserLoaded(User user) {
                if(user == null)
                {
                    Log.d("Notification", "User not found");
                    return;
                }
                if(user.getGetNotification() == false)
                {
                    // skip this iteration
                    Log.d("Notification", "User: " + user.getName() + " has notifications turned off");
                }
                else{
                    JSONObject jsonObject = new JSONObject();
                    try {
                        Log.d("Notification", "Sending notification to user: " + user.getName());

                        JSONObject notification = new JSONObject();
                        notification.put("title", "Milestone Reached");
                        notification.put("body", messageBody);

                        JSONObject dataObj = new JSONObject();
                        dataObj.put("userID", user.getUserId());

                        jsonObject.put("notification", notification);
                        jsonObject.put("data", dataObj);
                        jsonObject.put("to", user.getNotificationToken());

                        callApi(jsonObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }


    /**
     *  this calls the API to send a request FCM to send a notification to all attendees that have notifications turned on
     * @param jsonObject
     */
    private void callApi(JSONObject jsonObject)
    {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                // add the api key after bearer with a space
                .header("Authorization", "Bearer AAAA-z98YP0:APA91bEoBWfmJI7JHaV87puPVmZhDNv-4m0cxhjYXjsD5mAiPoTuhGbC6xfV0rVBt9qXj59n3TPCRe2QnwlZFXb96DvtoxYvyT5tCNqgaR0m8PapWiWHFVWbNpChm37VzNImEXL5T_iu")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("Notification", "Failed to send notification");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("Notification", "Notification sent successfully");
            }
        });
    }

}

