package com.example.quickscanquestpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // display the main page / qr code reader fragment when the app starts
        HomeViewFragment fragment = new HomeViewFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.title_qr_scanner));
        fragmentTransaction.commit();

        NavigationBarView navBarView = findViewById(R.id.bottom_navigation);
        // sets the default selected item for the main activity to the qrscanner button
        navBarView.setSelectedItemId(R.id.navigation_qr_scanner);
        // adds functions to the navbar button
        navBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.i("NavMenu", "navButtonPressed: title is " + item.getTitle());
                String pressedTitle = (String) item.getTitle();

                // gets the fragment currently loaded into the content view
                Fragment callerFragment = getSupportFragmentManager().findFragmentById(R.id.content);
                // gets the tag supplied to the fragment when displayed, which is the title of the button that opens it
                String caller = callerFragment.getTag();

                // gets the string resources for all the buttons
                String dashboardTitle = callerFragment.getString(R.string.title_dashboard);
                String qrTitle = callerFragment.getString(R.string.title_qr_scanner);
                String profileTitle = callerFragment.getString(R.string.title_profile);

                // if the button clicked is the same as the currently displayed fragment, do nothing!
                if (Objects.equals(caller, pressedTitle)) {
                    Log.i("NavMenu", "ignoring press on " + item.getTitle() + " because it was already active");
                    return false;
                }

                Fragment fragment;
                if (Objects.equals(pressedTitle, dashboardTitle)) {
                    fragment = new EventDashboardFragment();
                } else if (Objects.equals(pressedTitle, profileTitle)) {
                    fragment = new ProfileFragment();
                } else {
                    // default to qr code home view
                    fragment = new HomeViewFragment();
                }

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, pressedTitle);
                fragmentTransaction.commit();
                return true;
            }
        });

    }

}