package com.example.quickscanquestpro;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.MultiFormatWriter;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Used to create a new event. Allows organizers to create events with event details. Optionally select and upload a poster image when creating event.
 * Optionally reuse an existing QRCode and associate it with the new event for either the promotional code or the checkin code. Stores the user that created the event as an organizer
 */
public class EventCreationFragment extends Fragment implements QRCodeScanner.OnQRScanned{
    Event creatingEvent;
    private DatabaseService databaseService = new DatabaseService();
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText locationEditText;
    private TextView startDateText;
    private TextView endDateText;
    private TextView startTimeText;
    private TextView endTimeText;
    private MainActivity mainActivity;
    private Button createButton;
    private ImageView posterImageView;
    private Button reuseCheckinButton;
    private Button reusePromoButton;
    private Integer originalRightPadding;
    private String reuseType;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private EditText signupLimit;

    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Runs when the fragment is created, gets a new event ID for the event that is being created to use. This should be drawn from the database.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.creatingEvent = new Event(UUID.randomUUID().toString());
        setupActivityResultLaunchers();
    }

    private void setupActivityResultLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Glide.with(this).load(selectedImageUri).into(posterImageView);
                    uploadImage(selectedImageUri);
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    /**
     * Runs when the view is displayed, adds on click listeners and validates input for the buttons and entry fields.
     * Stores the event and generates qrcode for the event, or allows user to reuse an old event.
     * Uses event methods and setters/getters, time/date picker fragments and ActivityResultLauncher.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) this.getActivity();

        posterImageView = view.findViewById(R.id.create_event_poster);

        Button uploadImageButton = view.findViewById(R.id.banner_upload_button);

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // adds textwatchers that update the Event whenever text is changed
        titleEditText = view.findViewById(R.id.edit_text_event_title);
        descriptionEditText = view.findViewById(R.id.edit_text_event_description);
        locationEditText = view.findViewById(R.id.edit_text_event_address);
        signupLimit = view.findViewById(R.id.edit_text_signups);

        titleEditText.addTextChangedListener(getTextWatcher(titleEditText));
        descriptionEditText.addTextChangedListener(getTextWatcher(descriptionEditText));
        locationEditText.addTextChangedListener(getTextWatcher(locationEditText));
        signupLimit.addTextChangedListener(getTextWatcher(signupLimit));

        // setting time pickers for start / end times
        startTimeText = view.findViewById(R.id.text_event_start_time);
        startTimeText.setOnClickListener(v -> {
            new TimePickerFragment(startTimeText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "startTimePicker");
        });

        endTimeText = view.findViewById(R.id.text_event_end_time);
        endTimeText.setOnClickListener(v -> {
            new TimePickerFragment(endTimeText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "endTimePicker");
        });

        // setting date pickers for start / end dates
        startDateText = view.findViewById(R.id.text_event_start_date);
        startDateText.setOnClickListener(v -> {
            new DatePickerFragment(startDateText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "startDatePicker");
        });

        endDateText = view.findViewById(R.id.text_event_end_date);
        endDateText.setOnClickListener(v -> {
            new DatePickerFragment(endDateText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "endDatePicker");
        });

        reuseCheckinButton = view.findViewById(R.id.reuse_checkin_button);
        reuseCheckinButton.setOnClickListener(v -> {
            reuseType = "checkin";
            // launch qr scanner by ADDING the fragment (so it does not destroy this one, requiring it to be created again when its display)
            reuseCheckinButton.setFocusableInTouchMode(true);
            reuseCheckinButton.clearFocus();
            reuseCheckinButton.setFocusableInTouchMode(false);
            mainActivity.addFragment(new HomeViewFragment(this), "ReuseCheckin");
//            reuseCheckinButton.setError(null);
        });

        reusePromoButton = view.findViewById(R.id.reuse_promo_button);
        reusePromoButton.setOnClickListener(v -> {
            reuseType = "promo";
            reusePromoButton.setFocusableInTouchMode(true);
            reusePromoButton.clearFocus();
            reusePromoButton.setFocusableInTouchMode(false);
            // launch qr scanner by ADDING the fragment (so it does not destroy this one, requiring it to be created again when its display)
            mainActivity.addFragment(new HomeViewFragment(this), "ReusePromo");
//            reusePromoButton.setError(null);
        });

        // final button that creates event and stores it
        createButton = view.findViewById(R.id.create_event_confirm_button);
        createButton.setOnClickListener(v -> {
            if (validateEntryFields()) {
                mainActivity.setTestEvent(this.creatingEvent);
                String organizerId = mainActivity.getUser().getUserId();
                creatingEvent.setOrganizerId(organizerId);
                // create a new event in the database
                databaseService.addEvent(creatingEvent);
                Log.d("EventCreationFragment", "Event created: " + creatingEvent.toString() );
                // set active fragment to the event dashboard again
                mainActivity.transitionFragment(new EventDashboardFragment(), this.getString(R.string.title_dashboard));
            }
        });

        // must do this at the end, last thing before showing user the fields
        validateEntryFields();
    }

    private TextWatcher getTextWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int editId = editText.getId();
                if (editId == R.id.edit_text_event_title) {
                    creatingEvent.setTitle(editable.toString());
                } else if (editId == R.id.edit_text_event_description) {
                    creatingEvent.setDescription(editable.toString());
                } else if (editId == R.id.edit_text_event_address) {
                    creatingEvent.setLocation(editable.toString());
                } else if (editId == R.id.edit_text_signups) {
                    String signupLimitString = editable.toString();
                    Integer signupLimitValue = null; // Initialize to null for an empty case
                    if (!signupLimitString.isEmpty()) {
                        try {
                            signupLimitValue = Integer.parseInt(signupLimitString);
                        } catch (NumberFormatException e) {
                            // Log the exception or handle the error state as required
                        }
                    }
                    creatingEvent.setSignupLimit(signupLimitValue); // Could be null or a number
                }
                validateEntryFields();
            }
        };
    }

    /**
     * Checks if all the required entry fields (title, description, location, start/end date/time) are valid, and enables/disables the button based on this.
     * @return returns true if all valid, or false if not.
     */
    public Boolean validateEntryFields() {
        Boolean valid = true;

        if (titleEditText.getText().toString().length() <= 0) {
            titleEditText.setError("Must enter a title!");
            valid = false;
        }

        if (descriptionEditText.getText().toString().length() <= 0) {
            descriptionEditText.setError("Must enter a description!");
            valid = false;
        }

        if (locationEditText.getText().toString().length() <= 0) {
            locationEditText.setError("Must enter an address!");
            valid = false;
        }

        // Checking signupLimit only if it's not empty
        String signupLimitString = signupLimit.getText().toString();
        if (!signupLimitString.isEmpty()) {
            try {
                int signupLimitValue = Integer.parseInt(signupLimitString);
                if (signupLimitValue <= 0) {
                    signupLimit.setError("Signup limit must be greater than 0!");
                    valid = false;
                } else {
                    // If it's a valid integer and it's positive then update the event
                    creatingEvent.setSignupLimit(signupLimitValue);
                }
            } catch (NumberFormatException e) {
                signupLimit.setError("Signup limit must be a number!");
                valid = false;
            }
        } else {
            // If the field is empty
            signupLimit.setError(null);
            creatingEvent.setSignupLimit(null);
        }

        if (startDateText.getText().toString().length() <= 0) {
            startDateText.setError("Must enter a start date!");
            valid = false;
        } else {
            startDateText.setError(null);
        }

        if (endDateText.getText().toString().length() <= 0) {
            endDateText.setError("Must enter a end date!");
            valid = false;
        } else {
            endDateText.setError(null);
        }

        if (startTimeText.getText().toString().length() <= 0) {
            startTimeText.setError("Must enter a start time!");
            valid = false;
        } else {
            startTimeText.setError(null);
        }

        if (endTimeText.getText().toString().length() <= 0) {
            endTimeText.setError("Must enter a end time!");
            valid = false;
        } else {
            endTimeText.setError(null);
        }

        if (!valid) {
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(true);
        }

        return valid;
    }

    /**
     * This method uploads an image to the database. It takes a URI of the image file and uploads the image
     * to the database. We pass a null event to the database because we dont actually want it to update the
     * event in the database too, just give us the url for the image so we can update the event when we call
     * the DatabaseService addEvent when the user clicks the create event button.
     * @param file The URI of the image file to be uploaded.
     */
    private void uploadImage(Uri file) {
        // Implementation of uploadImage method, similar to the provided new code
        databaseService.uploadEventPhoto(file, null, new DatabaseService.OnEventPhotoUpload() {
            @Override
            public void onSuccess(String imageUrl, String imagePath) {
                creatingEvent.setEventBannerUrl(imageUrl);
                creatingEvent.setEventBannerPath(imagePath);
                posterImageView.setVisibility(View.VISIBLE);
                Glide.with(EventCreationFragment.this).load(imageUrl).into(posterImageView);
                Toast.makeText(getContext(), "Event Banner Uploaded", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed To Upload Profile Picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a new file in the cache directory and writes the image to it. Returns the URI of the file.
     * @param imageQR The image to be saved.
     * @return The URI of the file.
     */
    public Uri getImageToShare(Bitmap imageQR) {

        File folder = new File(getActivity().getCacheDir(), "images");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "imageQR.jpg");

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            imageQR.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(Objects.requireNonNull(requireActivity().getApplicationContext()), "com.example.quickscanquestpro" + ".provider", file);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    /**
     * This function is called when a QRCode is scanned from a reuse QR button, from the QR scanner, with the code
     * This validates if the scanned code is in use in the database already, and if not sets the event being created
     * to use the custom qr code that was scanned in for its promo or checkin code (depending on which was chosen)
     * @param scannedCode the string that was scanned in, could be any text, to be looked for in the database
     */
    @Override
    public void onQRScanned(String scannedCode) {
        if (scannedCode != null) {
            //do something with scanned code
            Log.d("EventCreationFragment", "Attempting to reuse code " + scannedCode);
            //transition back to the edit screen
            mainActivity.getSupportFragmentManager().popBackStackImmediate();
            // verify the qr code is not already in use in the database
            databaseService.getEventWithCustomQR(scannedCode, event -> {
                Button reuseButton;
                if (Objects.equals(reuseType, "checkin")) {
                    reuseButton = reuseCheckinButton;
                } else {
                    reuseButton = reusePromoButton;
                }
                // if it is in use, send them back to the page with an error icon next to the button
                if (event != null) {
                    if (originalRightPadding == null) {
                        originalRightPadding = reuseButton.getPaddingRight();
                    }
                    // you have to set the success checkmark before setting an error, because the onl way you can change the right drawable is apparently through seterror if you EVER call seterror... forever
                    reuseButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reuse_qr_success_checkmark, 0);
                    reuseButton.setError("The scanned QR code is already used by another event. You must scan an unused QR code.");
                    reuseButton.setPadding(reuseButton.getPaddingLeft(),reuseButton.getPaddingTop(),originalRightPadding-15,reuseButton.getPaddingBottom());
                    reuseButton.setFocusableInTouchMode(true);
                    reuseButton.requestFocus();
                    reuseButton.setFocusableInTouchMode(false);
                } else {
                    // as a final check, make sure they havent already set this code
                    if (Objects.equals(creatingEvent.getCustomCheckin(), scannedCode) || Objects.equals(creatingEvent.getCustomPromo(), scannedCode)) {
                        if (originalRightPadding == null) {
                            originalRightPadding = reuseButton.getPaddingRight();
                        }

                        reuseButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reuse_qr_success_checkmark, 0);
                        reuseButton.setError("The code you scanned has already been set as this event's promo or checkin code. You must scan an unused QR code.");
                        reuseButton.setPadding(reuseButton.getPaddingLeft(),reuseButton.getPaddingTop(),originalRightPadding-15,reuseButton.getPaddingBottom());
                        reuseButton.setFocusableInTouchMode(true);
                        reuseButton.requestFocus();
                        reuseButton.setFocusableInTouchMode(false);
                    } else {
                        // send them back to the page with a check if its not in use at all
                        if (originalRightPadding == null) {
                            originalRightPadding = reuseButton.getPaddingRight();
                        }

                        reuseButton.setError(null);
                        reuseButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.reuse_qr_success_checkmark, 0);
                        reuseButton.setPadding(reuseButton.getPaddingLeft(),reuseButton.getPaddingTop(),originalRightPadding-15,reuseButton.getPaddingBottom());
                        reuseButton.setFocusableInTouchMode(true);
                        reuseButton.clearFocus();
                        reuseButton.setFocusableInTouchMode(false);

                        // sets a custom checkin/promo code and generates a new qrbitmap for respective code
                        if (Objects.equals(reuseType, "checkin")) {
                            creatingEvent.setCustomCheckin(scannedCode);
                        } else {
                            creatingEvent.setCustomPromo(scannedCode);
                        }
                        Toast.makeText(mainActivity.getApplicationContext(), "Set " + reuseType + " QR!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // scanned code was somehow null
            mainActivity.getSupportFragmentManager().popBackStackImmediate();
        }
    }
}