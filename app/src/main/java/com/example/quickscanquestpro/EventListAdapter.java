package com.example.quickscanquestpro;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventDataList;
    private DatabaseService databaseService = new DatabaseService();

    public EventListAdapter(Context context, List<Event> eventDataList) {
        this.context = context;
        this.eventDataList = eventDataList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventStartTime, eventLocation, eventStartDate, eventEndTime;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventStartTime = itemView.findViewById(R.id.event_start_time); // Correctly initialized
            eventLocation = itemView.findViewById(R.id.event_location);
            eventStartDate = itemView.findViewById(R.id.event_start_date);
            eventEndTime = itemView.findViewById(R.id.event_end_time);
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
        holder.eventStartDate.setText(event.getStartDate().toString());
        holder.eventStartTime.setText(event.getStartTime().toString());
        holder.eventEndTime.setText(event.getEndTime().toString());


        String imageUrl = event.getEventBannerUrl(); // Ensure you have a method in your Event class to get the image URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.color.white) // Placeholder color or drawable
                    .fitCenter()
                    .into(holder.eventImage);
        } else {
            holder.eventImage.setImageResource(R.drawable.ic_launcher_background);
        }



        holder.itemView.setOnClickListener(v -> {
            // Check if context is an instance of FragmentActivity to ensure proper casting
            if (context instanceof FragmentActivity) {
                databaseService.getEvent(event.getId(), event1 -> {
                    if (event1 != null) {
                        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment(event1);

                        // Get the FragmentManager from the activity
                        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                        // Start a new FragmentTransaction
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        // Replace the current fragment with the eventDetailsFragment
                        fragmentTransaction.replace(R.id.content, eventDetailsFragment);

                        // Add the transaction to the back stack (optional)
                        fragmentTransaction.addToBackStack(null);

                        // Commit the transaction
                        fragmentTransaction.commit();
                    } else {
                        Log.e("EventListAdapter", "Event is null. Cannot display details.");
                    }
                });
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