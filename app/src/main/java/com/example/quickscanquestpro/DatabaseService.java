package com.example.quickscanquestpro;


import static android.content.ContentValues.TAG;
import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;


import androidx.annotation.NonNull;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.OnProgressListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class to handle all database operations
 */
public class DatabaseService {

    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ArrayList<Event> events = new ArrayList<Event>();

    /**
     * Interfaces to handle the callback when the data is loaded
     */
    public interface OnUsersDataLoaded {
        void onUsersLoaded(List<User> users);
    }

    public interface OnUserDataLoaded {
        void onUserLoaded(User user);
    }

    /**
     * Interfaces to handle uploading profile picture
     */
    public interface OnProfilePictureUpload {
        void onSuccess(String imageUrl, String imagePath);
        void onFailure(Exception e);
        void onProgress(double progress);
    }



    /**
     * Interfaces to handle deleting profile picture
     */
    public interface OnProfilePictureDelete {
        void onSuccess();
        void onFailure(Exception e);
    }



    public interface OnEventDataLoaded {
        void onEventLoaded(Event event);
    }

    public interface OnEventsDataLoaded {
        void onEventsLoaded(List<Event> events);
    }

    /**
<<<<<<< HEAD
     * Callback interface for uploading event photos to Firebase Storage.
     * This interface is used to notify the caller of the progress, success, or failure of the upload operation.
     */
    public interface OnEventPhotoUpload {
        void onSuccess(String imageUrl, String imagePath);
        void onFailure(Exception e);
    }
    /**
     * Constructor to initialize the Firestore database
     */

