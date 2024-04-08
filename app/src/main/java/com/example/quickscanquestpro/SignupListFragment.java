package com.example.quickscanquestpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment displays a list of users that have signed up for a specific event.
 * It allows a user to view the list of attendees.
 */
public class SignupListFragment extends Fragment {

    private DatabaseService databaseService;
    private String eventId;

    /**
     * calls the Fragment constructor and gets the event Id of the event passed to the constructor
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseService = new DatabaseService();
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    /**
     * This method creates the user interface by getting the xml file
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_list, container, false);
    }

    /**
     * This method calls getEventsSignUps in database service which gets a list of users that have signed up to that event.
     * It then passes that list to an adapter to update the UI.
     * It also implements the back button
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.signup_list);

        //back button
        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

        databaseService.getEventSignUps(eventId, new DatabaseService.OnEventSignUpsLoaded() {
            @Override
            public void onSignUpsLoaded(List<User> users) {
                // Update your ListView adapter with the fetched users
                SignupListAdapter adapter = new SignupListAdapter(getActivity(), R.layout.list_signups_view, users);
                listView.setAdapter(adapter);
            }
        });
    }
}

