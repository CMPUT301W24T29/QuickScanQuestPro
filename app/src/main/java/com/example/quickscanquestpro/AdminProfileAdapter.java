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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class AdminProfileAdapter extends ArrayAdapter<User> {
    private int resourceLayout;
    private Context mContext;

    private DatabaseService databaseService = new DatabaseService();



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
                databaseService.deleteUser(getItem(position));
                remove(getItem(position)); // Remove the user from the adapter
                notifyDataSetChanged(); // Refresh the adapter
            });

            textView.setOnClickListener(view -> {
                ProfileFragment profileFragment = ProfileFragment.newInstance(user.getUserId(), user.getName());
                FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content, profileFragment); // Make sure R.id.content is the ID of your fragment container
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();



            });


        }

        return convertView;
    }
}
