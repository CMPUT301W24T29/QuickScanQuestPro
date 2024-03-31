package com.example.quickscanquestpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.widget.Toast;


/**
 * This class starts the home view of the app, initializes the camera to start scanning QR code
 */
public class HomeViewFragment extends Fragment {
    private QRCodeScanner qrCodeScanner;
    private QRCodeScanner.OnQRScanned callback;

    // Invokes the user to allow runtime permission for Camera Access
    private ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted) {
                    setupCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied, QR Scanner cannot be used", Toast.LENGTH_LONG).show();
                }
            });

    public HomeViewFragment() {
        // Required empty public constructor
    }

    public HomeViewFragment(QRCodeScanner.OnQRScanned callback) {
        super();
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.homeViewLayout).setBackgroundColor(getActivity().getColor(R.color.white));

        // Request camera permission first
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

            // Camera permission granted, setup camera
            setupCamera();
        }
        else{
            // Request camera permission
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }


    }


    /**
     * This method initializes the camera for QR Code Scanning
     */
    private void setupCamera()
    {
        View view = getView();
        if (view != null) {
            PreviewView previewView = view.findViewById(R.id.cameraFeed);
            if (callback != null) {
                qrCodeScanner = new QRCodeScanner(getContext(), previewView, this, (MainActivity) this.getActivity(), callback);
            } else {
                qrCodeScanner = new QRCodeScanner(getContext(), previewView, this, (MainActivity) this.getActivity());
            }
            qrCodeScanner.startCamera();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restart camera if permissions are granted
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (qrCodeScanner != null) {
                qrCodeScanner.startCamera();
            } else {
                setupCamera();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Consider stopping or pausing camera here instead of onDestroy
        if (qrCodeScanner != null) {
            qrCodeScanner.shutdown();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (qrCodeScanner != null) {
            qrCodeScanner.shutdown();
        }
    }
}