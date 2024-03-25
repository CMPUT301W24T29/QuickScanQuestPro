package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * This fragment displays a list of attendees for a specific event.
 * It allows the organizer to view the list of attendees and the number of check-ins for each attendee.
 */
public class AttendeesListFragment extends Fragment {
    private ArrayList<ArrayList<Object>> checkInList;

    /**
     * This is the constructor for the AttendeesListFragment class.
     * It expects a list of check-ins for the event in the form of an ArrayList of ArrayLists,
     * where each inner ArrayList contains the user's name and the number of check-ins to the event.
     */
    public AttendeesListFragment(ArrayList<ArrayList<Object>> checkInList) {
        this.checkInList = checkInList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_attendee_list, container, false);
    }

    /**
     * Initializes all the views. Sets up adapter to hold attendees and their check-ins.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        ListView attendeesListView = view.findViewById(R.id.event_attendee_list);
        TextView checkInTotal = view.findViewById(R.id.live_count_number);
        TextView attendeeTotal = view.findViewById(R.id.attendee_count_number);

        backButton.setOnClickListener(v -> {

            // goes back to event details page
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();

        });

        // Create an AttendeesListAdapter and set it to the ListView
        AttendeesListAdapter attendeesListAdapter = new AttendeesListAdapter(getContext(), checkInList);
        attendeesListView.setAdapter(attendeesListAdapter);

        // Set the total number of check-ins and attendees
        attendeeTotal.setText(String.valueOf(checkInList.size()));
        int totalAttendees = 0;
        for (ArrayList<Object> checkIn : checkInList) {
            totalAttendees += (int) checkIn.get(1);
            }
        checkInTotal.setText(String.valueOf(totalAttendees));
    }
}