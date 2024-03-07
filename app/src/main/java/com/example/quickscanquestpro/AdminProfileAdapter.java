package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdminProfileAdapter extends ArrayAdapter<String> {

    private int resourceLayout;
    private Context mContext;
    private List<String> items;

    public AdminProfileAdapter(@NonNull Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        this.items = items;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        String profile = getItem(position);

        TextView textView = listItem.findViewById(R.id.profile_name_text_view);
        textView.setText(profile);

        Button deleteButton = listItem.findViewById(R.id.delete_profile_button);
        deleteButton.setOnClickListener(view -> {
            // Here, implement the deletion logic
            items.remove(position);
            notifyDataSetChanged(); // Notify the adapter to refresh the view
        });

        return listItem;
    }
}
