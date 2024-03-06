package com.example.quickscanquestpro;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.example.quickscanquestpro.QRCodeScanner;
import com.example.quickscanquestpro.R;

public class ReuseQRFragment extends Fragment {

    private QRCodeScanner qrCodeScanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reuse_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreviewView previewView = view.findViewById(R.id.cameraFeed);
        qrCodeScanner = new QRCodeScanner(getContext(), previewView, this, (MainActivity) this.getActivity());
        qrCodeScanner.startCamera(); // Start camera and prepare for scanning


        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            // Check if there are entries in the back stack
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                // If there are, pop the back stack to go to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        qrCodeScanner.shutdown();
    }
}
