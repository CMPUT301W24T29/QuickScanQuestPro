package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class AdminManageEventsFragment extends Fragment {

    DatabaseService dbService = new DatabaseService();

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

        // get all the events from the database
        List<Event> events = dbService.getEvents();


        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // Check if there are entries in the back stack
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                // If there are, pop the back stack to go to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });
    }

}