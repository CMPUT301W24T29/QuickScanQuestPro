package com.example.quickscanquestpro;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EventDetailsFragment extends Fragment {

    private Event event;
    private TextView eventTitle;
    private TextView eventDescription;
    private TextView eventDate;
    private TextView eventLocation;
    private ArrayAdapter<String> announcementAdapter;
    private ListView announcementListView;
    private ImageView eventImage;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailsFragment newInstance(String param1, String param2) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public EventDetailsFragment() {
        // Required empty public constructor
    }
    public EventDetailsFragment(Event event) {
        this.event = event;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        eventTitle = view.findViewById(R.id.event_title);
        eventDescription = view.findViewById(R.id.event_description);
        eventDate = view.findViewById(R.id.event_date);
        eventLocation = view.findViewById(R.id.event_location);
        ArrayList<String> announcementList = new ArrayList<String>();
        announcementAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);
        announcementListView = view.findViewById(R.id.event_announcements_list);
        eventImage = view.findViewById(R.id.event_banner);
        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        FloatingActionButton shareButton = view.findViewById(R.id.share_event_button);
        Button uploadImageButton = view.findViewById(R.id.edit_banner_button);


        // If there is no event passed in, use the test event
        if (this.event == null) {
            MainActivity mainActivity = (MainActivity) this.getActivity();
            event = Event.createTestEvent(mainActivity.getNewEventID());
        }

        if (event.getEventBanner() != null) {
            eventImage.setImageBitmap(event.getEventBanner());
            uploadImageButton.setVisibility(View.GONE);
        }
        else {
            eventImage.setVisibility(View.GONE);
        }

        // Set the text of the event details to the event details
        eventTitle.setText(event.getTitle());
        eventDescription.setText(event.getDescription());
        String eventDateString = event.getStartDate() + " at " + event.getStartTime() + " until " + event.getEndDate() + " at " + event.getEndTime();
        eventDate.setText(eventDateString);
        eventLocation.setText(event.getLocation());
        announcementList = event.getAnnouncements();

        // Set the listview of announcements to the announcements of the event and set the height of the listview
        announcementAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);
        announcementListView = view.findViewById(R.id.event_announcements_list);
        announcementListView.setAdapter(announcementAdapter);
        ListViewHelper.getListViewSize(announcementListView);

        // Set an on click listener for the back button
        backButton.setOnClickListener(v -> {
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.event_title));
            fragmentTransaction.commit();
        });

        shareButton.setOnClickListener(v -> {

            final Dialog shareQrDialog = new Dialog(this.getContext());
            shareQrDialog.setContentView(R.layout.dialog_share);
            shareQrDialog.show();

            Button sharePromoQR = shareQrDialog.findViewById(R.id.share_promo_button);
            sharePromoQR.setOnClickListener(v1 -> {
                shareQrDialog.dismiss();
                Bitmap promoCodeImage = event.getPromoQRImage();
                shareQRImage(promoCodeImage);
            });

            Button shareCheckInQR = shareQrDialog.findViewById(R.id.share_checkIn_button);
            shareCheckInQR.setOnClickListener(v1 -> {
                shareQrDialog.dismiss();
                Bitmap checkInCodeImage = event.getCheckinQRImage();
                shareQRImage(checkInCodeImage);
            });

            ImageButton closeDialog = shareQrDialog.findViewById(R.id.share_close_button);
            closeDialog.setOnClickListener(v1 -> shareQrDialog.dismiss());
        });

        // Gets the uri of the image to upload to event banner, and changes it into a bitmap to be
        // set as the event banner. Then sets the banner imageview to the new event banner.
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                Bitmap newBitmap = null;
                ContentResolver contentResolver = this.getActivity().getContentResolver();
                try {
                    if(Build.VERSION.SDK_INT < 28) {
                        newBitmap = MediaStore.Images.Media.getBitmap(contentResolver, result);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, result);
                        newBitmap = ImageDecoder.decodeBitmap(source);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                event.setEventBanner(newBitmap);
                eventImage.setImageBitmap(newBitmap);
            }
        });
        // Sets an on click listener for the upload image button

        uploadImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
        // Set an on click listener for the event image so image can still be uploaded after it has been set
        eventImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
    }

    // Method to share the QR code Uri of the event to other apps
    private void shareQRImage(Bitmap imageQR) {

        Uri uriQR = getImageToShare(imageQR);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriQR);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this event!");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code");
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));

    }
   // Method to get the Uri version of the QR code image to be shared
    private Uri getImageToShare(Bitmap imageQR) {

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
}