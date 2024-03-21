package com.example.quickscanquestpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickscanquestpro.AdminUserImageAdapter;
import com.example.quickscanquestpro.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class AdminManageImagesFragment extends Fragment {

    private RecyclerView usersRecyclerView, eventsRecyclerView;
    private AdminUserImageAdapter userAdapter;
    private AdminEventImageAdapter eventAdapter;

    // Instantiate your DatabaseService
    private DatabaseService databaseService;

    private List<User> usersList = new ArrayList<>();
    private List<Event> eventsList = new ArrayList<>();

    public AdminManageImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_image_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseService = new DatabaseService(); // Initialize your DatabaseService

        usersRecyclerView = view.findViewById(R.id.profiles_recycler_view);
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);

        setupRecyclerView(usersRecyclerView, eventsRecyclerView);

        fetchData();

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });


    }

    private void setupRecyclerView(RecyclerView usersRecyclerView, RecyclerView eventsRecyclerView) {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new AdminUserImageAdapter(getContext(), usersList, user -> {
            EnlargedPhotoFragment fragment = EnlargedPhotoFragment.newInstance(user.getUserId(), user.getProfilePictureUrl(), "user");
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_layout, fragment)
                    .addToBackStack(null)
                    .commit();

        });
        eventAdapter = new AdminEventImageAdapter(getContext(), eventsList, (eventId, photoUrl) -> {
            EnlargedPhotoFragment eventFragment = EnlargedPhotoFragment.newInstance(eventId, photoUrl, "event");
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, eventFragment) // Ensure you use the correct container ID
                        .addToBackStack(null)
                        .commit();
            }
        });

        usersRecyclerView.setAdapter(userAdapter);
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    private void fetchData() {
        // Fetch users
        databaseService.getUsers(new DatabaseService.OnUsersDataLoaded() {
            @Override
            public void onUsersLoaded(List<User> users) {
                if (users != null) {
                    usersList.clear();
                    usersList.addAll(users);
                    userAdapter.notifyDataSetChanged(); // This refreshes the user RecyclerView
                }
            }
        });

        databaseService.getEvents(new DatabaseService.OnEventsDataLoaded() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (events != null) {
                    eventsList.clear();
                    eventsList.addAll(events);
                    eventAdapter.notifyDataSetChanged(); // This refreshes the event RecyclerView
                }
            }
        });
    }
}
