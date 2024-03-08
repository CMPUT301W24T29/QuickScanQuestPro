package com.example.quickscanquestpro;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminDashboardFragment extends Fragment {

    public AdminDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the "Manage Events" button and set up the click listener
        Button manageEventsButton = view.findViewById(R.id.admin_button_manage_events);
        manageEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an instance of EventDashboardFragment
                AdminManageEventsFragment adminManageEventsFragment = new AdminManageEventsFragment();

                // Use FragmentManager to replace the AdminDashboardFragment with EventDashboardFragment
                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, adminManageEventsFragment)
                            .addToBackStack(null)  // Optional, if you want to navigate back to the admin dashboard
                            .commit();
                }
            }
        });

        Button manageProfileButton = view.findViewById(R.id.admin_button_manage_users);
        manageProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminManageProfileFragment adminManageProfileFragment = new AdminManageProfileFragment();

                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, adminManageProfileFragment)
                            .addToBackStack(null)  // Optional, if you want to navigate back to the admin dashboard
                            .commit();
                }
            }
        });

        Button viewProfileButton = view.findViewById(R.id.button_profile);
        viewProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ProfileFragment
                ProfileFragment profileFragment = new ProfileFragment();

                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, profileFragment) // Make sure R.id.content is the ID of your fragment container
                            .addToBackStack(null) // Add transaction to the back stack for proper navigation back
                            .commit();
                }
            }
        });



    }
}
