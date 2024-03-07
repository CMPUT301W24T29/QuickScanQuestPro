package com.example.quickscanquestpro;
import android.content.Context;
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
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.navigation.NavigationBarView;
import com.google.common.util.concurrent.ListenableFuture;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * *This is main class which defines QRCodeScanner Behaviors
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
                                    if (!processingQr) {
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
        for (Barcode barcode : barcodes) {
            String rawValue = barcode.getRawValue();
            Log.d("QRCodeScanner", "Barcode detected: " + rawValue);

            if(rawValue.startsWith("c") || rawValue.startsWith("p")) {
                // Extracting the prefix and ID from the QR code
                processingQrType = rawValue.substring(0, 1); // "c" for check-in, "p" for promo
                String eventId = rawValue.substring(1);

                // request the event from the database service / check if this event exists in the database
                databaseService.getEvent(eventId, this);
                // this returns now because when the data is retrieved or fails to retrieve the data, it will call onEventLoaded
                // to continue the processing
                return;
            } else {
                Log.e("QRCodeScanner", "Unknown QR Code format: " + rawValue);
                Toast.makeText(mainActivity.getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
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
            // the database didnt find a match
            Toast.makeText(mainActivity.getApplicationContext(), "Invalid QR", Toast.LENGTH_SHORT).show();
            processingQr = false;
        } else {
            // the database found a match, so continue
            // transition to the test event's details page
            if (processingQrType.equals("c")){
                Toast.makeText(mainActivity.getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity.getApplicationContext(), "Promotion code scanned!", Toast.LENGTH_SHORT).show();
            }

            // navigates to the details for the event
            mainActivity.transitionFragment(new EventDetailsFragment(event), "EventDetailsFragment");
            NavigationBarView navBarView = mainActivity.findViewById(R.id.bottom_navigation);
            // sets navbar selection to the event dashboard
            MenuItem item = navBarView.getMenu().findItem(R.id.navigation_dashboard);
            item.setChecked(true);

            shutdown();
        }
    }
}