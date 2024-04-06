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

public class AdminManageEventsFragment extends Fragment {
    private DatabaseService databaseService = new DatabaseService();

    public AdminManageEventsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events_manage, container, false);
    }

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
                eventsAdapter.updateEvents(filteredEvents);
            }
        });

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }
}
