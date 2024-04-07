package com.example.quickscanquestpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Fragment} subclass for the administrative interface that allows for managing events
 * within the application. It displays a list of current and future events, providing functionality
 * for reviewing, editing, or deleting events as needed. This fragment uses a {@link RecyclerView}
 * to display the events and integrates with a {@link DatabaseService} to fetch and manage the event
 * data.
 */
public class AdminManageEventsFragment extends Fragment {
    private DatabaseService databaseService = new DatabaseService();


    /**
     * Default constructor for the fragment. This is required for instantiating the fragment
     * by the Android framework. It is recommended not to perform any initializations here
     * that require context or access to activity resources, as those are not yet available
     * at the point this constructor is called.
     */
    public AdminManageEventsFragment() {
    }

    /**
     * Called to do initial creation of the fragment. This is called after onAttach(Activity)
     * and before onCreateView(LayoutInflater, ViewGroup, Bundle). Any non-view initialization
     * should be done here.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and
     * non-graphical fragments can return null. This method will be called between onCreate(Bundle)
     * and onViewCreated(View, Bundle).
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     *                           as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events_manage, container, false);
    }


    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before
     * any saved state has been restored into the view. This gives subclasses a chance to initialize
     * themselves once they know their view hierarchy has been completely created. The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     *                           as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView eventsRecyclerView = view.findViewById(R.id.browse_events_dashboard_list);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton backButton = view.findViewById(R.id.back_button);

        boolean isAdmin = false;
        if (getActivity() instanceof MainActivity) {
            isAdmin = ((MainActivity) getActivity()).getUser().isAdmin();
        }

        AdminEventAdapter eventsAdapter = new AdminEventAdapter(getContext(), new ArrayList<>(), isAdmin);
        eventsRecyclerView.setAdapter(eventsAdapter);

        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Event> filteredEvents = new ArrayList<>();

        databaseService.getEvents(updatedEvents -> {
            if (isAdded() && getActivity() != null) {
                for (Event event : updatedEvents) {
                    LocalDate endDate = event.getEndDate();
                    LocalTime endTime = event.getEndTime();
                    LocalDateTime endDateTime = endDate.atTime(endTime);
                    if (endDateTime.compareTo(currentDateTime) >= 0) {
                        filteredEvents.add(event);
                    }
                }
                eventsAdapter.updateEvents(updatedEvents);
            }
        });

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }
}
