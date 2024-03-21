package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class AttendeesListFragment extends Fragment {

    private DatabaseService databaseService;
    private ArrayList<User> attendeesList;
    private ArrayList<ArrayList<Object>> checkInList;

    public AttendeesListFragment(ArrayList<ArrayList<Object>> checkInList) {
        this.checkInList = checkInList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_attendee_list, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseService = new DatabaseService();
        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        ListView attendeesListView = view.findViewById(R.id.event_attendee_list);

        backButton.setOnClickListener(v -> {

            // if the user is organiser, i want to go back to admin event details page
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();

        });

        // Create an AttendeesListAdapter and set it to the ListView
        AttendeesListAdapter attendeesListAdapter = new AttendeesListAdapter(getContext(), checkInList);
        attendeesListView.setAdapter(attendeesListAdapter);
    }
}