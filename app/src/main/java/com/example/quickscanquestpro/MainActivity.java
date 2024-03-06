package com.example.quickscanquestpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Main activity for the app, initializes DatabaseService on startup,
 * checks/creates new UUID for User when app is started without one and stores reference to user for other fragments etc.
 * Runs for full duration of app and allows for semi-persistence.
 * Holds Navbar and starts with displaying QR scanner, used by other fragments to display in.
 */
public class MainActivity extends AppCompatActivity {

    private QRCodeScanner qrCodeScanner;
    private int newEventID = 0;
    private Event testEvent;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String USER_ID_KEY = "userId";

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // display the main page / qr code reader fragment when the app starts
        this.transitionFragment(new HomeViewFragment(), this.getString(R.string.title_qr_scanner));

        NavigationBarView navBarView = findViewById(R.id.bottom_navigation);
        // sets the default selected item for the main activity to the qrscanner button
        navBarView.setSelectedItemId(R.id.navigation_qr_scanner);
        // adds functions to the navbar button
        navBarView.setOnItemSelectedListener(item -> {
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

            // create fragment of the type selected
            Fragment fragment1;
            if (Objects.equals(pressedTitle, dashboardTitle)) {
                fragment1 = new EventDashboardFragment();
            } else if (Objects.equals(pressedTitle, profileTitle)) {
                fragment1 = new ProfileFragment();
            } else {
                // default to qr code home view
                fragment1 = new HomeViewFragment();
            }

            // actually display the fragment, using a tag with the same name as the button that was pressed
            this.transitionFragment(fragment1, pressedTitle);

            return true;
        });

        // Initiate user
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString(USER_ID_KEY, null);

        // If UserID not found then create a new one and add to firebase
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            prefs.edit().putString(USER_ID_KEY, userId).apply();

            addUserToFirestore(userId);
        } else {
            // UserID exists, proceed with existing UserID
            // Optionally, you can verify or update this user's details in Firestore
            existingUser(userId);
        }

        //Toast.makeText(getApplicationContext(), userId, Toast.LENGTH_SHORT).show();
        //user.saveToFirestore();


        /*
        String userId = FirebaseFirestore.getInstance().collection("users").document().getId();

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserID", userId);
        editor.apply();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        user.put("email", "john.doe@example.com");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //String userId = sharedPreferences.getString("UserID", null);
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> Log.d("TAG", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));


         */
    }

    public int getNewEventID() {
        return this.newEventID++;
    }

    public void setTestEvent(Event event) {
        this.testEvent = event;
    }

    public Event getTestEvent() {
        if (this.testEvent == null) {
            setTestEvent(Event.createTestEvent(getNewEventID()));
        }
        return this.testEvent;
    }

    private void addUserToFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a Map or a custom object
        Map<String, Object> user = new HashMap<>();
        user.put("exists", true); // Just a simple flag, you can add more user details here

        // Add a new document with the generated userId
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "New User", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Potential failure stuff
                });
    }

    //user constructor
    private void existingUser(String userId) {
        this.user = new User(userId);
        Toast.makeText(getApplicationContext(), "Welcome Back!", Toast.LENGTH_SHORT).show();

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * transitions the main fragment display (content) to the specified fragment with the given tag
     * @param fragment fragment to move to
     * @param tag internal tag that the app uses to know which fragment is open
     */
    public void transitionFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, tag);
        fragmentTransaction.commit();
    }

}