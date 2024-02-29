package com.example.quickscanquestpro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Time;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

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
    private ArrayList<String> announcementList;
    private ArrayAdapter<String> announcementAdapter;
    private ListView announcementListView;
    private ImageView eventImage;
    public final int galleryReqCode = 1000;


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

    public Event createTestEvent(){
        String testTitle = "Old Strathcona Summer Rib Fest";
        String testDescription = "Come join us for the 2021 Old Strathcona Summer Rib Fest! Enjoy a variety of delicious ribs, live music, and more!";

        Time testStartTime = new Time(11, 0, 0);
        Time testEndTime = new Time(21, 0, 0);
        Date testStartDate = new Date(2021, 7, 16);
        Date testEndDate = new Date(2021, 7, 18);
        String testLocation = "Edmonton, AB - 10310 83 Ave NW, Edmonton, AB T6E 2C6";
        announcementList = new ArrayList<String>();
        announcementList.add("• The Old Strathcona Summer Rib Fest is now open! Come join us for a day of fun and delicious ribs!");
        announcementList.add("• We are excited to announce that we will be having a live band at the event!");
        announcementList.add("• We are running out of ribs! Come get them while they last!");
        announcementList.add("• Restocking ribs! We will be back in 30 minutes!");
        announcementList.add("• Buy 1 rack of ribs, get the second rack 50% off!");
        announcementList.add("• We are now closed for the day. Thank you to everyone who came out to the event!");

        event = new Event(0, testTitle, testDescription, testStartDate, testEndDate, testStartTime, testEndTime, testLocation, 0, announcementList);
        return event;
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
        TextView eventTitle = view.findViewById(R.id.event_title);
        TextView eventDescription = view.findViewById(R.id.event_description);
        TextView eventDate = view.findViewById(R.id.event_date);
        TextView eventLocation = view.findViewById(R.id.event_location);
        ArrayList<String> announcementList = new ArrayList<String>();
        ArrayAdapter<String> announcementAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);
        ListView announcementListView = view.findViewById(R.id.event_announcements_list);
        ImageView eventImage = view.findViewById(R.id.event_banner);
        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        Button uploadImageButton = view.findViewById(R.id.edit_banner_button);

        // If there is no event passed in, use the test event
        if (getArguments() == null) {
            event = createTestEvent();
        }

        if (event.getEventBanner() != null) {
            eventImage.setImageBitmap(event.getEventBanner());
        }
        else {
            eventImage.setVisibility(View.GONE);
        }

        eventTitle.setText(event.getTitle());
        eventDescription.setText(event.getDescription());
        String eventDateString = event.getStartDate() + " at " + event.getStartTime() + " until " + event.getEndDate() + " at " + event.getEndTime();
        eventDate.setText(eventDateString);
        eventLocation.setText(event.getLocation());
        announcementList = event.getAnnouncements();

        announcementAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);

        announcementListView = view.findViewById(R.id.event_announcements_list);

        announcementListView.setAdapter(announcementAdapter);
        ListViewHelper.getListViewSize(announcementListView);

        backButton.setOnClickListener(v -> {
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.event_title));
            fragmentTransaction.commit();
        });

        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        eventImage.setImageURI(result);
                        eventImage.setVisibility(View.VISIBLE);
                        uploadImageButton.setVisibility(View.GONE);
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
}