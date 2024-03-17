package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

    private DatabaseService databaseService;

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
        databaseService = new DatabaseService();
        eventList = view.findViewById(R.id.event_dashboard_list);
        databaseService.listenForEventUpdates(new DatabaseService.OnEventsDataLoaded() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (getActivity() == null) {
                    Log.e("EventDashboardFragment", "Activity is null. Skipping setup.");
                    return;
                }

                if (events.isEmpty()) {
                    Toast.makeText(getActivity(), "No event found!", Toast.LENGTH_SHORT).show();
                } else {

                    EventDashboardAdapter adapter = new EventDashboardAdapter(getContext(), R.layout.list_event_organiser_user_view, events);
                    // Set the adapter for the eventListView
                    eventList.setOnItemClickListener((parent, view, position, id) -> {
                        Log.d("ItemClick", "Item clicked at position: " + position);
                        Event event = events.get(position);
                        EventDetailsFragment eventDetails = new EventDetailsFragment(event);
                        FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content, eventDetails);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    });
                    eventList.setAdapter(adapter);
                    Log.d("Check", "Its gotten till here");
                }
            }
        });

        Button createButton = view.findViewById(R.id.event_dashboard_create_button);
        createButton.setOnClickListener(v -> {
            EventCreationFragment fragment = new EventCreationFragment();
            FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });



    }

}