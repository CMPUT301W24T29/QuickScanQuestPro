package com.example.quickscanquestpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Allows users to browse events that have not yet ended
 */
public class BrowseEventsFragment extends Fragment {
    private ArrayList<String> eventDataList;

    private ArrayAdapter<String> eventArrayAdapter;

    private ListView eventList;

    private DatabaseService databaseService = new DatabaseService();

    public BrowseEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse_events, container, false);
    }

    /**
     * Runs when view is created and displayed.
     * Displays a list of events that have not yet ended
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView eventRecyclerView = view.findViewById(R.id.browse_events_dashboard_list);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton backButton = view.findViewById(R.id.back_button);

        // Initialize with an empty list; we will update the list when data is loaded
        EventListAdapter eventAdapter = new EventListAdapter(getContext(), new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);

        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Event> filteredEvents = new ArrayList<>();

        // Use a data loading method. It could be `listenForEventUpdates` or any other method you have.
        databaseService.getEvents(updatedEvents -> {
            // Ensure fragment is still attached to an activity
            if (isAdded() && getActivity() != null) {
                for (Event event : updatedEvents) {
                    LocalDate endDate = event.getEndDate();
                    LocalTime endTime = event.getEndTime();
                    LocalDateTime endDateTime = endDate.atTime(endTime);
                    if (endDateTime.compareTo(currentDateTime) >= 0) {
                        filteredEvents.add(event);
                    }
                }
                // Display only current and future events
                eventAdapter.updateEvents(filteredEvents);
//                eventAdapter.updateEvents(updatedEvents);
            }
        });

        backButton.setOnClickListener(v -> {

            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();

        });
    }
}
