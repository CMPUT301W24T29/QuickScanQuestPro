package com.example.quickscanquestpro;
import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
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


/**
 * This class defines the behaviour of the QRCodeScanner and lets attendees check in by scanning QR code seamlessly
 * Tells Events about new check in
 * The attendees can also go to events details page without checking, if a promotional code is scanned
 *
 */
public class QRCodeScanner {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private Context context;
    private BarcodeScanner scanner;

    private LifecycleOwner lifecycleOwner;

    /**
     * Constructor for QRCodeScanner
     * @param context The application context used for accessing the camera.
     * @param previewView The view into which the camera preview is rendered
     */
    public QRCodeScanner(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner)
    {
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
                                    processBarcodes(barcodes);
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
     * Processes scanned barcodes to perform check-ins
     * For each barcode, a Firestore document is updated to record the check in
     * @param barcodes The list of barcodes detected in the frame.
     */
    private void processBarcodes(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            String rawValue = barcode.getRawValue();
            Log.d("QRCodeScanner", "Barcode detected: " + rawValue);

            if ((rawValue.startsWith("c") || rawValue.startsWith("p")) && rawValue.length() > 1) {
                try {
                    // Check if following characters are integer
                    Integer.parseInt(rawValue.substring(1));
                    // If parsing is successful, it means we have an integer following 'c' or 'p'
                    Log.d("QRCodeScanner", "Valid QR Code detected: " + rawValue);
                    // Implement the popup dialog with the success message here
                    showSuccessPopup();


                    break;
                } catch (NumberFormatException e) {
                    Log.e("QRCodeScanner", "QR Code does not follow the required format.", e);
                }
            } else {
                Log.e("QRCodeScanner", "Unknown QR Code format: " + rawValue);
            }
        }
    }

    /**
     * If the QRCode is Scanned Properly a success message is showed
     */
    private void showSuccessPopup() {
        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(previewView.getContext());
        builder.setTitle("Success");
        builder.setMessage("Hurray! Successfully checked into Startcona Fest");

        // Add a button and its onClickListener
        builder.setPositiveButton("Go to event details page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code to go to the event details page

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    /**
     * Shuts down the executor service used for running image analysis to release resources
     */
    public void shutdown() {
        cameraExecutor.shutdown();
    }

}