package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.zxing.MultiFormatWriter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventCreationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventCreationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventCreationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventCreationFragment newInstance(String param1, String param2) {
        EventCreationFragment fragment = new EventCreationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button createButton = view.findViewById(R.id.create_event_confirm_button);
        createButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) this.getActivity();
            mainActivity.setTestEvent(new Event(mainActivity.getNewEventID()));

            // set active fragment to the event dashboard again
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.title_dashboard));
            fragmentTransaction.commit();
        });
    }
}