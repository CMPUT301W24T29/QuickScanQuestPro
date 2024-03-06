package com.example.quickscanquestpro;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdminDashboardFragment extends Fragment {

    public AdminDashboardFragment() {
        // Required empty public constructor
    }

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }
}
