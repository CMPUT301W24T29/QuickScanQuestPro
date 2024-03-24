package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickscanquestpro.R;
import com.example.quickscanquestpro.User;

/**
 * Adapter class for managing event images in an admin context.
 * This adapter is responsible for binding event data to views that are displayed within a RecyclerView.
 * It also handles item click events, allowing for custom behavior when an event image is interacted with.
 */


import java.util.List;
public class AdminEventImageAdapter extends RecyclerView.Adapter<AdminEventImageAdapter.ViewHolder> {

    private List<Event> eventList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClicked(String eventId, String photoUrl);
    }

    /**
     * Constructs a new AdminEventImageAdapter.
     *
     * @param context The current context.
     * @param eventList A list of events to be displayed.
     * @param onItemClickListener Listener for item click events.
     */

    public AdminEventImageAdapter(Context context, List<Event> eventList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_admin, parent, false);
        return new ViewHolder(view);
    }


    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link ViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.itemName.setText(event.getTitle());
        Glide.with(context)
                .load(event.getEventBannerUrl() != null ? event.getEventBannerUrl() : ContextCompat.getDrawable(context, R.drawable.default_event_profile))
                .placeholder(R.drawable.default_event_profile)
                .into(holder.miniPhoto);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClicked(event.getId(), event.getEventBannerUrl());
            }
        });

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        ImageView miniPhoto;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            miniPhoto = itemView.findViewById(R.id.mini_photo);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
