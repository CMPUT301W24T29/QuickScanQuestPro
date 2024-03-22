package com.example.quickscanquestpro;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;

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
public class MainActivity extends AppCompatActivity implements DatabaseService.OnUsersDataLoaded, DatabaseService.OnUserDataLoaded {
    // TODO: remove test event stuff before finishing project
    private String newEventID = UUID.randomUUID().toString();
    private Event testEvent;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String USER_ID_KEY = "userId";

    private User user;

    private Event event;
    private String userId;
    private List<User> usersList;

    private DatabaseService databaseService = new DatabaseService();

//    private FirebaseMessageReceiver firebaseMessageReceiver = new FirebaseMessageReceiver();

    private String notificationToken;
    private Boolean foundUser = false;

    private Boolean foundUserList = false;

    private SharedPreferences prefs;

    private NavigationBarView navBarView;

    private MenuItem item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        // Initiate user
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getString(USER_ID_KEY, null);
        databaseService.getUsers(this);

        this.transitionFragment(new HomeViewFragment(), this.getString(R.string.title_qr_scanner));

        navBarView = findViewById(R.id.bottom_navigation);
        // sets the default selected item for the main activity to the qrscanner button
        navBarView.setSelectedItemId(R.id.navigation_qr_scanner);
        // adds functions to the navbar button


        navBarView.setOnItemSelectedListener(item -> {
            this.item = item;
            databaseService.getSpecificUserDetails(userId, this);
            return true;
        });


    }

    // TODO: remove test event stuff before finishing project
    public String getNewEventID() {
        newEventID = UUID.randomUUID().toString();
        return newEventID;
    }

    // TODO: remove test event stuff before finishing project
    public void setTestEvent(Event event) {
        this.testEvent = event;
    }

    // TODO: remove test event stuff before finishing project
//    public Event getTestEvent() {
//        if (this.testEvent == null) {
//            setTestEvent(Event.createTestEvent(getNewEventID()));
//        }
//        return this.testEvent;
//    }

    /**
     * This method called to create a new user if it doesn't already exist in the database
     * It takes the User Id and creates a new user in the database
     * @param userId a String representing the User ID
     */
    private void newUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a Map or a custom object
        Map<String, Object> user = new HashMap<>();
        user.put("admin", true);
        user.put("check-ins", 0);
        user.put("name", "");
        user.put("homepage", "");
        user.put("email", "");
        user.put("geolocation", false);

        // Add a new document with the generated userId
        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "New User", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Potential failure stuff
                });
    }

    /**
     * This method calls the constructor to create a the user object if the user already
     * exists in the database.
     * @param userId A string for userId to pass to the constructor
     */
    private void existingUser(String userId) {
        user = new User(userId);
        // get the firebase messaging token
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {this.event = event;}

    /**
     * transitions the main fragment display (content) to the specified fragment with the given tag
     * @param fragment fragment to move to
     * @param tag internal tag that the app uses to know which fragment is open
     */
    public void transitionFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void addFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    public void hideFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onUsersLoaded(List<User> users)
    {
        if(users == null)
        {
            Toast.makeText(getApplicationContext(), "There was so such userList", Toast.LENGTH_SHORT).show();
            foundUserList = false;
        }
        else
        {
            foundUserList = true;
            usersList = users;

            // Handle the list of users
            for (User user : usersList) {
                if (user.getUserId().equals(userId)) {
                    foundUser = true;
                    break;
                }
            }
            if (foundUser) {
                existingUser(userId);
            } else {
                userId = UUID.randomUUID().toString();
                prefs.edit().putString(USER_ID_KEY, userId).apply();
                user = new User(userId);
                Log.d("Token", "Refreshed token: " + notificationToken);
                newUser(userId);
            }
        }
    }

    @Override
    public void onUserLoaded(User user) {

        if(user == null)
        {
            Toast.makeText(getApplicationContext(), "There was no such user", Toast.LENGTH_SHORT).show();
            foundUser = false;
        }
        else
        {
            this.user = user;
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
//                return false;
            }

            // create fragment of the type selected
            Fragment fragment1;
            if (Objects.equals(pressedTitle, dashboardTitle)) {
                fragment1 = new EventDashboardFragment();
            } else if (Objects.equals(pressedTitle, profileTitle)) {
                if (this.user != null && this.user.isAdmin()){
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

        }

    }
}