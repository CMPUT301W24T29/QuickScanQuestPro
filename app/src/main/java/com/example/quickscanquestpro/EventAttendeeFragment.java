package com.example.quickscanquestpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EventAttendeeFragment extends Fragment {

    private DatabaseService databaseService;

    private Event event;

    private ArrayList<User> uniqueAttendees;

    // Initialize a counter to track the number of completed async calls
    int completedCalls = 0;

    public EventAttendeeFragment(Event event) {
        this.event = event;
    }

    public EventAttendeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_attendee_list, container, false);
    }

    /**
     * onViewCreated is called immediately after onCreateView.
     * This is where you should do your view setup.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView attendeeListView = view.findViewById(R.id.event_attendee_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // i want to go back to the prev fragment
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        view.findViewById(R.id.alert_button).setOnClickListener(v -> {
            Log.d("Alert", "Alert button clicked");
            // Create an instance of the AttendeeAlertsFragment
            AttendeeAlertsFragment attendeeAlertsFragment = new AttendeeAlertsFragment(uniqueAttendees);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, attendeeAlertsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        /**
         * This is where you should call your databaseService to get the event data and get the attendees
         * and then set the adapter to the list view
         */

        databaseService.ListenForEventAttendeeUpdates(event.getId(), new DatabaseService.OnEventDataLoaded() {
            @Override
            public void onEventLoaded(Event event) {
                if(event == null)
                {
                    Toast.makeText(getContext(), "this event has no attendees", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get the attendees of the event
                ArrayList<User> attendees = event.getAttendees();
                if(attendees == null || attendees.isEmpty())
                {
                    Toast.makeText(getContext(), "No attendees found", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    // Create a map to store the number of times each user has checked in
                    Map<String, Integer> occurrences = new HashMap<>();
                    for (User attendee : attendees) {
                        String userId = attendee.getUserId();
                        occurrences.put(userId, occurrences.getOrDefault(userId, 0) + 1);
                    }

                    // Create a list to store unique attendees with their check-in counts
                    uniqueAttendees = new ArrayList<>();
                    for (User attendee : attendees) {
                        String userId = attendee.getUserId();
                        // Check if the attendee is not already in the unique list
                        boolean isUnique = true;
                        for (User uniqueAttendee : uniqueAttendees) {
                            if (uniqueAttendee.getUserId().equals(userId)) {
                                isUnique = false;
                                break;
                            }
                        }
                        if (isUnique) {
                            // Set the check-in count for the attendee
                            Log.d("Checkins", String.valueOf(occurrences.get(userId)));
                            attendee.setCheckins(occurrences.get(userId));
                            uniqueAttendees.add(attendee);
                        }
                    }

                    Log.d("UniqueAttendees", uniqueAttendees.toString());
                    Log.d("Checkins", String.valueOf(uniqueAttendees.get(0).getCheckins()));

                    // Create an adapter for the attendee list
                    EventAttendeeAdapter adapter = new EventAttendeeAdapter(mainActivity, R.layout.list_attendee_view, uniqueAttendees);

                    // Set the adapter for the attendeeListView
                    TextView liveAttendeeCount = view.findViewById(R.id.live_count_number);
                    liveAttendeeCount.setText(String.valueOf(uniqueAttendees.size()));
                    attendeeListView.setAdapter(adapter);
                }
            }
        });
    }
}



