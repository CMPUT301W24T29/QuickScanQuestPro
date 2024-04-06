package com.example.quickscanquestpro;
import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * This class defines the behaviour of the QRCodeScanner and lets attendees check in by scanning QR code seamlessly
 * Tells Events about new check in
 * The attendees can also go to events details page without checking, if a promotional code is scanned
 *
 */
public class QRCodeScanner implements DatabaseService.OnEventDataLoaded{
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
    public QRCodeScanner(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner, MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.scanner = BarcodeScanning.getClient();
        this.lifecycleOwner = lifecycleOwner;

    }

    public QRCodeScanner(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner, MainActivity mainActivity, OnQRScanned callback)
    {
        this.mainActivity = mainActivity;
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.scanner = BarcodeScanning.getClient();
        this.lifecycleOwner = lifecycleOwner;
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

            if (rawValue.startsWith("c") || rawValue.startsWith("p")) {
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
                databaseService.recordCheckIn(event.getId(), currentUser.getUserId(), "The location where QR is scanned");
                databaseService.updateLastCheckIn(currentUser.getUserId(), event.getId());

                Toast.makeText(mainActivity.getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
                event.checkIn();
            } else {
                Toast.makeText(mainActivity.getApplicationContext(), "Promotion code scanned!", Toast.LENGTH_SHORT).show();
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

}

