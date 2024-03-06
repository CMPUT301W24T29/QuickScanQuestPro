package com.example.quickscanquestpro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventAttendeeFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */

public class EventAttendeeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public EventAttendeeFragment()
    {
        // Required empty public constructor
    }

    public static EventAttendeeFragment newInstance(String param1, String param2) {
        EventAttendeeFragment fragment = new EventAttendeeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    // onCreate method from EventAttendeeFragment.java
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_attendee_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<Attendee> eventAttendees = new ArrayList<>();
        eventAttendees.add(new Attendee("Joshua", 1));
        eventAttendees.add(new Attendee("John", 1));
        // Get the list view from the layout
        ListView listView = view.findViewById(R.id.event_attendee_list);
        // display the items in eventAttendees
        ArrayAdapter<Attendee> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, eventAttendees);
        listView.setAdapter(adapter);

        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.events_list_title));
            fragmentTransaction.commit();
        });

        int currentLiveCount = eventAttendees.size();
        // update the live count according to the number of attendees
        TextView liveCount = view.findViewById(R.id.live_count_number);
        liveCount.setText(String.valueOf(eventAttendees.size()));

    }

}
