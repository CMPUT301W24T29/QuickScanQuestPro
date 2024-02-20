package com.example.quickscanquestpro;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
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
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * *This is main class which defines QRCodeScanner Behaviors
 */
public class QRCodeScanner {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;


    public QRCodeScanner(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView)
    {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        startCamera();
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreviewAndImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("QRCodeScanner", "Camera starting error",e);
            }
        }, ContextCompat.getMainExecutor(context));
    }


    @SuppressLint("UnsafeOptInUsageError")
    private void bindPreviewAndImageAnalysis(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), imageProxy -> {
            InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            BarcodeScanning.getClient().process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            DocumentReference attendeeRef = db.collection("events")
                                    .document("yourEventId")
                                    .collection("attendees")
                                    .document(rawValue);

                            Map<String, Object> checkInData = new HashMap<>();
                            checkInData.put("checkedIn", true);

                            attendeeRef.set(checkInData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d("QRCodeScanner", "Attendee checked-in successfully"))
                                    .addOnFailureListener(e -> Log.e("QRCode Scanner", "Error checking-in attendee", e));
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("QRCodeScanner", "QR Code scanning failed", e);
                        imageProxy.close();
                    });

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis);
        });


    }
}
