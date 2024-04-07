package com.example.quickscanquestpro;


import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Callback interface for uploading event photos to Firebase Storage.
     * This interface is used to notify the caller of the progress, success, or failure of the upload operation.
     */
    public interface OnEventPhotoUpload {
        void onSuccess(String imageUrl, String imagePath);
        void onFailure(Exception e);
        void onProgress(double progress);
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
    public void recordCheckIn(String eventId, String userId, String location) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Create a new check-in map to append to the 'checkins' array
        Map<String, Object> checkInMap = new HashMap<>();
        checkInMap.put("userId", userId);
        checkInMap.put("location", location);
        checkInMap.put("timestamp", new Date());

        // Append the new check-in map to the 'checkins' array field
        eventRef.update("checkins", FieldValue.arrayUnion(checkInMap))
                .addOnSuccessListener(aVoid -> Log.d("DatabaseService", "New check-in added successfully."))
                .addOnFailureListener(e -> Log.e("DatabaseService", "Error adding new check-in.", e));
    }

    public void updateLastCheckIn(String userId, String eventId){
        DocumentReference userRef = db.collection("users").document(userId);
        User user = new User(userId);
        user.setLastCheckIn(eventId);
        userRef.set(user, SetOptions.mergeFields("lastCheckIn"))
                .addOnSuccessListener(aVoid -> Log.d("DatabaseService", "Last Checked in event updated successfully"))
                .addOnFailureListener(e -> Log.e("DatabaseService", "Error updating last checked in event", e));
    }

    public void enableAdmin(String userId){
        DocumentReference userRef = db.collection("users").document(userId);
        User user = new User(userId);
        user.setAdmin(true);
        userRef.set(user, SetOptions.mergeFields("admin"))
                .addOnSuccessListener(aVoid -> Log.d("DatabaseService", "Admin enabled successfully"))
                .addOnFailureListener(e -> Log.e("DatabaseService", "Error enabling admin", e));
    }

    /**
     * Takes the passed in event and updates the database with the event attributes
     * @param event An event object with attributes to be updated in the database
     */
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
        eventData.put("customCheckin", event.getCustomCheckin());
        eventData.put("customPromo", event.getCustomPromo());



        eventData.put("signupLimit", event.getSignupLimit());


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
        userData.put("geolocation", user.isGeolocation());
        userData.put("check-ins", user.getCheckins());
        userData.put("Homepage", user.getHomepage());
        userData.put("profilePictureUrl", user.getProfilePictureUrl());
        userData.put("profilePicturePath", user.getProfilePicturePath());
        userData.put("NotificationToken", user.getNotificationToken());
        userData.put("lastCheckIn", user.getLastCheckIn());
        userData.put("ReceiveNotifications", user.getGetNotification());

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

                // Retrieve check-ins for this event
                ArrayList<Map<String, Object>> checkInsArray = (ArrayList<Map<String, Object>>) document.get("checkins");

                if (checkInsArray != null) {
                    // Process the check-ins array
                    ArrayList<CheckIn> checkIns = new ArrayList<>();
                    for (Map<String, Object> checkInMap : checkInsArray) {
                        String userId = (String) checkInMap.get("userId");
                        String location = (String) checkInMap.get("location");
                        checkIns.add(new CheckIn(userId, location));
                    }

                    event.setCheckIns(checkIns);
                }

                // Add the event to the list
                events.add(event);
            }

            // Call the callback with the list of events
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
                user.setNotificationToken(document.getString("NotificationToken"));
                user.setGeolocation(Boolean.TRUE.equals(document.getBoolean("geolocation")));
//                user.setCheckins(document.getLong("check-ins").intValue());
                user.setProfilePictureUrl(document.getString("profilePictureUrl"));
                user.setProfilePicturePath(document.getString("profilePicturePath"));
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
            user.setAdmin(Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("admin")));
            user.setProfilePictureUrl(queryDocumentSnapshot.getString("profilePictureUrl"));
            user.setProfilePicturePath(queryDocumentSnapshot.getString("profilePicturePath"));
            user.setNotificationToken(queryDocumentSnapshot.getString("NotificationToken"));
            user.setLastCheckIn(queryDocumentSnapshot.getString("lastCheckIn"));
            user.setGetNotification(queryDocumentSnapshot.getBoolean("ReceiveNotifications"));
            user.setGeolocation(Boolean.TRUE.equals(queryDocumentSnapshot.getBoolean("geolocation")));
