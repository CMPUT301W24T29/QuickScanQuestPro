package com.example.quickscanquestpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
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
    private DatabaseService databaseService;
    private EventTypeAdapter adapter;

    private User user;

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
        MainActivity mainActivity = (MainActivity) getActivity();
        super.onViewCreated(view, savedInstanceState);


        RecyclerView eventRecyclerView = view.findViewById(R.id.event_dashboard_list);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<EventDashboardModel> modelList = new ArrayList<>();
        List<Event> checked_in_events = new ArrayList<>();
        List<Event> signed_up_events = new ArrayList<>();
        List<Event> organized_events = new ArrayList<>();
        List<Event> other_events = new ArrayList<>();  //TODO: Remove before final submission (Stores all past events)
        LocalDateTime currentDateTime = LocalDateTime.now();

        if(mainActivity.getUser().getLastCheckIn()!=null){
            databaseService.getEvent(mainActivity.getUser().getLastCheckIn(), event1 -> {
                if (event1 != null) {
                    LocalDate endDate = event1.getEndDate();
                    LocalTime endTime = event1.getEndTime();
                    LocalDateTime endDateTime = endDate.atTime(endTime);
                    if (endDateTime.compareTo(currentDateTime)>=0) {
                        checked_in_events.add(event1);
                    }
                }
            });
        }

        // Use a data loading method. It could be `listenForEventUpdates` or any other method you have.
        databaseService.getEvents(updatedEvents -> {
            // Ensure fragment is still attached to an activity
            if (isAdded() && getActivity() != null && mainActivity.getUser() != null) {
                for (Event event : updatedEvents) {
                    LocalDate endDate = event.getEndDate();
                    LocalTime endTime = event.getEndTime();
                    LocalDateTime endDateTime = endDate.atTime(endTime);
                    if (endDateTime.compareTo(currentDateTime)>=0) {
                        if (event.getOrganizerId().equals(mainActivity.getUser().getUserId())){
                            organized_events.add(event);
                        } //else if (){  TODO: Add condition to get signed up events
                            //signed_up_events.add(event);
                        //}
                    } else {  //TODO: Remove this before submitting
                        other_events.add(event);
                    }
                }

                // Signed up events
                user = mainActivity.getUser();
                databaseService.getUserSignedupEvents(user, signedUpEvents -> {
                    if (isAdded() && getActivity() != null) {
                        modelList.add(new EventDashboardModel(signedUpEvents, "Signed Up Events"));
                        adapter.notifyDataSetChanged();
                    }
                });

                modelList.add(new EventDashboardModel(checked_in_events, "Checked In Events"));
                //modelList.add(new EventDashboardModel(signed_up_events, "Signed Up Events"));
                modelList.add(new EventDashboardModel(organized_events, "Organized Events"));
                modelList.add(new EventDashboardModel(other_events, "Other Events"));

                adapter = new EventTypeAdapter(getContext(), modelList);
                eventRecyclerView.setAdapter(adapter);
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
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.content, new EventCreationFragment(), "EventCreation"); // Ensure that 'R.id.content' is your container ID in the layout.
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button browseButton = view.findViewById(R.id.event_dashboard_browse_button);
        browseButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.content, new BrowseEventsFragment()); // Ensure that 'R.id.content' is your container ID in the layout.
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

//        Button browseButton = view.findViewById(R.id.event_dashboard_browse_button);
//        browseButton.setOnClickListener(v -> {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.replace(R.id.content, new BrowseEventsFragment()); // Ensure that 'R.id.content' is your container ID in the layout.
//            transaction.addToBackStack(null);
//            transaction.commit();
//        });
//    }
}