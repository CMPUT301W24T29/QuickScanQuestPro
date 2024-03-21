package com.example.quickscanquestpro;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class EventViewHolder extends RecyclerView.ViewHolder {

    ImageView eventImage;
    TextView eventTitle, eventLocation, eventDates, eventTimes;

    public EventViewHolder(View itemView) {
        super(itemView);
        eventImage = itemView.findViewById(R.id.event_image);
        eventTitle = itemView.findViewById(R.id.event_title);
        eventLocation = itemView.findViewById(R.id.event_location);
        eventDates = itemView.findViewById(R.id.event_dates);
        eventTimes = itemView.findViewById(R.id.event_times);
    }
}
