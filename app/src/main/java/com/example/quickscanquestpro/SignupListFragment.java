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

public class SignupListFragment extends Fragment {

    private DatabaseService databaseService;
    private String eventId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseService = new DatabaseService();
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_list, container, false);
    }

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

