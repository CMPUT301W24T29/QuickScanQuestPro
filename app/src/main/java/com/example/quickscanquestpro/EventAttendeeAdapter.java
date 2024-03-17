package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EventAttendeeAdapter extends ArrayAdapter<User>{

    private Context mContext;
    private User[] attendees;
    private Integer[] checkInCounts;

    public EventAttendeeAdapter(@NonNull Context context, User[] attendees, Integer[] checkInCounts) {
        super(context, R.layout.list_attendee_view, attendees);
        this.mContext = context;
        this.attendees = attendees;
        this.checkInCounts = checkInCounts;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_attendee_view, parent, false);
        }

        User attendee = getItem(position);
        if (attendee != null) {
            TextView attendeeName = convertView.findViewById(R.id.attendee_name_text_view);
            TextView checkInCount = convertView.findViewById(R.id.attendee_check_in_text_view);

            attendeeName.setText(attendee.getName());
            checkInCount.setText(checkInCounts[position].toString());
        }

        return convertView;
    }
}

