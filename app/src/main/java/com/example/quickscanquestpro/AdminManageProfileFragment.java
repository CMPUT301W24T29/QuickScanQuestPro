package com.example.quickscanquestpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Fragment} subclass that provides functionality for managing user profiles within the application.
 * It allows administrators to view and interact with a list of user profiles, offering options such as
 * reviewing profile details, and potentially editing or deleting profiles directly from the list.
 */
public class AdminManageProfileFragment extends Fragment {
    private RecyclerView profileRecyclerView;
    private AdminProfileAdapter profileAdapter;
    private DatabaseService databaseService;
    private List<User> usersList = new ArrayList<>();

    /**
     * Default constructor for the fragment.
     */
    public AdminManageProfileFragment() {
        // Required empty public constructor
    }


    /**
     * Factory method to create a new instance of this fragment.
     * @return A new instance of fragment AdminManageProfileFragment.
     */
    public static AdminManageProfileFragment newInstance() {
        return new AdminManageProfileFragment();
    }


    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the
     *                  LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     *                           as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile_manage, container, false);
    }



    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before
     * any saved state has been restored into the view
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     *                           as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseService = new DatabaseService();
        profileRecyclerView = view.findViewById(R.id.admin_profile_dashboard_list);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupRecyclerView();

        fetchData();

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        profileAdapter = new AdminProfileAdapter(getContext(), usersList, true);
        profileRecyclerView.setAdapter(profileAdapter);
    }

    private void fetchData() {
        databaseService.getUsers(new DatabaseService.OnUsersDataLoaded() {
            @Override
            public void onUsersLoaded(List<User> users) {
                if (getActivity() == null) {
                    Log.e("AdminProfileFragment", "Activity is null. Skipping setup.");
                    return;
                }

                if (users.isEmpty()) {
                    Toast.makeText(getActivity(), "No users found!", Toast.LENGTH_SHORT).show();
                } else {
                    usersList.clear();
                    usersList.addAll(users);
                    profileAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
