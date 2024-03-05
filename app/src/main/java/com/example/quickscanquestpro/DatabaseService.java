package com.example.quickscanquestpro;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DatabaseService {

    private static final String EVENTS_COLLECTION = "Events";
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    public DatabaseService() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Reference to the "Events" collection
        eventsRef = db.collection(EVENTS_COLLECTION);
    }

    public void addEvent(Event event) {
        // Get the last document number and increment it

            // Create a Map to store the data
            Map<String, Object> eventData = new HashMap<>();
            // Assuming your Event class has getters for its properties
            eventData.put("title", event.getTitle());
            eventData.put("description", event.getDescription());
            eventData.put("location", event.getLocation());

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

}
