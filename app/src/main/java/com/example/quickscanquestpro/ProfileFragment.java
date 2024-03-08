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
        initializeViews(view);

    }


    /**
     * Initializes views within the fragment and sets up listeners for user interactions.
     * This method is responsible for binding UI components to their respective views in the layout,
     * setting click listeners for buttons, adding text change listeners for EditText fields,
     * and initializing switch interactions. It also prepopulates user data into the UI components.
     *
     * @param view The parent view of the fragment in which the UI components are located.
     */
    private void initializeViews(View view) {
        profilePicturePlaceholder = view.findViewById(R.id.profilePicturePlaceholder);
        deleteProfilePictureButton = view.findViewById(R.id.deleteProfilePictureButton);
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);


        uploadProfilePictureButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });


        //For updating profile information
        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        SwitchMaterial geolocationSwitch = view.findViewById(R.id.geolocationSwitch);

        //Get User from Main activity

        if(user==null)
        {
            MainActivity mainActivity = (MainActivity) getActivity();
            user = mainActivity.getUser();
        }

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

        deleteProfilePictureButton.setOnClickListener(v -> deleteProfilePicture());

    }

    /**
     * Uploads the selected image to Firebase Storage and updates user profile picture information.
     * @param file The URI of the selected image to be uploaded.
     * This method uploads the image to a "images/" directory in Firebase Storage with a unique UUID.
     * It displays upload progress, updates the profile picture URL and path in the User object and DatabaseService upon successful upload,
     * and makes the delete profile picture button visible. In case of failure, it displays a toast message.
     */
    private void uploadImage(Uri file) {
        MainActivity mainActivity = (MainActivity) getActivity();
        User user = mainActivity.getUser();

        progressIndicator.setVisibility(View.VISIBLE);
        progressIndicator.setIndeterminate(true);

        databaseService.uploadProfilePicture(file, user, new DatabaseService.OnProfilePictureUpload() {
            @Override
            public void onSuccess(String imageUrl, String imagePath) {
                Glide.with(ProfileFragment.this).load(imageUrl).into(profilePicturePlaceholder);
                deleteProfilePictureButton.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT).show();
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed To Upload Profile Picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onProgress(double progress) {
                // Update the UI with the progress
                progressIndicator.setProgress((int) progress);
            }
        });
    }


    /**
     * Deletes the current profile picture from Firebase Storage and updates Firestore.
     * This method checks if the current user has a profile picture set. If yes, it deletes the picture from Firebase Storage through DatabaseService
     * sets the profile picture URL and path in the User object to null, updates Firestore through Database Service,
     * resets the UI to show the default profile picture, and hides the delete profile picture button.
     * It shows a toast message indicating success or failure.
     */
    public void deleteProfilePicture() {
        MainActivity mainActivity = (MainActivity) getActivity();
        User user = mainActivity.getUser();

        databaseService.deleteProfilePicture(user, new DatabaseService.OnProfilePictureDelete() {
            @Override
            public void onSuccess() {
                profilePicturePlaceholder.setImageResource(R.drawable.ic_profile_picture_placeholder);
                deleteProfilePictureButton.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Profile Picture Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Fetches the current user's data from the Firestore database and populates the UI with the fetched data.
     * This method retrieves user information such as name, homepage, mobile number, email, profile picture URL,
     * and geolocation preference from the database using the user's unique ID. Once the data is fetched successfully,
     * it updates the UI elements accordingly.
     *
     * Note: This method assumes the presence of a valid {@link User} object and that the Firestore database is
     * properly initialized and accessible. It also assumes that the user data is stored under a collection named "users".
     *
     * @param user The {@link User} object representing the current user. It should contain the user's unique ID.
     */
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
                    String mobileNum = document.getString("phone");
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


    /**
     * Updates the UI with the user's data fetched from the database.
     * This method is responsible for setting the text of EditText fields for the user's name, homepage,
     * mobile number, and email address. It also sets the state of a Switch for geolocation preference and
     * loads the user's profile picture using Glide if a URL is provided. If no profile picture URL is provided,
     * or it is empty, the method hides the delete profile picture button.
     *
     * Note: This method assumes it is called on the UI thread. If called from a background thread, you should use
     * {@code getActivity().runOnUiThread(Runnable)} to ensure UI operations are performed safely.
     *
     * @param name The user's name to be set in the UI.
     * @param homepage The URL of the user's homepage to be set in the UI.
     * @param mobileNum The user's mobile number to be set in the UI.
     * @param email The user's email address to be set in the UI.
     * @param geolocation The user's geolocation preference to be updated in the UI.
     * @param profilePictureUrl The URL of the user's profile picture. If provided, it is loaded into the profile picture view.
     */
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