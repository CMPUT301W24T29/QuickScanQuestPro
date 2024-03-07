package com.example.quickscanquestpro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdminEventAdapter extends ArrayAdapter<Event>{
    private int resourceLayout;
    private Context mContext;

    private DatabaseService databaseService = new DatabaseService();

    public AdminEventAdapter(@NonNull Context context, int resource, List<Event> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        convertView.setOnClickListener(v -> {
            Log.d("AdapterItemClick", "Item clicked at position: " + position);
            // Handle click event as needed
        });
        Event event = getItem(position);
        if (event != null) {
            TextView textView = convertView.findViewById(R.id.profile_name_text_view);
            textView.setText(event.getTitle());
            Button deleteButton = convertView.findViewById(R.id.delete_profile_button);
            deleteButton.setOnClickListener(view -> {

                databaseService.deleteEvent(getItem(position));
                remove(getItem(position)); // Remove the user from the adapter
                notifyDataSetChanged(); // Refresh the adapter
            });
        }

        return convertView;
    }
}
