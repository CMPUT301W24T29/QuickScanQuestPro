package com.example.quickscanquestpro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.OnProgressListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;


/**
 * This fragment displays user profile details.
 * The user can update details of themselves
 * The user can upload profile picture
 * The user can delete profile picture
 */
public class ProfileFragment extends Fragment {

    private ImageView profilePicturePlaceholder;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Button deleteProfilePictureButton;

    LinearProgressIndicator progressIndicator;



    private StorageReference storageReference;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

        setupActivityResultLaunchers();


    }

    /**
     * Sets up the ActivityResultLauncher for handling image selection result.
     * This method initializes the {@code pickImageLauncher} with the action to take when an image is selected from the device's gallery.
     * Upon successful selection, the selected image URI is loaded into {@code profilePicturePlaceholder} ImageView and uploaded via {@code uploadImage(Uri file)} method.
     */
    private void setupActivityResultLaunchers() {

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Glide.with(this).load(selectedImageUri).into(profilePicturePlaceholder);
                    uploadImage(selectedImageUri);
                }
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
        storageReference = FirebaseStorage.getInstance().getReference();
        initializeViews(view);


    }


    private void initializeViews(View view) {
        profilePicturePlaceholder = view.findViewById(R.id.profilePicturePlaceholder);
        deleteProfilePictureButton = view.findViewById(R.id.deleteProfilePictureButton);
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);


        uploadProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pickImageLauncher.launch(intent);
            }
        });


        //For updating profile information
        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        SwitchMaterial geolocationSwitch = view.findViewById(R.id.geolocationSwitch);

        //Get User from Main activity
        MainActivity mainActivity = (MainActivity) getActivity();
        User user = mainActivity.getUser();


        //Update information
        fullNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                user.setName(s.toString());
                user.saveToFirestore(); // Update Firestore with the new user data
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
                user.saveToFirestore();
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
                user.saveToFirestore();
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
                user.saveToFirestore();
            }
        });

        geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user.setGeolocation(isChecked);
            user.saveToFirestore();
        });

        //Prepopulate EditText
        fetchAndPopulateUserData(user);

        deleteProfilePictureButton.setOnClickListener(v -> deleteProfilePicture());

    }

    /**
     * Uploads the selected image to Firebase Storage and updates user profile picture information.
     * @param file The URI of the selected image to be uploaded.
     * This method uploads the image to a "images/" directory in Firebase Storage with a unique UUID.
     * It displays upload progress, updates the profile picture URL and path in the User object upon successful upload,
     * and makes the delete profile picture button visible. In case of failure, it displays a toast message.
     */
    private void uploadImage(Uri file) {
        String refPath = "images/" + UUID.randomUUID().toString();
        StorageReference ref = storageReference.child(refPath);
        progressIndicator.setVisibility(View.VISIBLE); // Make the progress indicator visible

        ref.putFile(file)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressIndicator.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    MainActivity mainActivity = (MainActivity) getActivity();
                    User user = mainActivity.getUser();
                    if (user != null) {
                        user.setProfilePicturePath(refPath);
                        user.setProfilePictureUrl(imageUrl);
                        user.saveToFirestore();
                    }
                    Glide.with(ProfileFragment.this).load(imageUrl).into(profilePicturePlaceholder);
                    deleteProfilePictureButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT).show();
                    progressIndicator.setVisibility(View.GONE); // Hide the progress indicator
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed To Upload Profile Picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressIndicator.setVisibility(View.GONE); // Hide the progress indicator
                });
    }


    /**
     * Deletes the current profile picture from Firebase Storage and updates Firestore.
     * This method checks if the current user has a profile picture set. If yes, it deletes the picture from Firebase Storage,
     * sets the profile picture URL and path in the User object to null, updates Firestore,
     * resets the UI to show the default profile picture, and hides the delete profile picture button.
     * It shows a toast message indicating success or failure.
     */
    public void deleteProfilePicture() {
        MainActivity mainActivity = (MainActivity) getActivity();
        User user = mainActivity.getUser();
        if (user != null && user.getProfilePicturePath() != null) {
            // Delete from Firebase Storage
            StorageReference picRef = FirebaseStorage.getInstance().getReference().child(user.getProfilePicturePath());
            picRef.delete().addOnSuccessListener(aVoid -> {

                user.setProfilePictureUrl(null);
                user.setProfilePicturePath(null);
                user.saveToFirestore();
                // Reset UI
                profilePicturePlaceholder.setImageResource(R.drawable.ic_profile_picture_placeholder);
                deleteProfilePictureButton.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Profile Picture Deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
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
                    String profilePictureUrl = document.getString("profilePictureUrl");
                    Boolean geolocation = document.getBoolean("geolocation");


                    // Assuming this runs on the UI thread, but consider checking and/or using runOnUiThread if needed
                    updateUIWithUserData(name, homepage, mobileNum, email, geolocation, profilePictureUrl);
                } else {
                    Log.d("ProfileFragment", "No such document");
                }
            } else {
                Log.d("ProfileFragment", "get failed with ", task.getException());
            }
        });
    }

    private void updateUIWithUserData(String name, String homepage, String mobileNum, String email, Boolean geolocation, String profilePictureUrl) {
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

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(this).load(profilePictureUrl).into(profilePicturePlaceholder);
            deleteProfilePictureButton.setVisibility(View.VISIBLE);
        }
        else
        {
            deleteProfilePictureButton.setVisibility(View.GONE);
        }
        if (geolocation != null) {
            geolocationSwitch.setChecked(geolocation);
        }
    }


}