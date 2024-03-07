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

public class AdminEventAdapter extends ArrayAdapter<Event> implements DatabaseService.OnEventDataLoaded{
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

        Event event = getItem(position);
        if (event != null) {
            TextView textView = convertView.findViewById(R.id.profile_name_text_view);
            textView.setText(event.getTitle());
            Button deleteButton = convertView.findViewById(R.id.admin_delete_button);
            deleteButton.setOnClickListener(view -> {
                databaseService.deleteEvent(getItem(position));
                remove(getItem(position)); // Remove the user from the adapter
                notifyDataSetChanged(); // Refresh the adapter
            });
            textView.setOnClickListener(view -> {
                databaseService.getEvent(getItem(position).getId(), this);
            });
        }

        return convertView;
    }

    @Override
    public void onEventLoaded(Event event) {
        if (event != null) {
            EventDetailsFragment eventDetailsFragment = new EventDetailsFragment(event);

            // Get the FragmentManager from the activity
            FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();

            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Replace the current fragment with the eventDetailsFragment
            fragmentTransaction.replace(R.id.content, eventDetailsFragment);

            // Add the transaction to the back stack (optional)
            fragmentTransaction.addToBackStack(null);

            // Commit the transaction
            fragmentTransaction.commit();
        } else {
            Log.e("AdminEventAdapter", "Event is null. Cannot display details.");
                // open the event details fragment
            }
        }
    }
