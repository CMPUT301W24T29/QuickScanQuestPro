package com.example.quickscanquestpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class AdminManageProfileFragment extends Fragment {
    private DatabaseService databaseService;
    // Assuming MainActivity is properly handling context for Toasts if needed.

    public AdminManageProfileFragment() {
        // Required empty public constructor
    }

    public static AdminManageProfileFragment newInstance() {
        return new AdminManageProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize DatabaseService here or in onViewCreated depending on when you need it.
        return inflater.inflate(R.layout.fragment_admin_profile_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView profileListView = view.findViewById(R.id.profile_dashboard_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Fetch users from Firestore and update the ListView
        databaseService.listenForUsersUpdates(new DatabaseService.OnUsersDataLoaded() {
            @Override
            public void onUsersLoaded(List<User> users) {
                if (getActivity() == null) {
                    Log.e("AdminProfileFragment", "Activity is null. Skipping setup.");
                    return;
                }

                if (users.isEmpty()) {
                    Toast.makeText(getActivity(), "No users found!", Toast.LENGTH_SHORT).show();
                } else {
                    // Use AdminProfileAdapter to display the users in the ListView
                    AdminProfileAdapter adapter = new AdminProfileAdapter(getActivity(), R.layout.list_profile_admin_view, users);
                    profileListView.setAdapter(adapter);
                }
            }
        });
    }
}
