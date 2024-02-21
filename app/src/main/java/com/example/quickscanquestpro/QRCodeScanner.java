package com.example.quickscanquestpro;
import android.content.Context;
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
 * *This is main class which defines QRCodeScanner Behaviors
 */
public class QRCodeScanner {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private Context context;
    private BarcodeScanner scanner;

    /**
     * Constructor for QRCodeScanner
     * @param context The application context used for accessing the camera.
     * @param previewView The view into which the camera preview is rendered
     */
    public QRCodeScanner(Context context, PreviewView previewView)
    {
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        this.scanner = BarcodeScanning.getClient();
    }

    /**
     * This method sets ups the camera a PreviewView and an ImageAnalysis
     * use case to process frames in real-time and perform barcode scanning
     */
    @OptIn(markerClass = ExperimentalGetImage.class) public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

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
                    try
                    {
                        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                        scanner.process(inputImage)
                                .addOnSuccessListener(barcodes -> processBarcodes(barcodes))
                                .addOnFailureListener(e -> {})
                                .addOnCompleteListener(task -> image.close());
                    }
                    catch (Exception e)
                    {
                        image.close();
                    }
                });

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
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
        for (Barcode barcode: barcodes) {
            String rawValue = barcode.getRawValue();
            String yourEventId = "yourEventId";

            DocumentReference attendeeRef = db.collection("events")
                    .document(yourEventId)
                    .collection("attendees")
                    .document(rawValue);
            Map<String, Object> checkedInData = new HashMap<>();
            checkedInData.put("checkedIn", true);

            attendeeRef.set(checkedInData, SetOptions.merge())
                    .addOnCompleteListener(aVoid -> Log.d("QRCodeScanner", "Attendee checked-in successfully"))
                    .addOnFailureListener(e -> Log.e("QRCodeScanner", "Error checking-in attendee",e));

        }
    }

    /**
     * Shuts down the executor service used for running image analysis to release resources
     */
    public void shutdown() {
        cameraExecutor.shutdown();
    }

}