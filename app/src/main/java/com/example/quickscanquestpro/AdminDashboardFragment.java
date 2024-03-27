package com.example.quickscanquestpro;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A {@link Fragment} subclass that represents the admin dashboard within the application.
 * It provides the admin user with various management options including managing events,
 * managing user profiles, and viewing profiles. This serves as a central hub for
 * administrative tasks.
 */

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

    /**
     * Sets up the interaction for all buttons within the fragment after the view is created.
     * This includes setting click listeners for managing events, managing user profiles, and
     * viewing profiles, providing navigation to the respective fragments for each action.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, the fragment is being re-constructed from a previous saved state.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the "Manage Events" button and set up the click listener
        Button manageEventsButton = view.findViewById(R.id.admin_button_manage_events);
        manageEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminManageEventsFragment adminManageEventsFragment = new AdminManageEventsFragment();

                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, adminManageEventsFragment)
                            .addToBackStack(null)
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
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        Button viewImagesButton = view.findViewById(R.id.admin_button_view_images);

        viewImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminManageImagesFragment adminManageImagesFragment = new AdminManageImagesFragment();

                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, adminManageImagesFragment)
                            .addToBackStack(null)
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
                            .replace(R.id.content, profileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });


    }
}
