package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickscanquestpro.R;
import com.example.quickscanquestpro.User;

import java.util.List;
public class AdminEventImageAdapter extends RecyclerView.Adapter<AdminEventImageAdapter.ViewHolder> {

    private List<Event> eventList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClicked(String eventId, String photoUrl);
    }

    public AdminEventImageAdapter(Context context, List<Event> eventList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.itemName.setText(event.getTitle());
        Glide.with(context).load(event.getEventBannerUrl()).placeholder(R.drawable.default_event_profile).into(holder.miniPhoto);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClicked(event.getId(), event.getEventBannerUrl());
            }
        });

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

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
