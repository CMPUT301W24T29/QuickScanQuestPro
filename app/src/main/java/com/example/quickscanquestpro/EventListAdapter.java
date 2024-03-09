package com.example.quickscanquestpro;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventDataList;

    public EventListAdapter(Context context, List<Event> eventDataList) {
        this.context = context;
        this.eventDataList = eventDataList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventStartTime,  eventLocation;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventStartTime = itemView.findViewById(R.id.event_start_time);
            eventLocation = itemView.findViewById(R.id.event_location);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventDataList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventLocation.setText(event.getLocation());
        // Set more event details as required, like start time, end time, etc.

        // Here you could use Glide or another image loading library to set the event image
        // Glide.with(context).load(event.getImageUrl()).into(holder.eventImage);

        holder.itemView.setOnClickListener(v -> {
            // Use Context to navigate to EventDetailsFragment with event ID
            if (context instanceof FragmentActivity) {
                EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventId", event.getId()); // Make sure your Event object has a getId() method.
                eventDetailsFragment.setArguments(args);

                FragmentActivity activity = (FragmentActivity) context;
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, eventDetailsFragment) // Make sure R.id.content is the correct ID of your container.
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventDataList.size();
    }

    public void updateEvents(List<Event> events) {
        this.eventDataList.clear();
        this.eventDataList.addAll(events);
        notifyDataSetChanged();
    }
}