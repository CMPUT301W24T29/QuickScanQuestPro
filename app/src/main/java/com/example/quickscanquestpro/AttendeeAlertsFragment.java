package com.example.quickscanquestpro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * create an alert to send to all attendees of an event if they have notifications turned on
 */
public class AttendeeAlertsFragment extends DialogFragment {

    public AttendeeAlertsFragment() {
        // Required empty public constructor
    }

    private DatabaseService databaseService = new DatabaseService();
    private ArrayList<String> uniqueAttendees;

    private ArrayList<String> announcements;
    private String eventID;

    /**
     *  this is the list of unique attendees attending the event
     * @param uniqueAttendees
     */
    public AttendeeAlertsFragment(ArrayList<String> uniqueAttendees, String eventID) {
        this.uniqueAttendees = uniqueAttendees;
        this.eventID = eventID;
        Log.d("Notification", "Unique attendees: " + uniqueAttendees);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alert_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        databaseService.getEvent(eventID, new DatabaseService.OnEventDataLoaded() {
            @Override
            public void onEventLoaded(Event event) {
                if (event == null) {
                    Log.d("Notification", "Event not found");
                    return;
                }
                announcements = event.getAnnouncements();
                // get the title and body from the alert_content.xml
                EditText title = view.findViewById(R.id.edit_notification_title);
                EditText body = view.findViewById(R.id.edit_notification_body);
                // get the send button from the alert_content.xml
                view.findViewById(R.id.create_notification_confirm_button).setOnClickListener(v -> {
                    // send the alert
                    sendAlert(title.getText().toString(), body.getText().toString());
                    announcements.add(title.getText().toString() + ": " + body.getText().toString());
                    event.setAnnouncements(announcements);
                    databaseService.addEvent(event);
                    Log.d("Got till here", "Got till here");
                    // go back to event attendees fragment
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.popBackStack();
                });

                FloatingActionButton fab = view.findViewById(R.id.send_alert_back_button);
                fab.setOnClickListener(v -> {
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.popBackStack();
                });
            }
        });
    }

    /**
     * called on once the organiser clicks send notification button
     * @param title  the title of the notification
     * @param body   the body of the notification
     */
    public void sendAlert(String title, String body) {
        try {
            // send to all users in uniqueAttendees
            for (String userId : uniqueAttendees) {
                Log.d("Notification", "Sending notification to user: " + userId);
                // get specific user details form databaseService and use onUserLoaded to send notification
                databaseService.getSpecificUserDetails(userId, new DatabaseService.OnUserDataLoaded() {
                    @Override
                    public void onUserLoaded(User user) {
                        if(user == null)
                        {
                            Log.d("Notification", "User not found");
                            return;
                        }
                        if(user.getGetNotification() == false || user.getGetNotification() == null)
                        {
                            // skip this iteration
                            Log.d("Notification", "User: " + user.getName() + " has notifications turned off");
                        }
                        else{
                            JSONObject jsonObject = new JSONObject();
                            try {
                                Log.d("Notification", "Sending notification to user: " + user.getName());

                                JSONObject notification = new JSONObject();
                                notification.put("title", title);
                                notification.put("body", body);

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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  this calls the API to send a request FCM to send a notification to all attendees that have notifications turned on
     * @param jsonObject
     */
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