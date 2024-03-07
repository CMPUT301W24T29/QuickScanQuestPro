package com.example.quickscanquestpro;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quickscanquestpro.AdminProfileAdapter;
import com.example.quickscanquestpro.DatabaseService;

import java.util.List;

public class AdminManageProfileFragment extends Fragment {
    private DatabaseService databaseService;

    public AdminManageProfileFragment() {
        // Required empty public constructor
    }

    public static AdminManageProfileFragment newInstance() {
        return new AdminManageProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        databaseService = new DatabaseService(); // Initialize here or in onViewCreated
        return inflater.inflate(R.layout.fragment_admin_profile_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView profileListView = view.findViewById(R.id.profile_dashboard_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Fetch users from Firestore and update the ListView
        databaseService.getUsers(new DatabaseService.OnUsersDataLoaded() {
            @Override
            public void onUsersLoaded(List<User> users) {
                if (getActivity() != null) {
                    AdminProfileAdapter adapter = new AdminProfileAdapter(getActivity(), R.layout.list_profile_admin_view, users);
                    profileListView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
