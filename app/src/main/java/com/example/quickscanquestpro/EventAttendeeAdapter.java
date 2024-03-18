package com.example.quickscanquestpro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAttendeeAdapter extends ArrayAdapter<User>{

    private Context mContext;
    private int resourceLayout;

    private ArrayList<User> attendees;

    private DatabaseService databaseService = new DatabaseService();

    public EventAttendeeAdapter(@NonNull Context context, int resource, ArrayList<User> attendees) {
        super(context, resource, attendees);
        this.mContext = context;
        this.resourceLayout = resource;
        this.attendees = attendees;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        // Get the user ID of the current position
        User currentUser = attendees.get(position);
        String userId = currentUser.getUserId();

        // Fetch the user details from the database based on the user ID
        View finalConvertView = convertView;
        databaseService.getSpecificUserDetails(userId, user -> {
            // Update the list item with the user's name and check-in count
            TextView nameTextView = finalConvertView.findViewById(R.id.attendee_name_text_view);
            TextView checkinCountTextView = finalConvertView.findViewById(R.id.attendee_check_in_text_view);

            if (user != null) {
                nameTextView.setText(user.getName());
            } else {
                nameTextView.setText("Unknown User");
            }
            // convert the integer to a string
            checkinCountTextView.setText(String.valueOf(currentUser.getCheckins()));
        });


        return convertView;
    }



}

