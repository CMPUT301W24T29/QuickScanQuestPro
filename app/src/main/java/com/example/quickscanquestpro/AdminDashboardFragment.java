package com.example.quickscanquestpro;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quickscanquestpro.EventDashboardFragment;

public class AdminDashboardFragment extends Fragment {

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
    }
}
