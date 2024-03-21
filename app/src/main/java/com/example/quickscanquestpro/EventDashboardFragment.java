package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Displays events that user has created or is attending.
 */
public class EventDashboardFragment extends Fragment {
    private ArrayList<String> eventDataList;

    private ArrayAdapter<String> eventArrayAdapter;

    private ListView eventList;

    private DatabaseService databaseService = new DatabaseService();
    private static final String TAG = "EventDashboardFragment";

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
        super.onViewCreated(view, savedInstanceState);

        RecyclerView eventRecyclerView = view.findViewById(R.id.event_dashboard_list);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize with an empty list; we will update the list when data is loaded
        BrowseEventListAdapter eventAdapter = new BrowseEventListAdapter(getContext(), new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);

        // Use a data loading method. It could be `listenForEventUpdates` or any other method you have.
        databaseService.getEvents(updatedEvents -> {
            // Ensure fragment is still attached to an activity
            if (isAdded() && getActivity() != null) {
                LocalDateTime currentDateTime = LocalDateTime.now();
                Toast.makeText(getContext(), currentDateTime.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Dashboard", currentDateTime.toString());
                List<Event> filteredEvents = new ArrayList<>();
                for (Event event : updatedEvents) {
                    LocalDate endDate = event.getEndDate();
                    LocalTime endTime = event.getEndTime();
                    LocalDateTime endDateTime = endDate.atTime(endTime);
                    if (endDateTime.compareTo(currentDateTime)>=0) {
                        filteredEvents.add(event);
                    }
                }
                eventAdapter.updateEvents(filteredEvents);
            }
        });

        Button createButton = view.findViewById(R.id.event_dashboard_create_button);
        createButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.content, new EventCreationFragment()); // Ensure that 'R.id.content' is your container ID in the layout.
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button browseButton = view.findViewById(R.id.event_dashboard_browse_button);
        browseButton.setOnClickListener(v -> {

        });
    }

    public List<String> setEventHeaders() {
        List <String> eventHeaders = new ArrayList<>();
        eventHeaders.add("Checked-In Events");
        //eventHeaders.add("Signed-Up Events");
        //eventHeaders.add("Organized Events");

        return eventHeaders;
    }

}