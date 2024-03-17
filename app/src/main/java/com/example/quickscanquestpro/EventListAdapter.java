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

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventDataList.get(position);

        // Truncate title and location if longer than 30 characters
        String title = event.getTitle().length() > 30 ? event.getTitle().substring(0, 27) + "..." : event.getTitle();
        String location = event.getLocation().length() > 30 ? event.getLocation().substring(0, 27) + "..." : event.getLocation();

        // Format the dates and times
        String dates = event.getStartDate().toString() + " - " + event.getEndDate().toString();
        String times = event.getStartTime().toString() + " - " + event.getEndTime().toString();

        holder.eventTitle.setText(title);
        holder.eventLocation.setText(location);
        holder.eventDates.setText(dates);
        holder.eventTimes.setText(times);


        String imageUrl = event.getEventBannerUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.color.white)
                    .fitCenter()
                    .into(holder.eventImage);
        } else {
            holder.eventImage.setImageResource(R.drawable.ic_launcher_background);
        }



        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                databaseService.getEvent(event.getId(), event1 -> {
                    if (event1 != null) {
                        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment(event1);

                        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        fragmentTransaction.replace(R.id.content, eventDetailsFragment);

                        fragmentTransaction.addToBackStack(null);

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