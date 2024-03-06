package com.example.quickscanquestpro;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseService {

    private static final String EVENTS_COLLECTION = "Events";
    private static final String USERS_COLLECTION = "users";
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;

    private ArrayList<Event> events = new ArrayList<Event>();

    public interface OnUsersDataLoaded {
        void onUsersLoaded(List<User> users);
        void onError(Exception e);
    }

    public DatabaseService() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Reference to the "Events" collection
        eventsRef = db.collection(EVENTS_COLLECTION);
        usersRef = db.collection(USERS_COLLECTION);
    }

    public void addEvent(Event event) {
        // Create a Map to store the data
        Map<String, Object> eventData = new HashMap<>();
        // Assuming your Event class has getters for its properties
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("organizerId", event.getOrganizerId());

        // Combine all data into a single map
        Map<String, Object> combinedData = new HashMap<>();
        combinedData.putAll(eventData);
        combinedData.put("Start-date", event.getStartDate().toString());
        combinedData.put("End-date", event.getEndDate().toString());
        combinedData.put("Start-time", event.getStartTime().toString());
        combinedData.put("End-time", event.getEndTime().toString());

        // Add the event data to the Firestore "Events" collection with the incremented document number
        eventsRef.document(String.valueOf(event.getId())).set(combinedData, SetOptions.merge());
    }

    public void addUser(User user) {
        // Create a Map to store the data
        Map<String, Object> userData = new HashMap<>();
        // Assuming your User class has getters for its properties
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getMobileNum());
        userData.put("geoLocation", user.isGeolocation());
        userData.put("check-ins", user.getCheckins());
        userData.put("admin", false);

        // Add the user data to the Firestore "users" collection with the incremented document number
        usersRef.document(String.valueOf(user.getUserId())).set(userData, SetOptions.merge());
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String eventId = document.getId(); // Get the document ID
                Event event = new Event(Integer.parseInt(eventId));

                // Set other fields as before
                event.setTitle(document.getString("title"));
                event.setDescription(document.getString("description"));
                event.setLocation(document.getString("location"));
                event.setOrganizerId(document.getString("organizerId"));
                event.setStartDate(LocalDate.parse(document.getString("Start-date")));
                event.setEndDate(LocalDate.parse(document.getString("End-date")));
                event.setStartTime(LocalTime.parse(document.getString("Start-time")));
                event.setEndTime(LocalTime.parse(document.getString("End-time")));

                events.add(event);
            }
        });
        return events;
    }

    public void getUsers(OnUsersDataLoaded callback) {
        List<User> users = new ArrayList<>();

        usersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String userId = document.getId(); // Get the document ID
                User user = new User(userId);

                // Set other fields as before
//                user.setName(document.getString("name"));
//                user.setEmail(document.getString("email"));
//                user.setMobileNum(document.getString("phone"));
//                user.setGeolocation(document.getBoolean("geoLocation"));
//                user.setCheckins(document.getLong("check-ins").intValue());
                users.add(user);
            }
            callback.onUsersLoaded(users);
        }).addOnFailureListener(callback::onError);
    }

    public User getSpecificUser(String Userid)
    {
        User user = new User(Userid);
        usersRef.document(Userid).get().addOnSuccessListener(documentSnapshot -> {
            user.setName(documentSnapshot.getString("name"));
            user.setAdmin(documentSnapshot.getBoolean("admin"));
//            user.setEmail(documentSnapshot.getString("email"));
//            user.setMobileNum(documentSnapshot.getString("phone"));
//            user.setGeolocation(documentSnapshot.getBoolean("geoLocation"));
//            user.setCheckins(documentSnapshot.getLong("check-ins").intValue());
        });
        return user;
    }

}
