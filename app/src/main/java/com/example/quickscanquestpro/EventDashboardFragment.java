package com.example.quickscanquestpro;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Displays events that user has created or is attending.
 */
public class EventDashboardFragment extends Fragment {
    private ArrayList<String> eventDataList;

    private ArrayAdapter<String> eventArrayAdapter;

    private RecyclerView eventList;

    private List<Event> events = new ArrayList<>();

    private DatabaseService databaseService = new DatabaseService();

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
        EventListAdapter eventAdapter = new EventListAdapter(getContext(), new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);

        // Use a data loading method. It could be `listenForEventUpdates` or any other method you have.
        databaseService.getEvents(updatedEvents -> {
            // Ensure fragment is still attached to an activity
            if (isAdded() && getActivity() != null) {
                eventAdapter.updateEvents(updatedEvents);
            }
        });

        Button createButton = view.findViewById(R.id.event_dashboard_create_button);
        createButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.content, new EventCreationFragment()); // Ensure that 'R.id.content' is your container ID in the layout.
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

}