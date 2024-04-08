package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AttendeesHeatmapFragment extends Fragment {

    public AttendeesHeatmapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendees_heatmap, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

    }
}