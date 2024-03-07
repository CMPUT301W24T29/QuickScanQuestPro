package com.example.quickscanquestpro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {


    private ImageView profilePicturePlaceholder;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private DatabaseService databaseService = new DatabaseService();

    private User user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(User user) {
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the permission request launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize the image picker launcher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                profilePicturePlaceholder.setImageURI(selectedImage);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profilePicturePlaceholder = view.findViewById(R.id.profilePicturePlaceholder);
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);

        uploadProfilePictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        });


        //For updating profile information
        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        SwitchMaterial geolocationSwitch = view.findViewById(R.id.geolocationSwitch);

        if(user == null)
        {
            MainActivity mainActivity = (MainActivity) getActivity();
            user = mainActivity.getUser();
        }
        //Get User from Main activity

        //Update information
        fullNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                user.setName(s.toString());
                databaseService.addUser(user);
            }
        });

        homepageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                user.setHomepage(s.toString());
                databaseService.addUser(user);
            }
        });

        mobileNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                user.setMobileNum(s.toString());
                databaseService.addUser(user);
            }
        });

        emailAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                user.setEmail(s.toString());
                databaseService.addUser(user);
            }
        });

        geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user.setGeolocation(isChecked);
            databaseService.addUser(user);
        });

        //Prepopulate EditText
        fetchAndPopulateUserData(user);

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void fetchAndPopulateUserData(User user) {
        // Assuming you have a way to get the current user's ID
        String userId = user.getUserId()/* Retrieve the user ID, possibly from SharedPreferences or passed through arguments */;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Now, you directly access fields
                    String name = document.getString("name");
                    String homepage = document.getString("homepage");
                    String mobileNum = document.getString("mobileNum");
                    String email = document.getString("email");
                    Boolean geolocation = document.getBoolean("geolocation");

                    // Assuming this runs on the UI thread, but consider checking and/or using runOnUiThread if needed
                    updateUIWithUserData(name, homepage, mobileNum, email, geolocation);
                } else {
                    Log.d("ProfileFragment", "No such document");
                }
            } else {
                Log.d("ProfileFragment", "get failed with ", task.getException());
            }
        });
    }

    private void updateUIWithUserData(String name, String homepage, String mobileNum, String email, Boolean geolocation) {
        View view = getView();
        if (view == null) return; // Ensure view is available

        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        SwitchMaterial geolocationSwitch = view.findViewById(R.id.geolocationSwitch);

        fullNameInput.setText(name);
        homepageInput.setText(homepage);
        mobileNumberInput.setText(mobileNum);
        emailAddressInput.setText(email);
        if (geolocation != null) {
            geolocationSwitch.setChecked(geolocation);
        }
    }


}