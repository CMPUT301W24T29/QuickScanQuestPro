
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import java.util.UUID;

/**
 * This fragment displays the details of an event, including the title, description, date, location,
 * announcements, and the event banner. It also allows the event organizer to upload a new event
 * banner and share the event QR code.
 */
public class EventDetailsFragment extends Fragment {

    private Event event;
    private DatabaseService databaseService = new DatabaseService();
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView eventImage;

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
                    Glide.with(this).load(selectedImageUri).into(eventImage);
                    uploadImage(selectedImageUri);
                }
                else
                {

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
        MainActivity mainActivity = (MainActivity) this.getActivity();
        if(mainActivity != null && mainActivity.getUser() != null) {
            // Initialize the views that will display the event details
            TextView eventTitle = view.findViewById(R.id.event_title);
            TextView eventDescription = view.findViewById(R.id.event_description);
            TextView eventDate = view.findViewById(R.id.event_date);
            TextView eventLocation = view.findViewById(R.id.event_location);
            eventImage = view.findViewById(R.id.event_banner);
            FloatingActionButton backButton = view.findViewById(R.id.back_button);
            FloatingActionButton shareButton = view.findViewById(R.id.share_event_button);
            Button uploadImageButton = view.findViewById(R.id.edit_banner_button);

            uploadImageButton.setVisibility(View.VISIBLE);


            // If there is no event passed in, create a test event
            if (this.event == null) {
                event = Event.createTestEvent(mainActivity.getNewEventID());
            }

            // Set the image of the event to the event banner if it exists, otherwise hide the imageview
            if (event.getEventBannerUrl() != null) {
                Glide.with(this).load(event.getEventBannerUrl()).into(eventImage);
            } else {
                eventImage.setVisibility(View.GONE);
            }

            // Set the text of the event details to the event details
            eventTitle.setText(event.getTitle());
            eventDescription.setText(event.getDescription());
            String eventDateString = event.getStartDate().toString() + " at " + event.getStartTime().toString() + " until " + event.getEndDate().toString() + " at " + event.getEndTime().toString();
            eventDate.setText(eventDateString);
            eventLocation.setText(event.getLocation());
            ArrayList<String> announcementList = event.getAnnouncements();

            // Set the listview of announcements to the announcements of the event and set the height of the listview
            ArrayAdapter<String> announcementAdapter =
                    new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);
            ListView announcementListView = view.findViewById(R.id.event_announcements_list);
            announcementListView.setAdapter(announcementAdapter);
            ListViewHelper.getListViewSize(announcementListView);

            // Set an on click listener for the back button
            backButton.setOnClickListener(v -> {

                // if the user is organiser, i want to go back to admin event dashboard
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();

            });

            // Enable these buttons if the user is the organizer of the event
            if (event.getOrganizerId().equals(mainActivity.getUser().getUserId())) {
                uploadImageButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    pickImageLauncher.launch(intent);
                });
                // For now, option to change event banner is unavailable
                // eventImage.setOnClickListener(event.uploadPhoto(this, eventImage));
                setShareButton(shareButton);
            }
            // Hide these buttons if user is not the organizer
            else {
                uploadImageButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);
            }

            // For now, the option to change the event banner is unavailable
            // eventImage.setOnClickListener(event.uploadPhoto(this, eventImage));
            //uploadImageButton.setOnClickListener(event.uploadPhoto(this, eventImage));
            setShareButton(shareButton);
        }
        else {
            Log.e(TAG, "User or MainActivity is null");

        }
    }

    @Override
    public void onDestroyView() {
        // Check if event is not null before attempting to access its methods
        if (event != null && event.getEventBanner() != null) {
            uploadImage(getImageToShare(event.getEventBanner()));
        }
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
        databaseService.uploadEventPhoto(file, event, new DatabaseService.OnEventPhotoUpload() {
            @Override
            public void onSuccess(String imageUrl, String imagePath) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Glide.with(EventDetailsFragment.this)
                                .load(imageUrl)
                                .into(eventImage);

                        eventImage.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Event Banner Uploaded", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.d(TAG, "Fragment is not attached to an activity.");
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }
}
