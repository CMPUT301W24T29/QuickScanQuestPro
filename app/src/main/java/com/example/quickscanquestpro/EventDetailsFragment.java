package com.example.quickscanquestpro;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * This fragment displays the details of an event, including the title, description, date, location,
 * announcements, and the event banner. It also allows the event organizer to upload a new event
 * banner and share the event QR code.
 */
public class EventDetailsFragment extends Fragment {

    private Event event;
    private DatabaseService databaseService = new DatabaseService();

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private ImageView eventPosterPlaceHolder;

    /**
     * This is the default constructor for the EventDetailsFragment class. If no event is passed in,
     * a test event is created.
     */
    public EventDetailsFragment() {
    }
    /**
     * This is a constructor for the EventDetailsFragment class. It takes an event as a parameter and
     * sets the event field to the event passed in.
     * @param event The event to be displayed in the fragment
     */
    public EventDetailsFragment(Event event) {
        this.event = event;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
    }

    private void setupActivityResultLaunchers() {

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Glide.with(this).load(selectedImageUri).into(eventPosterPlaceHolder);
                    uploadImage(selectedImageUri);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)},
     * It initializes the views in order to display the event details. It sets the event details.
     * It also sets an click listener for the back button for all users. If the user's ID matches the
     * event organizer's ID, it also sets a click listener for the share button and the upload image button.
     * If the user is not the organizer, the upload image button and the share button are hidden.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventPosterPlaceHolder = view.findViewById(R.id.event_banner); // Correct initialization

        FloatingActionButton backButton = view.findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {

            // if the user is organiser, i want to go back to admin event dashboard
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();

        });

        if(event == null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            event = mainActivity.getEvent();
        }

        if(event != null) {
            fetchAndPopulateEventData(event);
        } else {
            Log.e("EventDetailsFragment", "Event is null");
        }

        Button uploadImageButton = view.findViewById(R.id.edit_banner_button);
        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

    }
    private void initializeViews(View view){
        // Initialize the views that will display the event details
        TextView eventTitle = view.findViewById(R.id.event_title);
        TextView eventDescription = view.findViewById(R.id.event_description);
        TextView eventDate = view.findViewById(R.id.event_date);
        TextView eventLocation = view.findViewById(R.id.event_location);
        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        FloatingActionButton shareButton = view.findViewById(R.id.share_event_button);
        Button uploadImageButton = view.findViewById(R.id.edit_banner_button);
        eventPosterPlaceHolder = view.findViewById(R.id.event_banner);

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        if(event==null)
        {
            MainActivity mainActivity = (MainActivity) getActivity();
            event = mainActivity.getEvent();
        }

        fetchAndPopulateEventData(event);
    }

    private void fetchAndPopulateEventData(Event event){
        String eventId = event.getId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Now, you directly access fields

                    String eventDate = document.getString("Start-date");
                    String eventDescription= document.getString("description");
                    String eventTitle = document.getString("title");
                    String eventBannerUrl = document.getString("eventBannerUrl");
                    String eventLocation = document.getString("location");




                    // Assuming this runs on the UI thread, but consider checking and/or using runOnUiThread if needed
                    updateUIWithEventData(eventDate, eventDescription, eventTitle, eventBannerUrl, eventLocation);
                } else {
                    Log.d("EventDetailsFragment", "No such document");
                }
            } else {
                Log.d("EventDetailsFragment", "get failed with ", task.getException());
            }
        });

    }



    private void updateUIWithEventData(String eventDate,String  eventDescription,String eventTitle,String eventBannerUrl,String eventLocation) {
        View view = getView();
        if (view == null) return;

        TextView setTitleEvent = view.findViewById(R.id.event_title);
        TextView setEventDescription = view.findViewById(R.id.event_description);
        TextView setEventDate = view.findViewById(R.id.event_date);
        TextView setEventLocation = view.findViewById(R.id.event_location);
        ImageView setEventImage = view.findViewById(R.id.event_banner);


        setTitleEvent.setText(eventTitle);
        setEventDescription.setText(eventDescription);
        setEventDate.setText(eventDate);
        setEventLocation.setText(eventLocation);


        if (eventBannerUrl != null && !eventBannerUrl.isEmpty()) {
            Glide.with(this).load(eventBannerUrl).into(setEventImage);
        }

    }






    @Override
    public void onDestroyView() {
//        if (event.getEventBanner() != null) {
//            uploadImage(getImageToShare(event.getEventBanner()));
//        }
        super.onDestroyView();
    }

    /**
     * This method sets the on click listener for the share button. When the share button is clicked,
     * a dialog is displayed with two buttons, one to share the promo QR code and one to share the check in QR code.
     * When one of these buttons is clicked, the dialog is dismissed and the QR code is shared to other apps.
     * @param shareButton The button that opens the dialog to share the QR code when clicked
     */
    private void setShareButton(FloatingActionButton shareButton){
        shareButton.setOnClickListener(v -> {

            final Dialog shareQrDialog = new Dialog(this.getContext());
            shareQrDialog.setContentView(R.layout.dialog_share);
            shareQrDialog.show();

            Button sharePromoQR = shareQrDialog.findViewById(R.id.share_promo_button);
            sharePromoQR.setOnClickListener(v1 -> {
                shareQrDialog.dismiss();
                Bitmap promoCodeImage = event.getPromoQRImage();
                shareQRImage(promoCodeImage, "promo");
            });

            Button shareCheckInQR = shareQrDialog.findViewById(R.id.share_checkIn_button);
            shareCheckInQR.setOnClickListener(v1 -> {
                shareQrDialog.dismiss();
                Bitmap checkInCodeImage = event.getCheckinQRImage();
                shareQRImage(checkInCodeImage, "checkIn");
            });

            ImageButton closeDialog = shareQrDialog.findViewById(R.id.share_close_button);
            closeDialog.setOnClickListener(v1 -> shareQrDialog.dismiss());
        });
    }

    /**
     * This method shares the QR code image to other apps. It creates a URI from the QR code bitmap,
     * by calling getImageToShare, and then creates an intent to share the image. The user can then
     * select an app to share the image with.
     * @param imageQR A bitmap of the QR code image to be shared
     * @param codeType A string symbolizing which type of QR code to be shared, either "promo" or "checkIn"
     */

    private void shareQRImage(Bitmap imageQR, String codeType) {

        Uri uriQR = getImageToShare(imageQR);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriQR);
        if (codeType.equals("promo"))
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Promo QR code for " + event.getTitle());
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check in QR code for " + event.getTitle());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code");
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));

    }
   /**
     * This method creates a URI from a QR code bitmap. It creates a file in the cache directory of the app
     * and writes the bitmap to the file. It then creates a URI from the file and returns the URI.
     * @param imageQR A bitmap of the QR code image to be shared
     * @return A URI of the QR code image to be shared
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
     * This method uploads an image to the database. It takes a URI of the image file and uploads the image
     * to the database.
     * @param file A URI of the image file to be uploaded
     */
    private void uploadImage(Uri file) {
        MainActivity mainActivity = (MainActivity) getActivity();
        Event event = mainActivity.getEvent();
        databaseService.uploadEventPhoto(file, event, new DatabaseService.OnEventPhotoUpload() {
            @Override
            public void onSuccess(String imageUrl, String imagePath) {
                Glide.with(EventDetailsFragment.this).load(imageUrl).into(eventPosterPlaceHolder);
                Log.d(TAG, "onSuccess: " + imageUrl);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

        });
    }
}