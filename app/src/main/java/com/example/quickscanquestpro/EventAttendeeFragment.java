package com.example.quickscanquestpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EventAttendeeFragment extends Fragment {

    private DatabaseService databaseService;

    private Event event;

    private ArrayList<User> uniqueAttendees;

    // Initialize a counter to track the number of completed async calls
    int completedCalls = 0;

    public EventAttendeeFragment(Event event) {
        this.event = event;
    }

    public EventAttendeeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_attendee_list, container, false);
    }

    /**
     * onViewCreated is called immediately after onCreateView.
     * This is where you should do your view setup.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView attendeeListView = view.findViewById(R.id.event_attendee_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // i want to go back to the prev fragment
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        view.findViewById(R.id.alert_button).setOnClickListener(v -> {
            Log.d("Alert", "Alert button clicked");
            // Create an instance of the AttendeeAlertsFragment
            AttendeeAlertsFragment attendeeAlertsFragment = new AttendeeAlertsFragment(uniqueAttendees);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, attendeeAlertsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        /**
         * This is where you should call your databaseService to get the event data and get the attendees
         * and then set the adapter to the list view
         */

        databaseService.ListenForEventAttendeeUpdates(event.getId(), new DatabaseService.OnEventDataLoaded() {
            @Override
            public void onEventLoaded(Event event) {
                if(event == null)
                {
                    Toast.makeText(getContext(), "this event has no attendees", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get the attendees of the event
                ArrayList<User> attendees = event.getAttendees();
                if(attendees == null || attendees.isEmpty())
                {
                    Toast.makeText(getContext(), "No attendees found", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    // Create a map to store the number of times each user has checked in
                    Map<String, Integer> occurrences = new HashMap<>();
                    for (User attendee : attendees) {
                        String userId = attendee.getUserId();
                        occurrences.put(userId, occurrences.getOrDefault(userId, 0) + 1);
                    }

                    // Create a list to store unique attendees with their check-in counts
                    uniqueAttendees = new ArrayList<>();
                    for (User attendee : attendees) {
                        String userId = attendee.getUserId();
                        // Check if the attendee is not already in the unique list
                        boolean isUnique = true;
                        for (User uniqueAttendee : uniqueAttendees) {
                            if (uniqueAttendee.getUserId().equals(userId)) {
                                isUnique = false;
                                break;
                            }
                        }
                        if (isUnique) {
                            // Set the check-in count for the attendee
                            Log.d("Checkins", String.valueOf(occurrences.get(userId)));
                            attendee.setCheckins(occurrences.get(userId));
                            uniqueAttendees.add(attendee);
                        }
                    }
                }
                // checks if the user exists in the database, if not it deletes it from uniqueAttendees
                for (User attendee : uniqueAttendees) {
                    databaseService.getSpecificUserDetails(attendee.getUserId(), new DatabaseService.OnUserDataLoaded() {

                        @Override
                        public void onUserLoaded(User user) {
                            if(user == null)
                            {
                                // remove the user from the list
                                uniqueAttendees.remove(attendee);
                            }
                            // Create an adapter for the attendee list
                            EventAttendeeAdapter adapter = new EventAttendeeAdapter(mainActivity, R.layout.list_attendee_view, uniqueAttendees);

                            // change this if you need to change the milestone count
                            // if the unique attendees are 2 or more send a notification ot the organiser of the event
                            if(uniqueAttendees.size() >= 3)
                            {
                                sendAlert();
                            }

                            // Set the adapter for the attendeeListView
                            TextView liveAttendeeCount = view.findViewById(R.id.live_count_number);
                            liveAttendeeCount.setText(String.valueOf(uniqueAttendees.size()));
                            attendeeListView.setAdapter(adapter);
                        }
                    });
                }
            }
        });
    }

    public void sendAlert() {
        try {
            // send the notification to the organiser of the event
            String userId = event.getOrganizerId();
            databaseService.getSpecificUserDetails(userId, new DatabaseService.OnUserDataLoaded() {
                @Override
                public void onUserLoaded(User user) {
                    if (user == null) {
                        Log.d("Notification", "User not found");
                        return;
                    }
                    if (user.getGetNotification() == false) {
                        // skip this iteration
                        Log.d("Notification", "User: " + user.getName() + " has notifications turned off");
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            Log.d("Notification", "Sending notification to user: " + user.getName());

                            JSONObject notification = new JSONObject();
                            notification.put("title", "Attendence Milestone");
                            notification.put("body", "You have reached a milestone!!!! Yahoooo!!!!");

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userID", user.getUserId());

                            jsonObject.put("notification", notification);
                            jsonObject.put("data", dataObj);
                            jsonObject.put("to", user.getNotificationToken());

                            callApi(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callApi(JSONObject jsonObject)
    {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                // add the api key after bearer with a space
                .header("Authorization", "Bearer AAAA-z98YP0:APA91bEoBWfmJI7JHaV87puPVmZhDNv-4m0cxhjYXjsD5mAiPoTuhGbC6xfV0rVBt9qXj59n3TPCRe2QnwlZFXb96DvtoxYvyT5tCNqgaR0m8PapWiWHFVWbNpChm37VzNImEXL5T_iu")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("Notification", "Failed to send notification");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("Notification", "Notification sent successfully");
            }
        });
    }
}



