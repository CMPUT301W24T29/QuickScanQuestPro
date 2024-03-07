package com.example.quickscanquestpro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
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
    private String newEventID = UUID.randomUUID().toString();
    private Event testEvent;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String USER_ID_KEY = "userId";

    private User user;

    private User testUser;

    private DatabaseService databaseService = new DatabaseService();

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        // Initiate user
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getString(USER_ID_KEY, null);
        databaseService.getUsers(new DatabaseService.OnUsersDataLoaded() {
            boolean userExists = false;
            @Override
            public void onUsersLoaded(List<User> users) {
                // Handle the list of users
                for (User user : users) {
                    if (user.getUserId().equals(userId)) {
                        userExists = true;
                    }
                }
                if (userExists) {
                    existingUser(userId);
                } else {
                    userId = UUID.randomUUID().toString();
                    prefs.edit().putString(USER_ID_KEY, userId).apply();
                    newUser(userId);
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
                Log.e("MainActivity", "Error loading users: " + e.getMessage());
            }

        });

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
                if (testUser.isAdmin()){
                    fragment1 = new AdminDashboardFragment();
                }
                else{
                    fragment1 = new ProfileFragment();
                }

            } else {
                // default to qr code home view
                fragment1 = new HomeViewFragment();
            }

            // actually display the fragment, using a tag with the same name as the button that was pressed
            this.transitionFragment(fragment1, pressedTitle);

            return true;
        });



        // josephs code ----

////         If UserID not found then create a new one and add to firebase
//        if (userId == null) {
//            userId = UUID.randomUUID().toString();
//            prefs.edit().putString(USER_ID_KEY, userId).apply();
//
//            newUser(userId);
//        } else {
//            // UserID exists, proceed with existing UserID
//            // Optionally, you can verify or update this user's details in Firestore
//            existingUser(userId);
//        }

        // josephs code ----



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

    public String getNewEventID() {
        newEventID = UUID.randomUUID().toString();

        return newEventID;
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

    /**
     * This method called to create a new user if it doesn't already exist in the database
     * It takes the User Id and creates a new user in the database
     * @param userId a String representing the User ID
     */
    private void newUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a Map or a custom object
        Map<String, Object> user = new HashMap<>();
        user.put("exists", "i think so"); // Just a simple flag, you can add more user details here
        user.put("admin", false);

        // Add a new document with the generated userId
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "New User", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Potential failure stuff
                });

        databaseService.getSpecificUser(userId, new DatabaseService.OnUserDataLoaded() {
            @Override
            public void onUserLoaded(User user) {
                testUser = new User(userId);
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error loading user: " + e.getMessage());
            }
        });
    }

    /**
     * This method calls the constructor to create a the user object if the user already
     * exists in the database.
     * @param userId A string for userId to pass to the constructor
     */
    private void existingUser(String userId) {
        this.user = new User(userId);
        databaseService.getSpecificUser(userId, new DatabaseService.OnUserDataLoaded() {
            @Override
            public void onUserLoaded(User user) {
                testUser = user;
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error loading user: " + e.getMessage());
            }
        });
        Toast.makeText(getApplicationContext(), "Welcome Back!", Toast.LENGTH_SHORT).show();

    }

    /**
     * Getter for the User object
     * @return User Object
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter for the User object
     * @param user Takes a user object to set
     */
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
