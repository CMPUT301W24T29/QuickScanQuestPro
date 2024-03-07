package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.example.quickscanquestpro.Event;

import java.util.List;

public class AdminManageEventsFragment extends Fragment {

    private DatabaseService databaseService;

    public AdminManageEventsFragment() {
        // Required empty public constructor
    }

    public static AdminManageEventsFragment newInstance() {
        return new AdminManageEventsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView eventListView = view.findViewById(R.id.admin_event_dashboard_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Fetch users from Firestore and update the ListView
        databaseService.listenForEventUpdates(new DatabaseService.onEventsDataLoaded() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (getActivity() == null) {
                    Log.e("AdminProfileFragment", "Activity is null. Skipping setup.");
                    return;
                }

                if (events.isEmpty()) {
                    Toast.makeText(getActivity(), "No users found!", Toast.LENGTH_SHORT).show();
                } else {
                    AdminEventAdapter adapter = new AdminEventAdapter(getActivity(), R.layout.list_admin_view, events);
                    // Set the adapter for the eventListView
                    eventListView.setOnItemClickListener((parent, view, position, id) -> {
                        Log.d("ItemClick", "Item clicked at position: " + position);
                        Event event = events.get(position);
                        // Create an instance of EventDetailsFragment
                        EventDetailsFragment adminEventDetailsFragment = new EventDetailsFragment(event);

                        // Use FragmentManager to replace the AdminManageEventsFragment with EventDetailsFragment
                        if (isAdded() && getActivity() != null) {
                            getChildFragmentManager().beginTransaction()
                                    .replace(R.id.content, adminEventDetailsFragment)
                                    .addToBackStack(null)  // Optional, if you want to navigate back to the admin manage events
                                    .commit();
                        }
                    });
                    eventListView.setAdapter(adapter);
                    Log.d("Check", "Its gotten till here");
                }
            }
        });

    }

}