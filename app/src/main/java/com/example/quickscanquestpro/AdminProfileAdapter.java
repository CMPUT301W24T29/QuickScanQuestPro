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

public class AdminProfileAdapter extends ArrayAdapter<User> {
    private int resourceLayout;
    private Context mContext;

    public AdminProfileAdapter(@NonNull Context context, int resource, List<User> items) {
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

        User user = getItem(position);
        if (user != null) {
            TextView textView = convertView.findViewById(R.id.profile_name_text_view);
            textView.setText(user.getName());
            Button deleteButton = convertView.findViewById(R.id.delete_profile_button);
            deleteButton.setOnClickListener(view -> {
                // Implement user deletion here if desired
            });
        }

        return convertView;
    }
}
