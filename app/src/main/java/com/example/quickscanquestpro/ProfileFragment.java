package com.example.quickscanquestpro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.OnProgressListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This fragment displays user profile details.
 * The user can update details of themselves
 * The user can upload profile picture
 * The user can delete profile picture
 */
public class ProfileFragment extends Fragment implements GeolocationService.GeolocationRegisteredFragment {
    private ImageView profilePicturePlaceholder;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String[]> locPermLauncher;
    private ActivityResultLauncher<IntentSenderRequest> locResolutionIntentSender;
    private ImageView deleteProfilePictureButton;

    LinearProgressIndicator progressIndicator;
    private DatabaseService databaseService = new DatabaseService();
    private Switch notificationSwitch;
    private User user;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o) {
                notificationSwitch.setChecked(true);
                user.setGetNotification(true);
                Toast.makeText(getContext(), "Notifications Permission granted", Toast.LENGTH_LONG).show();
            }
            else {
                notificationSwitch.setChecked(false);
                user.setGetNotification(false);
                Toast.makeText(getContext(), "Notifications Permission denied", Toast.LENGTH_LONG).show();
            }
            databaseService.addUser(user);
        }
    });
    private GeolocationService geolocationService = new GeolocationService(this, this);
    private boolean ignoreGeolocSwitch = false;
    private SwitchMaterial geolocationSwitch;

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

        // launcher to deal with permission results
        locPermLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), geolocationService::locationPermissionResultHandler);

        // launcher to deal with user's not having location enabled, but having geolocation permissions granted and toggled on in profile
        locResolutionIntentSender = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), geolocationService::locationEnabledResolutionHandler);

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        NavigationBarView navBarView = mainActivity.findViewById(R.id.bottom_navigation);
        // Sets navbar selection to the profile dashboard
        MenuItem item = navBarView.getMenu().findItem(R.id.navigation_profile);
        item.setChecked(true);

        // when user clicks the back button
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });


        notificationSwitch = view.findViewById(R.id.alert_switch);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    // check if user has accepted the permission, if not turn the switch off
                }
                else
                {
                    user.setGetNotification(true);
                    databaseService.addUser(user);
                }
            }
            else
                {
                    user.setGetNotification(false);
                    databaseService.addUser(user);
                }
        });
    }


    /**
     * Initializes views within the fragment and sets up listeners for user interactions.
     * This method is responsible for binding UI components to their respective views in the layout,
     * setting click listeners for buttons, adding text change listeners for EditText fields,
     * and initializing switch interactions. It also prepopulates user data into the UI components.
     * @param view The parent view of the fragment in which the UI components are located.
     */
    private void initializeViews(View view) {
        profilePicturePlaceholder = view.findViewById(R.id.profilePicturePlaceholder);
        deleteProfilePictureButton = view.findViewById(R.id.deleteProfilePictureButton);
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);


        uploadProfilePictureButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });




        //For updating profile information
        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        geolocationSwitch = view.findViewById(R.id.geolocationSwitch);


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
                if (user != null) {
                    if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
                        displayInitials();
                    }
                }

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
            if (isChecked && !ignoreGeolocSwitch) {
                // disable it before attempting to get location, because this could take a while
                geolocationSwitch.setEnabled(false);
                geolocationService.getLocation();
            } else if (!ignoreGeolocSwitch){
                user.setGeolocation(false);
                databaseService.addUser(user);
            }
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
        databaseService.deleteProfilePicture(user, new DatabaseService.OnProfilePictureDelete() {
            @Override
            public void onSuccess() {
                profilePicturePlaceholder.setImageResource(R.drawable.ic_profile_picture_placeholder);
                deleteProfilePictureButton.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Profile Picture Deleted", Toast.LENGTH_SHORT).show();
                displayInitials();
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
        String userId = user.getUserId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Now, you directly access fields

                    String name = document.getString("name");
                    String homepage = document.getString("Homepage");
                    String mobileNum = document.getString("phone");
                    String email = document.getString("email");
                    String profilePictureUrl = document.getString("profilePictureUrl");
                    Boolean geolocation = document.getBoolean("geolocation");
                    Boolean NotificationPermission = document.getBoolean("ReceiveNotifications");

                    updateUIWithUserData(name, homepage, mobileNum, email, geolocation, profilePictureUrl, NotificationPermission);
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
    private void updateUIWithUserData(String name, String homepage, String mobileNum, String email, Boolean geolocation, String profilePictureUrl, Boolean getNotification) {
        View view = getView();
        if (view == null) return; // Ensure view is available

        EditText fullNameInput = view.findViewById(R.id.fullNameInput);
        EditText homepageInput = view.findViewById(R.id.homepageInput);
        EditText mobileNumberInput = view.findViewById(R.id.mobileNumberInput);
        EditText emailAddressInput = view.findViewById(R.id.emailAddressInput);
        SwitchMaterial geolocationSwitch = view.findViewById(R.id.geolocationSwitch);
        Switch notificationSwitch = view.findViewById(R.id.alert_switch);

        fullNameInput.setText(name);
        homepageInput.setText(homepage);
        mobileNumberInput.setText(mobileNum);
        emailAddressInput.setText(email);

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(this).load(profilePictureUrl).into(profilePicturePlaceholder);
            deleteProfilePictureButton.setVisibility(View.VISIBLE);
        } else {
            // Use the centralized method to display initials
            displayInitials();
            deleteProfilePictureButton.setVisibility(View.GONE);
        }
        if (geolocation != null) {
            ignoreGeolocSwitch = true;
            geolocationSwitch.setChecked(geolocation);
            ignoreGeolocSwitch = false;
        }
        // set the notification switch to on or off based on the user's preference
        if(getNotification != null)
        {
            notificationSwitch.setChecked(getNotification);
        }
    }

    /**
     * Generates a bitmap image containing the user's initials. This method creates a bitmap of specified width and height,
     * fills it with a background color, and then draws the user's initials in the center. The text and background colors,
     * as well as the text size, can be adjusted within the method.
     *
     * @param initials The initials to be drawn on the bitmap. This should be a string containing the first letters of the user's name.
     * @param width The desired width of the resulting bitmap in pixels.
     * @param height The desired height of the resulting bitmap in pixels.
     * @return A Bitmap object containing the user's initials. This bitmap can be used directly to set an ImageView or saved as an image file.
     */
    private Bitmap generateInitialsBitmap(String initials, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // Text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(100); // Adjust as needed
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Adjust text size and position as needed
        float xPos = width / 2;
        float yPos = (height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2);

        canvas.drawText(initials, xPos, yPos, textPaint);

        return bitmap;
    }

    /**
     * Displays the user's initials in the profile picture placeholder. If the user's name is available,
     * this method generates a bitmap with the user's initials and sets it as the image for the profile picture placeholder.
     * If the user's name is not available, it sets the placeholder image to a default resource.
     * This method ensures that there is always an appropriate image displayed in the profile picture placeholder,
     * whether it be the user's initials or a default image when no name is available.
     */
    private void displayInitials() {
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            Bitmap initialsBitmap = generateInitialsBitmap(user.getInitials(), 300, 300);
            profilePicturePlaceholder.setImageBitmap(initialsBitmap);
            // Setting a tag or content description for testing purposes
            profilePicturePlaceholder.setContentDescription("InitialsDisplayed");
        } else {
            profilePicturePlaceholder.setImageResource(R.drawable.ic_profile_picture_placeholder);
            profilePicturePlaceholder.setContentDescription("DefaultPlaceholder");
        }
    }

    /**
     * function that is called when the result for a users location (enabling location) is done
     * @param success true if the location was set, false if it failed
     * @param result the latitude and longitude as a "lat,long" string, or error string if it failed
     */
    @Override
    public void geolocationRequestComplete(boolean success, String result) {
        if (success) {
            user.setGeolocation(true);
            databaseService.addUser(user);
            geolocationSwitch.setEnabled(true);
            Toast.makeText(getContext(), "Location enabled!", Toast.LENGTH_SHORT).show();
        } else {
            geolocationSwitch.setChecked(false);
            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
            geolocationSwitch.setEnabled(true);
        }
    }

    /**
     * Returns the registered handler for permission activity results
     * @return result launcher handler for using .launch()
     */
    public ActivityResultLauncher<String[]> getLocPermLauncher() {
        return locPermLauncher;
    }

    /**
     * Returns the registered handler for settings activity results
     * @return result launcher handler for using .launch()
     */
    public ActivityResultLauncher<IntentSenderRequest> getLocResolutionIntentSender() {
        return locResolutionIntentSender;
    }
}
