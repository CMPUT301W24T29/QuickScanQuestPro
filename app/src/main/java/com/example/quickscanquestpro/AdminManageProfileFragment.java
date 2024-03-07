package com.example.quickscanquestpro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminManageProfileFragment extends Fragment {

    public AdminManageProfileFragment() {
        // Required empty public constructor
    }

    public static AdminManageProfileFragment newInstance() {
        return new AdminManageProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        ListView profileListView = view.findViewById(R.id.profile_dashboard_list);

        // Create some fake data for demonstration
        ArrayList<String> fakeProfiles = new ArrayList<>(Arrays.asList("Profile 1", "Profile 2", "Profile 3", "Profile 4", "Profile 5"));

        // Use the custom adapter
        AdminProfileAdapter adapter = new AdminProfileAdapter(getContext(), R.layout.list_profile_admin_view, fakeProfiles);

        profileListView.setAdapter(adapter);
    }
}
