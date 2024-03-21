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
import android.view.MotionEvent;
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

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o) {
                Toast.makeText(getContext(), "Notifications Permission granted", Toast.LENGTH_LONG).show();
            }
        }
    });

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
        super.onViewCreated(view, savedInstanceState);

        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

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
            EventCreationFragment fragment = new EventCreationFragment();
            FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

    }

}