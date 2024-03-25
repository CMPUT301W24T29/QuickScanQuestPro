package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AttendeesListAdapter extends ArrayAdapter<ArrayList<Object>> {
    private int resourceLayout;
    private Context mContext;

    // Will pass a list of user ids from check ins
    public AttendeesListAdapter(@NonNull Context context, ArrayList<ArrayList<Object>> attendeesList) {
        super(context, R.layout.list_attendees_view, attendeesList);
        this.resourceLayout = R.layout.list_attendees_view;
        mContext = context;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        ArrayList<Object> attendeeCheckins = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        TextView attendeeName = view.findViewById(R.id.attendee_name);
        TextView attendeeCheckIn = view.findViewById(R.id.attendee_checkins);

        // Set the text of the TextViews to the user's name and number of check-ins
        attendeeName.setText(attendeeCheckins.get(0).toString());
        attendeeCheckIn.setText("Check-ins: " + attendeeCheckins.get(1).toString());

        return view;
    }
}
