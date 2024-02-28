package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

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
        announcementList = new ArrayList<String>();
        eventImage = view.findViewById(R.id.event_banner);

        // If there is no event passed in, use the test event
        if (getArguments() == null) {

        eventImage.setImageResource(R.drawable.pork_ribs);
        eventTitle.setText("Old Strathcona Summer Rib Fest");
        eventDescription.setText("Come join us for the 2021 Old Strathcona Summer Rib Fest! Enjoy a variety of delicious ribs, live music, and more!");
        eventDate.setText("August 20, 2021" + " - " + "August 22, 2021" + " 11:00 AM - 9:00 PM");
        eventLocation.setText("Edmonton, AB - 10310 83 Ave NW, Edmonton, AB T6E 2C6");
        announcementList.add("• The Old Strathcona Summer Rib Fest is now open! Come join us for a day of fun and delicious ribs!");
        announcementList.add("• We are excited to announce that we will be having a live band at the event!");
        announcementList.add("• We are running out of ribs! Come get them while they last!");
        announcementList.add("• Restocking ribs! We will be back in 30 minutes!");
        announcementList.add("• Buy 1 rack of ribs, get the second rack 50% off!");
        announcementList.add("• We are now closed for the day. Thank you to everyone who came out to the event!");
        }

        // If there is no event passed in, use the test event
        else {
            if (event.getEventBanner() != null) {
                eventImage.setImageBitmap(event.getEventBanner());
            }
            eventTitle.setText(event.getTitle());
            eventDescription.setText(event.getDescription());
            String eventDateString = event.getStartDate() + " - " + event.getEndDate() + " " + event.getStartTime() + " - " + event.getEndTime();
            eventDate.setText(eventDateString);
            eventLocation.setText(event.getLocation());
            announcementList = event.getAnnouncements();
        }

        announcementAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, announcementList);

        announcementListView = view.findViewById(R.id.event_announcements_list);

        announcementListView.setAdapter(announcementAdapter);
        ListViewHelper.getListViewSize(announcementListView);

        FloatingActionButton backButton = view.findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.event_title));
            fragmentTransaction.commit();
        });
    }
}