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

/**
 * A {@link Fragment} subclass that provides functionality for managing user profiles within the application.
 * It allows administrators to view and interact with a list of user profiles, offering options such as
 * reviewing profile details, and potentially editing or deleting profiles directly from the list.
 */
public class AdminManageProfileFragment extends Fragment {
    private DatabaseService databaseService;
    // Assuming MainActivity is properly handling context for Toasts if needed.

    public AdminManageProfileFragment() {
        // Required empty public constructor
    }
    /**
     * Creates a new instance of {@link AdminManageProfileFragment}.
     * This can be used to create instances of this fragment with any required initialization parameters.
     *
     * @return A new instance of fragment AdminManageProfileFragment.
     */
    public static AdminManageProfileFragment newInstance() {
        return new AdminManageProfileFragment();
    }
    /**
     * Inflates the fragment layout and initializes fragment view components.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Returns the View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize DatabaseService here or in onViewCreated depending on when you need it.
        return inflater.inflate(R.layout.fragment_admin_profile_manage, container, false);
    }
    /**
     * Once the view is created, this method is called to set up the ListView with the adapter
     * and fetch the list of user profiles from the database. It also initializes the DatabaseService
     * and sets a click listener on the back button to allow navigation back to the previous screen.
     *
     * @param view               The View returned by onCreateView method.
     * @param savedInstanceState If non-null, the fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseService = new DatabaseService(); // Initialize your DatabaseService
        ListView profileListView = view.findViewById(R.id.admin_profile_dashboard_list);

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

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
                    AdminProfileAdapter adapter = new AdminProfileAdapter(getActivity(), R.layout.list_admin_view, users);
                    profileListView.setAdapter(adapter);
                }
            }
        });
    }
}
