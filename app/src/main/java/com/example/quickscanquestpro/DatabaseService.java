package com.example.quickscanquestpro;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
        // Create a Map to store the data
        Map<String, Object> eventData = new HashMap<>();
        // Assuming your Event class has getters for its properties
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        // Add other properties as needed

        // Add the event data to the Firestore "Events" collection
        eventsRef.add(eventData);
    }
}