//            user.setCheckins(queryDocumentSnapshot.getLong("check-ins").intValue());

            callback.onUserLoaded(user);
        }).addOnFailureListener(e -> callback.onUserLoaded(null));
    }

    public Event createEventFromDocumentSnapshot(String eventId, DocumentSnapshot queryDocumentSnapshot) {
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
        event.setCustomCheckin(queryDocumentSnapshot.getString("customCheckin"));
        event.setCustomPromo(queryDocumentSnapshot.getString("customPromo"));

        //update optional signuplimit
        Number signupLimitNumber = queryDocumentSnapshot.getLong("signupLimit"); // Using getLong for a more direct approach
        if (signupLimitNumber != null) {
            event.setSignupLimit(signupLimitNumber.intValue());
        }

        // Retrieve check-ins for this event
        ArrayList<Map<String, Object>> checkInsArray = (ArrayList<Map<String, Object>>) queryDocumentSnapshot.get("checkins");
        if (checkInsArray != null) {
            // Process the check-ins array
            ArrayList<CheckIn> checkIns = new ArrayList<>();
            for (Map<String, Object> checkInMap : checkInsArray) {
                String userId = (String) checkInMap.get("userId");
                String location = (String) checkInMap.get("location");
                checkIns.add(new CheckIn(userId, location));
            }

            event.setCheckIns(checkIns);
        }
        return event;
    }

    /**
     * This function searches the database for an existing event that either has an ID that is the same as the string being passed in
     * Or that has a customCheckin or customPromo attribute that contains the string passed in. Then it returns the Event with details from
     * the database for this found event, or null otherwise if no event was found.
     * @param customQR The string to search the database for
     * @param callback The function to call once the data is retrieved and the event is found/not found, passes in either Event or null
     */
    public void getEventWithCustomQR(String customQR, OnEventDataLoaded callback) {
        if (customQR.startsWith("c") || customQR.startsWith("p")) {
            // may be attempting to get a "custom" qr that is just the ID of an existing event
            getEvent(customQR.substring(1), event -> {
                if (event != null) {
                    // found an existing event with that QR code, so return it
                    callback.onEventLoaded(event);
                } else {
                    // else, this qr just happens to start with c or p but its not actually used as an event id, so go ahead and look for it in the rest of the custom codes
                    // search database for an event that has a customCheckin or customPromo attribute matching the scanned code
                    eventsRef.where(Filter.or(
                            Filter.equalTo("customCheckin", customQR),
                            Filter.equalTo("customPromo", customQR))
                    ).get().addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            // no matching events found
                            callback.onEventLoaded(null);
                            return;
                        } else {
                            DocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);
                            Event customQREvent = createEventFromDocumentSnapshot(queryDocumentSnapshot.getId(), queryDocumentSnapshot);
                            callback.onEventLoaded(customQREvent);
                        }
                    }).addOnFailureListener(e -> callback.onEventLoaded(null));
                }
            });
        } else {
            // search database for an event that has a customCheckin or customPromo attribute matching the scanned code
            eventsRef.where(Filter.or(
                    Filter.equalTo("customCheckin", customQR),
                    Filter.equalTo("customPromo", customQR))
            ).get().addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    // no matching events found
                    callback.onEventLoaded(null);
                    return;
                } else {
                    DocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);
                    Event customQREvent = createEventFromDocumentSnapshot(queryDocumentSnapshot.getId(), queryDocumentSnapshot);
                    callback.onEventLoaded(customQREvent);
                }
            }).addOnFailureListener(e -> callback.onEventLoaded(null));
        }
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

            Event event = createEventFromDocumentSnapshot(eventId, queryDocumentSnapshot);
            callback.onEventLoaded(event);

        }).addOnFailureListener(e -> callback.onEventLoaded(null));

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
                    user.setName(doc.getString("name"));
                    user.setProfilePictureUrl(doc.getString("profilePictureUrl"));
                    user.setProfilePicturePath(doc.getString("profilePicturePath"));

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
                .addOnProgressListener(taskSnapshot -> {
                    double progressPercentage = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    callback.onProgress(progressPercentage);
                })
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
                        addEvent(event);
                    } else {
                        Log.e(TAG, "Event object is null.");
                        // Handle the null case appropriately, maybe notify the user or log the error.
                    }
                    callback.onSuccess(imageUrl, refPath);

                }))
                .addOnFailureListener(callback::onFailure);
    }


    /**
     * This interface is implemented by userSignup
     * Used as a callback for Signup button to know if the signup was successful
     */
    public interface SignupCallback {
        void onSuccess();
        void onSignupLimitReached();
        void onFailure(Exception e);
    }

    /**
     * This method is called when the signup button is pressed in an Event Details Fragment.
     * It Creates A List of Users in the database to represents signups.
     * Also implements a callback function so the method that calls this method will know the sigun status
     * @param user User object to be added as a signup
     * @param event Event object that will hold the signups field
     * @param callback A callback function to be able to tell the signup function if the signup was successful
     */
    public void userSignup(User user, Event event, SignupCallback callback) {
        db.runTransaction(transaction -> {
            DocumentReference userRef = usersRef.document(user.getUserId());
            DocumentReference eventRef = eventsRef.document(event.getId());

            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            List<String> currentSignups = (List<String>) eventSnapshot.get("signups");
            Number signupLimit = (Number) eventSnapshot.get("signupLimit");

            // Check is event signups are filled
            if (signupLimit != null && currentSignups != null && currentSignups.size() >= signupLimit.intValue()) {
                return "Signup limit reached";
            }

            transaction.update(eventRef, "signups", FieldValue.arrayUnion(user.getUserId()));
            transaction.update(userRef, "signedUpEvents", FieldValue.arrayUnion(event.getId()));

            return "Success";
        }).addOnSuccessListener(result -> {
            if ("Signup limit reached".equals(result)) {
                callback.onSignupLimitReached();
            } else {
                callback.onSuccess();
            }
        }).addOnFailureListener(e -> {
            callback.onFailure(e);
        });
    }


    /**
     * This interface is implemented by getUserSignedupEvents
     * functions as a callback function
     */
    public interface OnSignedUpEventsLoaded {
        void onSignedUpEventsLoaded(List<Event> events);
    }

    /**
     * This method is called in EventDashboardFragment
     * @param user
     * @param callback
     */
    public void getUserSignedupEvents(User user, OnSignedUpEventsLoaded callback) {
        DocumentReference userRef = usersRef.document(user.getUserId());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("signedUpEvents")) {
                List<String> eventIds = (List<String>) documentSnapshot.get("signedUpEvents");
                if (eventIds != null && !eventIds.isEmpty()) {
                    List<Event> signedUpEvents = new ArrayList<>();
                    AtomicInteger eventsCount = new AtomicInteger(eventIds.size());
                    for (String eventId : eventIds) {
                        getEvent(eventId, event -> {
                            if (event != null) {
                                // Checks if the events date is already passed
                                LocalDate endDate = event.getEndDate();
                                LocalDateTime endDateTime = endDate.atTime(event.getEndTime());
                                if (endDateTime.isAfter(LocalDateTime.now())) {
                                    signedUpEvents.add(event);
                                }
                            }
                            if (eventsCount.decrementAndGet() == 0) {
                                callback.onSignedUpEventsLoaded(signedUpEvents);
                            }
                        });
                    }
                } else {
                    // if there are no signups
                    callback.onSignedUpEventsLoaded(Collections.emptyList());
                }
            } else {
                callback.onSignedUpEventsLoaded(Collections.emptyList());
            }
        }).addOnFailureListener(e -> {
            Log.e("DatabaseService", "Error fetching signedUpEvents", e);
            callback.onSignedUpEventsLoaded(Collections.emptyList());
        });
    }

    public interface OnEventSignUpsLoaded {
        void onSignUpsLoaded(List<User> users);
    }

    public void getEventSignUps(String eventId, OnEventSignUpsLoaded callback) {
        DocumentReference eventRef = eventsRef.document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("signups")) {
                List<String> userIds = (List<String>) documentSnapshot.get("signups");
                if (userIds != null && !userIds.isEmpty()) {
                    List<User> signedUpUsers = new ArrayList<>();
                    AtomicInteger usersCount = new AtomicInteger(userIds.size());

                    for (String userId : userIds) {
                        getSpecificUserDetails(userId, new OnUserDataLoaded() {
                            @Override
                            public void onUserLoaded(User user) {
                                if (user != null) {
                                    signedUpUsers.add(user);
                                }
                                if (usersCount.decrementAndGet() == 0) {
                                    callback.onSignUpsLoaded(signedUpUsers);
                                }
                            }
                        });
                    }
                } else {
                    // If there are no users signed up for the event
                    callback.onSignUpsLoaded(new ArrayList<>());
                }
            } else {
                // If the event does not exist or does not have any signups
                callback.onSignUpsLoaded(new ArrayList<>());
            }
        }).addOnFailureListener(e -> {
            // In case of any failure
            callback.onSignUpsLoaded(new ArrayList<>());
        });
    }




    public void deleteUserProfilePicture(String userId, OnProfilePictureDelete callback) {
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);
        userRef.update("profilePictureUrl", null)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteEventPhoto(String eventId, OnProfilePictureDelete callback) {
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(eventId);
        eventRef.update("eventPictureUrl", null)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void clearEventCheckins(String eventId) {
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(eventId);
        eventRef.update("checkins", null);
    }



}


