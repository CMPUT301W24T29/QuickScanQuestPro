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

/**
 * This class is an adapter for the ListView that displays the list of attendees for an event.
 * It displays the user's name and the number of check-ins they have made to the event.
 */

public class AttendeesListAdapter extends ArrayAdapter<ArrayList<Object>> {
    private int resourceLayout;
    private Context mContext;

    // Will pass a list of user ids from check ins
    public AttendeesListAdapter(@NonNull Context context, ArrayList<ArrayList<Object>> attendeesList) {
        super(context, R.layout.list_attendees_view, attendeesList);
        this.resourceLayout = R.layout.list_attendees_view;
        mContext = context;
    }
    /**
     * This method is called when the ListView is created.
     * It sets the text of the TextViews in the ListView to the user's name and the number of check-ins they have made.
     * @param position The position of the item in the ListView.
     * @param view The view to be displayed.
     * @param parent The parent view of the ListView.
     * @return The view to be displayed.
     */
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

    /**
     * Updates the list of attendees displayed in the ListView.
     * @param attendeesList The list of attendees to display.
     */
    public void updateAttendeesList(ArrayList<ArrayList<Object>> attendeesList) {
        clear();
        addAll(attendeesList);
        notifyDataSetChanged();
    }
}
