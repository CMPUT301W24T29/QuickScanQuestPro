package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdminManageEventsFragment extends Fragment {

    public AdminManageEventsFragment() {
        // Required empty public constructor
    }

    public static AdminManageEventsFragment newInstance() {
        return new AdminManageEventsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            // Check if there are entries in the back stack
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                // If there are, pop the back stack to go to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });
    }

}


