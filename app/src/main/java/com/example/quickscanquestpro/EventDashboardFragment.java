package com.example.quickscanquestpro;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Displays events that user has created or is attending.
 */
public class EventDashboardFragment extends Fragment {

    public EventDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_dashboard, container, false);
    }

    /**
     * Runs when view is created and displayed. Currently adds a test event to the list if none exist, or otherwise displays attributes of the event stored in main activity.
     * This should display a list of the users events they have created as organizer or are actively attending.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) this.getActivity();

        ArrayList<String> eventDataList = new ArrayList<>();
        eventDataList.add("Test Event ID: " + mainActivity.getTestEvent().getId().toString() + " | Title: " + mainActivity.getTestEvent().getTitle() + " | Description: " + mainActivity.getTestEvent().getDescription() + " | Location: " + mainActivity.getTestEvent().getLocation() + " | Start: " + mainActivity.getTestEvent().getStartDate().toString() + " at " + mainActivity.getTestEvent().getStartTime().toString() + " | End: " + mainActivity.getTestEvent().getEndDate().toString() + " at " + mainActivity.getTestEvent().getEndTime().toString() + " | QR Code: " + String.valueOf(mainActivity.getTestEvent().getCheckinQRCode().hashCode()));

        ListView eventList = view.findViewById(R.id.event_dashboard_list);
        ArrayAdapter<String> eventArrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, eventDataList);
        eventList.setAdapter(eventArrayAdapter);

        Button createButton = view.findViewById(R.id.event_dashboard_create_button);
        createButton.setOnClickListener(v -> {
            mainActivity.transitionFragment(new EventCreationFragment(), "EventCreationFragment");
        });

        // set the event list to open the event details fragment when an event is clicked
        eventList.setOnItemClickListener((parent, view1, position, id) -> {
            mainActivity.transitionFragment(new EventDetailsFragment(mainActivity.getTestEvent()), "EventDetailsFragment");
        });
    }
}