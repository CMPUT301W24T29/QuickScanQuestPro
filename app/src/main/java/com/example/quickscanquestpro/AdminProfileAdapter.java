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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;


/**
 * An ArrayAdapter designed for rendering a list of {@link User} objects within an admin interface.
 * This adapter is responsible for converting each {@link User} object into a View that can be inserted into
 * a ListView. It provides functionality for deleting a user and navigating to the user's profile
 * details.
 */

public class AdminProfileAdapter extends ArrayAdapter<User> {
    private int resourceLayout;
    private Context mContext;

    private DatabaseService databaseService = new DatabaseService();

    /**
     * Constructs a new {@link AdminProfileAdapter}.
     *
     * @param context   The current context.
     * @param resource  The resource ID for a layout file containing a TextView to use when
     *                  instantiating views.
     * @param items     The list of {@link User} objects to represent in the ListView.
     */
    public AdminProfileAdapter(@NonNull Context context, int resource, List<User> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }


    /**
     *
     * @param position     The position of the item within the adapter's data set of the item whose view
     *                     we want.
     * @param convertView  The old view to reuse
     * @param parent       The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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
            Button deleteButton = convertView.findViewById(R.id.admin_delete_button);
            deleteButton.setOnClickListener(view -> {
                databaseService.deleteUser(getItem(position));
                remove(getItem(position)); // Remove the user from the adapter
                notifyDataSetChanged(); // Refresh the adapter
            });

            textView.setOnClickListener(view -> {
                ProfileFragment profileFragment = new ProfileFragment(user);
                FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content, profileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });
        }

        return convertView;
    }
}
