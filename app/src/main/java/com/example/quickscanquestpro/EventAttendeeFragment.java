package com.example.quickscanquestpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAttendeeFragment extends Fragment {

    private DatabaseService databaseService;

    private Event event;

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
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView eventListView = view.findViewById(R.id.event_attendee_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // i want to go back to the prev fragment
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        /**
         * This is where you should call your databaseService to get the event data and get the attendees
         * and then set the adapter to the list view
         */

        // Initialize HashMap to store unique attendees and their check-in counts
        Map<User, Integer> attendeeMap = new HashMap<>();
        Map<String, Integer> userOccurrenceMap = new HashMap<>();

        databaseService.getEventAttendees(event.getId(), new DatabaseService.OnEventDataLoaded() {

            @Override
            public void onEventLoaded(Event event) {
                attendeeMap.clear();
                // Get the attendees from the event
                List<User> attendees = event.getAttendees();

                // check if the attendees list is empty
                if (attendees == null) {
                    // Show a toast message if the attendees list is empty
                    Toast.makeText(getActivity(), "No attendees found", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    for (User attendee : attendees) {
                        userOccurrenceMap.put(attendee.getUserId(), userOccurrenceMap.getOrDefault(attendee, 0) + 1);
                    }
                    // Iterate through attendees and update the HashMap
                    for (User attendee : attendees) {
                        if (attendeeMap.containsKey(attendee)) {
                            attendeeMap.put(attendee, attendeeMap.get(attendee) + 1);
                        } else {
                            attendeeMap.put(attendee, 1);
                        }
                    }
                }
                // Convert HashMap to array for adapter
                User[] uniqueAttendees = attendeeMap.keySet().toArray(new User[0]);
                Log.d("Attendees", "onEventLoaded: " + uniqueAttendees);
                Integer[] checkInCounts = attendeeMap.values().toArray(new Integer[0]);

                // Create and set custom adapter
                EventAttendeeAdapter adapter = new EventAttendeeAdapter(getActivity(), uniqueAttendees, checkInCounts);
                eventListView.setAdapter(adapter);
            }
        });
    }
}


