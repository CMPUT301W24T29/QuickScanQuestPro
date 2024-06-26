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
        FloatingActionButton expandButton = view.findViewById(R.id.expand_attendee_button);
        expandButton.setTag("false");
        FloatingActionButton mapButton = view.findViewById(R.id.heatmap_button);
        mapButton.setVisibility(View.GONE);
        attendeesListView = view.findViewById(R.id.event_attendee_list);
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
        alertButton.setVisibility(View.GONE);
        alertButton.setOnClickListener(v -> {
            // Create a new AttendeeAlertsFragment
            firstrun = true;
            AttendeeAlertsFragment attendeeAlertsFragment = new AttendeeAlertsFragment(UserIds, event.getId());
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, attendeeAlertsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        expandButton.setOnClickListener(v -> {
            if (expandButton.getTag() == "false") {
                expandButton.setImageResource(R.drawable.baseline_close_24);
                alertButton.setVisibility(View.VISIBLE);
                mapButton.setVisibility(View.VISIBLE);
                expandButton.setTag("true");

            } else {
                expandButton.setImageResource(R.drawable.baseline_menu_24);
                alertButton.setVisibility(View.GONE);
                mapButton.setVisibility(View.GONE);
                expandButton.setTag("false");
            }
        });

        mapButton.setOnClickListener(view1 -> {
            MainActivity mainActivity = (MainActivity) this.getActivity();
            mainActivity.transitionFragment(new AttendeesHeatmapFragment(event), "Heatmap");
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
        }, 0, 5000); // Schedule to run every 5 seconds (5000 milliseconds)
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
//                FragmentManager fragmentManager = getParentFragmentManager();
//                fragmentManager.popBackStack();
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
            AtomicInteger totalAttendees = new AtomicInteger(0);
            int size = newCheckInList.size(); // Size of checkInList to compare with the count

            for (ArrayList<Object> attendee : newCheckInList) {
                String userId = (String) attendee.get(0);
                databaseService.getSpecificUserDetails(userId, new DatabaseService.OnUserDataLoaded() {
                    @Override
                    public void onUserLoaded(User user) {
                        // Check if the activity is still running
                        if (getActivity() == null) {
                            Log.e("AttendeeListFragment", "Activity is null. Skipping setup.");
                            return;
                        }
                        // Update the user's name in the list
                        if (user == null) {
                            attendee.set(0, "Deleted User");
                        } else {
                            if (user.getName().equals("")) {
                                attendee.set(0, "Unnamed User");
                            } else {
                                attendee.set(0, user.getName());
                            }
                        }
                        // Increment the totalAttendees count
                        if (totalAttendees.incrementAndGet() == size) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                // Update the UI on the main thread
                                public void run() {
                                    attendeesListAdapter.updateAttendeesList(newCheckInList);
                                    if (attendeesListView.getVisibility() == View.GONE) {
                                        attendeesListView.setVisibility(View.VISIBLE);
                                    }
                                    attendeeTotal.setText(String.valueOf(newCheckInList.size()));
                                    int totalAttendees = 0;
                                    for (ArrayList<Object> checkIn : newCheckInList) {
                                        totalAttendees += (int) checkIn.get(1);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
        firstrun = false;
        for (ArrayList<Object> attendee : newCheckInList) {
            String Id = (String) attendee.get(0);
            if(!UserIds.contains(Id))
                UserIds.add(Id);
        }
        return newCheckInList;
    }
}