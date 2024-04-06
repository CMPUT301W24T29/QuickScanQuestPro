package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This fragment displays a list of attendees for a specific event.
 * It allows the organizer to view the list of attendees and the number of check-ins for each attendee.
 */
public class AttendeesListFragment extends Fragment {
    private Event event;
    private DatabaseService databaseService = new DatabaseService();
    private ArrayList<ArrayList<Object>> checkInList;
    private AttendeesListAdapter attendeesListAdapter;
    private Timer timer;
    private TextView attendeeTotal;
    private TextView checkInTotal;
    private ListView attendeesListView;
    private boolean firstrun = true;

    private ArrayList<String> UserIds = new ArrayList<>();

    /**
     * This is the constructor for the AttendeesListFragment class.
     * It expects a list of check-ins for the event in the form of an ArrayList of ArrayLists,
     * where each inner ArrayList contains the user's name and the number of check-ins to the event.
     */
    public AttendeesListFragment(Event event) {
        this.event = event;
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
        attendeesListView = view.findViewById(R.id.event_attendee_list);
        checkInTotal = view.findViewById(R.id.live_count_number);
        attendeeTotal = view.findViewById(R.id.attendee_count_number);

        backButton.setOnClickListener(v -> {
            // goes back to event details page
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        // Initialize checkInList with empty data
        checkInList = new ArrayList<>();
        attendeesListAdapter = new AttendeesListAdapter(getContext(), checkInList);
        attendeesListView.setAdapter(attendeesListAdapter);

        startFetchingData();
        FloatingActionButton alertButton = view.findViewById(R.id.announcement_button);
        alertButton.setOnClickListener(v -> {
            // Create a new AttendeeAlertsFragment
            firstrun = true;
            AttendeeAlertsFragment attendeeAlertsFragment = new AttendeeAlertsFragment(UserIds);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, attendeeAlertsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    /**
     * This method starts a timer that fetches data from the database every 5 seconds.
     * It uses a Timer and TimerTask to schedule the data fetching task.
     * The task fetches the data from the database and updates the UI on the main thread.
     */
    private void startFetchingData() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Fetch data from the database (you need to implement this method)
                fetchData();
            }
        }, 0, 1000); // Schedule to run every 5 seconds (5000 milliseconds)
    }

    /**
     * This method fetches the data from the database and updates the UI with the new data every 5 seconds.
     * It fetches the event details and the list of attendees for the event.
     */
    private void fetchData() {
        databaseService.getEvent(event.getId(), event -> {
            if (event != null) {

                this.event = event;
                checkInList = getAttendees();

            } else {
                Log.e("AttendeesListFragment", "Event is null. Cannot fetch details.");
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
    }


    /**
     * This method fetches the list of attendees for the event from the database.
     * It updates the attendeesListAdapter with the new data and updates the total attendee count.
     * @return The updated list of attendees.
     */

    private ArrayList<ArrayList<Object>> getAttendees() {
        ArrayList<ArrayList<Object>> newCheckInList = event.countAttendees();
        boolean dataChanged = !checkInList.equals(newCheckInList);

        // Update only if data has changed
        if (dataChanged || firstrun) {
            checkInList = newCheckInList;
            AtomicInteger totalAttendees = new AtomicInteger(0);
            int size = checkInList.size(); // Size of checkInList to compare with the count
            int unknownUserCount = 0;

            for (ArrayList<Object> attendee : checkInList) {
                String userId = (String) attendee.get(0);
                databaseService.getSpecificUserDetails(userId, new DatabaseService.OnUserDataLoaded() {
                    @Override
                    public void onUserLoaded(User user) {
                        // Check if the activity is still running
                        if (getActivity() == null) {
//                            Log.e("AttendeeListFragment", "Activity is null. Skipping setup.");
                            return;
                        }
                        // Update the user's name in the list
                        if (user == null) {
//                            Log.e("AttendeeListFragment", "User not found.");
                            attendee.set(0, "Unknown User");
                        } else {
//                            Log.d("AttendeeListFragment", user.getName() + " found!");
                            attendee.set(0, user.getName());
                        }
                        // Increment the totalAttendees count
                        if (totalAttendees.incrementAndGet() == size) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                // Update the UI on the main thread
                                public void run() {
                                    attendeesListAdapter.updateAttendeesList(checkInList);
                                    if (attendeesListView.getVisibility() == View.GONE) {
                                        attendeesListView.setVisibility(View.VISIBLE);
                                    }
                                    attendeeTotal.setText(String.valueOf(checkInList.size()));
                                    int totalAttendees = 0;
                                    for (ArrayList<Object> checkIn : checkInList) {
                                        totalAttendees += (int) checkIn.get(1);
                                    }
                                    checkInTotal.setText(String.valueOf(totalAttendees));
                                }
                            });
                        }
                    }
                });
            }
        }
        firstrun = false;
        for (ArrayList<Object> attendee : checkInList) {
            String Id = (String) attendee.get(0);
            if(!UserIds.contains(Id))
                UserIds.add(Id);
        }
        return checkInList;
    }
}