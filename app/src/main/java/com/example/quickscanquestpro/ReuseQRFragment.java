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

/**
 * A {@link Fragment} subclass that provides QR code scanning functionality. This fragment uses the {@link QRCodeScanner}
 * utility class to access the device's camera, display the camera feed, and scan for QR codes.
 * It also includes a back button that allows the user to return to the previous screen.
 */
public class ReuseQRFragment extends Fragment {

    private QRCodeScanner qrCodeScanner;

    /**
     * Inflates the fragment's layout.
     *
     * @param inflater LayoutInflater for inflating views in the fragment.
     * @param container Parent view the fragment UI should be attached to.
     * @param savedInstanceState Previously saved state of the fragment.
     * @return The View for the fragment's UI.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reuse_qr, container, false);
    }


    /**
     * Sets up QR code scanner and back button functionality.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, contains a previous saved state of the fragment.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreviewView previewView = view.findViewById(R.id.cameraFeed);
        qrCodeScanner = new QRCodeScanner(getContext(), previewView, this, (MainActivity) this.getActivity());
        qrCodeScanner.startCamera(); // Start camera and prepare for scanning


        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // Check if there are entries in the back stack
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                // If there are, pop the back stack to go to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });
    }

    /**
     * Called when the view hierarchy associated with the fragment is being removed. This method is used to
     * shut down the QR code scanner's camera feed to release resources.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        qrCodeScanner.shutdown();
    }
}
