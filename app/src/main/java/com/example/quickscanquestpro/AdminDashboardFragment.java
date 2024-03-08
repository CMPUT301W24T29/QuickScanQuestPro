package com.example.quickscanquestpro;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quickscanquestpro.EventDashboardFragment;

public class AdminDashboardFragment extends Fragment {



    //User user;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters


    public static AdminDashboardFragment newInstance(String param1, String param2) {
        AdminDashboardFragment fragment = new AdminDashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AdminDashboardFragment() {
        // Required empty public constructor
    }

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
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
        Button manageEventsButton = view.findViewById(R.id.button_manage_events);
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

        Button manageProfileButton = view.findViewById(R.id.button_manage_users);
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
                ProfileFragment profileFragment = new ProfileFragment();// Use correct parameters or modify newInstance accordingly

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