    public DatabaseService() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Reference to the "Events" collection
        eventsRef = db.collection(EVENTS_COLLECTION);
        usersRef = db.collection(USERS_COLLECTION);
    }

    /**
     * Updates the check-ins array in a specified event document with a new check-in entry.
     * @param eventId The ID of the event to update.
     * @param userId The ID of the user checking in.
     * @param location The location associated with the check-in.
     */
    public void recordCheckIn(String eventId, String userId, String userName, String location) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Create a new check-in map to append to the 'checkins' array
        Map<String, Object> checkInMap = new HashMap<>();
        checkInMap.put("userId", userId);
        checkInMap.put("name", userName);
        checkInMap.put("location", location);
        checkInMap.put("timestamp", new Date());

        // Append the new check-in map to the 'checkins' array field
        eventRef.update("checkins", FieldValue.arrayUnion(checkInMap))
                .addOnSuccessListener(aVoid -> Log.d("DatabaseService", "New check-in added successfully."))
                .addOnFailureListener(e -> Log.e("DatabaseService", "Error adding new check-in.", e));
    }


    public void addEvent(Event event) {

        // Create a Map to store the data
        Map<String, Object> eventData = new HashMap<>();
        // Assuming your Event class has getters for its properties
        eventData.put("title", event.getTitle());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        eventData.put("organizerId", event.getOrganizerId());
        eventData.put("eventPictureUrl", event.getEventBannerUrl());
        eventData.put("eventPicturePath", event.getEventBannerPath());

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

    /**
     * Method to add a user to the Firestore database
     * @param user The user to be added
     */

    public void addUser(User user) {
        // Create a Map to store the data
        Map<String, Object> userData = new HashMap<>();
        // Assuming your User class has getters for its properties
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getMobileNum());
        userData.put("geoLocation", user.isGeolocation());
//        userData.put("check-ins", user.getCheckins());
        userData.put("Homepage", user.getHomepage());
        userData.put("profilePictureUrl", user.getProfilePictureUrl());
        userData.put("profilePicturePath", user.getProfilePicturePath());

        // Add the user data to the Firestore "users" collection with the incremented document number
        usersRef.document(String.valueOf(user.getUserId())).set(userData, SetOptions.merge());
    }

    /**
     * Method to get all events from the Firestore database
     * @param callback The callback to be called when the data is loaded
     */

    public void getEvents(OnEventsDataLoaded callback) {
        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String eventId = document.getId(); // Get the document ID
                Event event = new Event(eventId);

                // Set event fields based on queryDocumentSnapshot
                event.setTitle(document.getString("title"));
                event.setDescription(document.getString("description"));
                event.setLocation(document.getString("location"));
                event.setOrganizerId(document.getString("organizerId"));
                event.setStartDate(LocalDate.parse(document.getString("Start-date")));
                event.setEndDate(LocalDate.parse(document.getString("End-date")));
                event.setStartTime(LocalTime.parse(document.getString("Start-time")));
                event.setEndTime(LocalTime.parse(document.getString("End-time")));
                event.setEventBannerUrl(document.getString("eventPictureUrl"));
                event.setEventBannerPath(document.getString("eventPicturePath"));

                events.add(event);
            }
            callback.onEventsLoaded(events);
        }).addOnFailureListener(e -> callback.onEventsLoaded(null));
    }

    /**
     * Method to get all users from the Firestore database
     * @param callback The callback to be called when the data is loaded
     */

    public void getUsers(OnUsersDataLoaded callback) {
        usersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String userId = document.getId(); // Get the document ID
                String name = document.getString("name");
                User user = new User(userId);
                user.setName(name);

                // Set user fields based on queryDocumentSnapshot
                user.setName(document.getString("name"));
                user.setEmail(document.getString("email"));
                user.setMobileNum(document.getString("phone"));
                user.setHomepage(document.getString("Homepage"));
//                user.setGeolocation(document.getBoolean("geoLocation"));
//                user.setCheckins(document.getLong("check-ins").intValue());
                users.add(user);
            }
            callback.onUsersLoaded(users);
        }).addOnFailureListener(e -> callback.onUsersLoaded(null));
    }

    /**
     * Method to get a specific user from the Firestore database
     * @param userId The ID of the user to be fetched
     * @param callback The callback to be called when the data is loaded
     */


    public void getSpecificUserDetails(String userId, OnUserDataLoaded callback) {
        usersRef.document(userId).get().addOnSuccessListener(queryDocumentSnapshot -> {
            if (!queryDocumentSnapshot.exists()) {
                callback.onUserLoaded(null);
                return;
            }

            User user = new User(userId);

            // Set user fields based on queryDocumentSnapshot
            user.setName(queryDocumentSnapshot.getString("name"));
            user.setEmail(queryDocumentSnapshot.getString("email"));
            user.setMobileNum(queryDocumentSnapshot.getString("phone"));
            user.setHomepage(queryDocumentSnapshot.getString("Homepage"));
            user.setAdmin(queryDocumentSnapshot.getBoolean("admin"));
            user.setProfilePictureUrl(queryDocumentSnapshot.getString("profilePictureUrl"));
            user.setProfilePicturePath(queryDocumentSnapshot.getString("profilePicturePath"));
//            user.setGeolocation(queryDocumentSnapshot.getBoolean("geoLocation"));
//            user.setCheckins(queryDocumentSnapshot.getLong("check-ins").intValue());

            callback.onUserLoaded(user);
        }).addOnFailureListener(e -> callback.onUserLoaded(null));
    }

    /**
     * This will get a requested event from the Firestore database, then call a callback when the data is loaded into an event class
     * @param eventId the id of the event to search for in the database
     * @param callback the callback function in the class that called this, which will run when the data is loaded
     */
    public void getEvent(String eventId, OnEventDataLoaded callback) {
        eventsRef.document(eventId).get().addOnSuccessListener(queryDocumentSnapshot -> {
                if (!queryDocumentSnapshot.exists()) {
                    callback.onEventLoaded(null);
                    return;
                }

                // creating an event using its id will also create a QR code from the id it was given, which will always be the same
                Event event = new Event(eventId);

                // Set other fields as before
                event.setTitle(queryDocumentSnapshot.getString("title"));
                event.setDescription(queryDocumentSnapshot.getString("description"));
                event.setLocation(queryDocumentSnapshot.getString("location"));
                event.setOrganizerId(queryDocumentSnapshot.getString("organizerId"));
                event.setStartDate(LocalDate.parse(queryDocumentSnapshot.getString("Start-date")));
                event.setEndDate(LocalDate.parse(queryDocumentSnapshot.getString("End-date")));
                event.setStartTime(LocalTime.parse(queryDocumentSnapshot.getString("Start-time")));
                event.setEndTime(LocalTime.parse(queryDocumentSnapshot.getString("End-time")));
                event.setEventBannerUrl(queryDocumentSnapshot.getString("eventPictureUrl"));
                event.setEventBannerPath(queryDocumentSnapshot.getString("eventPicturePath"));
                // This is supposed to load event picture, but unsure if it works properly
                // To be implemented later
                /*String eventPictureUrl = queryDocumentSnapshot.getString("eventPictureUrl");
                if (eventPictureUrl != null) {
                    Glide.with(mainActivity)
                            .asBitmap()
                            .load(eventPictureUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    event.setEventBanner(resource);
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                }*/

                callback.onEventLoaded(event);
        }).addOnFailureListener(e -> callback.onEventLoaded(null));

    }


    public void getEventAttendees(String eventId, OnEventDataLoaded callback) {
            // Assuming eventsRef is a reference to the collection containing event documents
            DocumentReference eventDocRef = eventsRef.document(eventId);
            eventDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("DatabaseService", "Listen failed.", e);
                        callback.onEventLoaded(null);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Event event = new Event(eventId);
                        List<Map<String, Object>> checkInsList = (List<Map<String, Object>>) documentSnapshot.get("checkins");
                        if (checkInsList != null) {
                            ArrayList<User> attendees = new ArrayList<>();
                            for (Map<String, Object> checkInMap : checkInsList) {
                                // Extract data from the check-in map
                                String userId = (String) checkInMap.get("userId");
                                // Create a User object
                                User user = new User(userId);
                                // Add the user object to the list of attendees
                                attendees.add(user);
                            }
                            // Set the list of attendees to the event object
                            event.setAttendees(attendees);
                        }
                        callback.onEventLoaded(event);
                    } else {
                        callback.onEventLoaded(null);
                    }
                }
            });
        }

    /**
     * Uploads a profile picture to Firebase Storage and updates the user's profile in Firestore.
     * This method uploads the given image file to Firebase Storage under a unique path and,
     * upon successful upload, retrieves the image's URL. It then updates the User object with
     * this URL and the storage path, and finally updates the corresponding user document in Firestore.
     *
     * @param fileUri The URI of the file to upload. Must not be null.
     * @param user The user whose profile picture is being uploaded. This object is updated with
     *             the new profile picture URL and path upon successful upload.
     * @param callback An instance of {@link OnProfilePictureUpload}, which will be called with the
     *                 progress, success, or failure of the upload operation.
     */
    public void uploadProfilePicture(Uri fileUri, User user, OnProfilePictureUpload callback) {
        String refPath = "profilePictures/" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(refPath);

        ref.putFile(fileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progressPercentage = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    callback.onProgress(progressPercentage);
                })
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Update user with new profile picture URL and path
                    user.setProfilePictureUrl(imageUrl);
                    user.setProfilePicturePath(refPath);
                    // Update Firestore document for this user
                    addUser(user);
                    callback.onSuccess(imageUrl, refPath);
                }))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes the user's profile picture from Firebase Storage and updates the user's profile in Firestore.
     *
     * This method deletes the profile picture currently associated with the user from Firebase Storage.
     * Upon successful deletion, it clears the profile picture URL and path stored in the User object,
     * then updates the user's document in Firestore to reflect these changes.
     *
     * @param user The user whose profile picture is to be deleted. This object's profile picture URL
     *             and path are cleared upon successful deletion.
     * @param callback An instance of {@link OnProfilePictureDelete}, which will be called on the
     *                 success or failure of the deletion operation.
     */
    public void deleteProfilePicture(User user, OnProfilePictureDelete callback) {
        if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
            StorageReference picRef = storage.getReference().child(user.getProfilePicturePath());
            picRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Clear profile picture URL and path
                        user.setProfilePictureUrl(null);
                        user.setProfilePicturePath(null);
                        // Update Firestore document for this user
                        addUser(user);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }

    /**
     * Method to listen for updates to the users collection in the Firestore database
     * @param callback The callback to be called when the data is loaded
     */
    public void listenForUsersUpdates(OnUsersDataLoaded callback) {
        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DatabaseService", "Listen failed.", e);
                    return;
                }

                List<User> userList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String userId = doc.getId();
                    User user = new User(userId);
                    // Assuming you have a method in User class to set the name directly from Firestore document
                    user.setName(doc.getString("name")); // Ensure field name matches your Firestore structure
                    userList.add(user);
                }
                callback.onUsersLoaded(userList);
            }
        });
    }

    /**
     * Method to listen for updates to the events collection in the Firestore database
     * @param callback The callback to be called when the data is loaded
     */

    public void listenForEventUpdates(OnEventsDataLoaded callback) {
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DatabaseService", "Listen failed.", e);
                    return;
                }

                List<Event> eventList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String eventId = doc.getId();
                    Event event = new Event(eventId);
                    event.setTitle(doc.getString("title"));
                    eventList.add(event);
                }
                callback.onEventsLoaded(eventList);
            }
        });
    }

    /**
     * Method to delete a user in the Firestore database
     * @param user The user to be updated
     */
    public void deleteUser(User user){
        usersRef.document(user.getUserId()).delete();
    }

    /**
     * Method to delete an event in the Firestore database
     * @param event The event to be updated
     */

    public void deleteEvent(Event event){
        eventsRef.document(event.getId()).delete();
    }

    /**
     * Uploads an event photo to Firebase Storage and updates the event's document in Firestore.
     * @param fileUri The URI of the file to upload. Must not be null.
     * @param event The event to which the photo belongs. This object is updated with the new photo URL and path upon successful upload.
     * @param callback An instance of {@link OnEventPhotoUpload}, which will be called with the success, or failure of the upload operation.
     */
    public void uploadEventPhoto(Uri fileUri, Event event, OnEventPhotoUpload callback) {
        String refPath = "eventPictures/" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(refPath);

        ref.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    if (event != null) {
                        event.setEventBannerUrl(imageUrl);
                        event.setEventBannerPath(refPath);
                    } else {
                        // Handle the case where the event is null, maybe log an error or show a user-friendly message
                        Log.e("DatabaseService", "Cannot set event banner URL because the event is null");
                    }
                    if (event != null) {
                        updateEventInDatabase(event);
                    } else {
                        Log.e(TAG, "Event object is null.");
                        // Handle the null case appropriately, maybe notify the user or log the error.
                    }
                    callback.onSuccess(imageUrl, refPath);

                }))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateEventInDatabase(Event event) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("eventPictureUrl", event.getEventBannerUrl());
        updates.put("eventPicturePath", event.getEventBannerPath());
        Map<String, Object> combinedData = new HashMap<>();
        combinedData.putAll(updates);
        eventsRef.document(String.valueOf(event.getId())).set(combinedData, SetOptions.merge());
    }

}


