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
    private Handler handler = new Handler();
    private TextView attendeeTotal;
    private TextView checkInTotal;
    private ListView attendeesListView;
    private boolean firstrun = false;

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

        // Get the list of attendees for the event
        checkInList = event.countAttendees();
        checkInList = getAttendees();

        // Create an AttendeesListAdapter and set it to the ListView
        attendeesListAdapter = new AttendeesListAdapter(getContext(), checkInList);
        attendeesListView.setAdapter(attendeesListAdapter);

        // Set the total number of check-ins and attendees
        attendeeTotal.setText(String.valueOf(checkInList.size()));
        int totalAttendees = 0;
        for (ArrayList<Object> checkIn : checkInList) {
            totalAttendees += (int) checkIn.get(1);
        }
        checkInTotal.setText(String.valueOf(totalAttendees));

        startFetchingData();

    }

    private void startFetchingData() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Fetch data from the database (you need to implement this method)
                fetchData();

                // Update UI on the main thread
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI components with fetched data
                        attendeeTotal.setText(String.valueOf(checkInList.size()));
                        int totalAttendees = 0;
                        for (ArrayList<Object> checkIn : checkInList) {
                            totalAttendees += (int) checkIn.get(1);
                        }
                        checkInTotal.setText(String.valueOf(totalAttendees));
                    }
                });
            }
        }, 0, 5000); // Schedule to run every 5 seconds (5000 milliseconds)
    }

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
     * This method gets the list of attendees for the event from the database. It calls the getUsers method
     * from the DatabaseService class and passes in an OnUsersDataLoaded listener. When the list of users is
     * loaded, the onUsersLoaded method is called and the list of users is passed in. The method then calls
     * the countAttendees method from the Event class and passes in the list of users. The method then sets
     * the checkInList field to the list of attendee names with the number of check-ins for each attendee.
     */


/*    private ArrayList<ArrayList<Object>> getAttendees() {
        checkInList = event.countAttendees();
        for (ArrayList<Object> attendee: checkInList) {
            databaseService.getSpecificUserDetails(attendee.get(0).toString(), new DatabaseService.OnUserDataLoaded() {
                @Override
                public void onUserLoaded(User user) {
                    if (getActivity() == null) {
                        Log.e("AttendeeListFragment", "Activity is null. Skipping setup.");
                        return;
                    }

                    if (user == null) {

                        Log.e("AttendeeListFragment",  "User not found.");
                        attendee.set(0, "Unknown User");
                    } else {
                        Log.d("AttendeeListFragment", user.getName() + " found!");
                        // checkInList will be a list of user names and the number of check-ins for each user
                        attendee.set(0, user.getName());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendeesListAdapter.updateAttendeesList(checkInList);
                                if (attendeesListView.getVisibility() == View.GONE) {
                                    attendeesListView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            });
        }
        return checkInList;
    }*/

    private ArrayList<ArrayList<Object>> getAttendees() {
        ArrayList<ArrayList<Object>> newCheckInList = event.countAttendees();
        boolean dataChanged = !checkInList.equals(newCheckInList);

        // Update only if data has changed
        if (dataChanged | ! firstrun) {
            checkInList = newCheckInList;
            for (ArrayList<Object> attendee : checkInList) {
                databaseService.getSpecificUserDetails(attendee.get(0).toString(), new DatabaseService.OnUserDataLoaded() {
                    @Override
                    public void onUserLoaded(User user) {
                        if (getActivity() == null) {
                            Log.e("AttendeeListFragment", "Activity is null. Skipping setup.");
                            return;
                        }

                        if (user == null) {
                            Log.e("AttendeeListFragment", "User not found.");
                            attendee.set(0, "Unknown User");
                        } else {
                            Log.d("AttendeeListFragment", user.getName() + " found!");
                            // checkInList will be a list of user names and the number of check-ins for each user
                            attendee.set(0, user.getName());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                attendeesListAdapter.updateAttendeesList(checkInList);
                                if (attendeesListView.getVisibility() == View.GONE) {
                                    attendeesListView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        }
        firstrun = true;
        return checkInList;
    }

}