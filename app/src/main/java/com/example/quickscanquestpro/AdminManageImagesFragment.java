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

/**
 * A Fragment subclass for managing images within the admin panel.
 * This class facilitates the display and management of user and event images,
 * including the functionality to view and delete images.
 */

public class AdminManageImagesFragment extends Fragment {

    private RecyclerView usersRecyclerView, eventsRecyclerView;
    private AdminUserImageAdapter userAdapter;
    private AdminEventImageAdapter eventAdapter;

    // Instantiate your DatabaseService
    private DatabaseService databaseService;

    private List<User> usersList = new ArrayList<>();
    private List<Event> eventsList = new ArrayList<>();

    public AdminManageImagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_image_manage, container, false);
    }

    /**
     * Setup for RecyclerViews and data fetching after the view has been created.
     * Initializes RecyclerViews for users and events, sets their layout managers and adapters,
     * and triggers the data fetching process.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseService = new DatabaseService();

        usersRecyclerView = view.findViewById(R.id.profiles_recycler_view);
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);

        setupRecyclerView(usersRecyclerView, eventsRecyclerView);

        fetchData();

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        getParentFragmentManager().setFragmentResultListener("updatePhotoResult", getViewLifecycleOwner(), (requestKey, result) -> {
            String photoId = result.getString("photoId");
            String photoType = result.getString("photoType");
            boolean isDeleted = result.getBoolean("isDeleted", false);

            // Check if the photo deletion was successful and update the UI accordingly
            if (isDeleted) {
                if ("user".equals(photoType)) {
                    // Update user photo to null and refresh adapter
                    for (User user : usersList) {
                        if (user.getUserId().equals(photoId)) {
                                user.setProfilePictureUrl(null);
                            break;
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                } else if ("event".equals(photoType)) {
                    for (Event event : eventsList) {
                        if (event.getId().equals(photoId)) {
                            event.setEventBannerUrl(null);
                            break;
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Configures RecyclerViews for displaying users and events.
     * Sets the layout manager and attaches the adapter for each RecyclerView.
     *
     * @param usersRecyclerView RecyclerView for displaying user images.
     * @param eventsRecyclerView RecyclerView for displaying event images.
     */

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
                        .replace(R.id.main_layout, eventFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        usersRecyclerView.setAdapter(userAdapter);
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    /**
     * Fetches user and event data from the database and updates the UI.
     * Initiates asynchronous calls to fetch user and event lists, then updates the respective adapters
     * upon successful data retrieval.
     */

    private void fetchData() {
        // Fetch users
        databaseService.getUsers(new DatabaseService.OnUsersDataLoaded() {
            @Override
            public void onUsersLoaded(List<User> users) {
                if (users != null) {
                    usersList.clear();
                    usersList.addAll(users);
                    userAdapter.notifyDataSetChanged();
                }
            }
        });

        databaseService.getEvents(new DatabaseService.OnEventsDataLoaded() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (events != null) {
                    eventsList.clear();
                    eventsList.addAll(events);
                    eventAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
