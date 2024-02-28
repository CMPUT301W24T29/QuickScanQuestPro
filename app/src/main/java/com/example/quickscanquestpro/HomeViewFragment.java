package com.example.quickscanquestpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeViewFragment extends Fragment {

    private QRCodeScanner qrCodeScanner;

    // Invokes the user to allow runtime permission for Camera Access
    private ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted)
                {
                    setupCamera();
                }
                else
                {
                    Toast.makeText(getContext(), "Camera permission denied, QR Scanner cannot be used", Toast.LENGTH_LONG).show();
                }
            });

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeView.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeViewFragment newInstance(String param1, String param2) {
        HomeViewFragment fragment = new HomeViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the app already has run time permission for camera it will start setupCamera otherwise invoke requestCameraPermissionLauncher
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            setupCamera();
        }
        else{
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

    }

    private void setupCamera()
    {
        View view = getView();
        if (view != null) {
            PreviewView previewView = view.findViewById(R.id.cameraFeed);
            qrCodeScanner = new QRCodeScanner(getContext(), previewView);
            qrCodeScanner.startCamera();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        qrCodeScanner.shutdown();
    }
}