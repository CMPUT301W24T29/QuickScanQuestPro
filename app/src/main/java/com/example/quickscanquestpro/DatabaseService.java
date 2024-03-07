package com.example.quickscanquestpro;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.OnProgressListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseService {

    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ArrayList<Event> events = new ArrayList<Event>();

    public interface OnUsersDataLoaded {
        void onUsersLoaded(List<User> users);
    }

    public interface OnUserDataLoaded {
        void onUserLoaded(User user);
    }

    public interface OnProfilePictureUpload {
        void onSuccess(String imageUrl, String imagePath);
        void onFailure(Exception e);
        void onProgress(double progress);
    }

    public interface OnProfilePictureDelete {
        void onSuccess();
        void onFailure(Exception e);
    }



    public interface OnEventDataLoaded {
        void onEventLoaded(Event event);
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
        userData.put("Homepage", user.getHomepage());
        userData.put("profilePictureUrl", user.getProfilePictureUrl());
        userData.put("profilePicturePath", user.getProfilePicturePath());

        // Add the user data to the Firestore "users" collection with the incremented document number
        usersRef.document(String.valueOf(user.getUserId())).set(userData, SetOptions.merge());
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String eventId = document.getId(); // Get the document ID
                Event event = new Event(eventId);

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
        usersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String userId = document.getId(); // Get the document ID
                User user = new User(userId);

                // Set user fields based on queryDocumentSnapshot
                user.setName(document.getString("name"));
                user.setEmail(document.getString("email"));
                user.setMobileNum(document.getString("phone"));
//                user.setGeolocation(document.getBoolean("geoLocation"));
//                user.setCheckins(document.getLong("check-ins").intValue());

                users.add(user);
            }
            callback.onUsersLoaded(users);
        }).addOnFailureListener(e -> callback.onUsersLoaded(null));
    }


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
            user.setAdmin(queryDocumentSnapshot.getBoolean("admin"));
//            user.setGeolocation(queryDocumentSnapshot.getBoolean("geoLocation"));
//            user.setCheckins(queryDocumentSnapshot.getLong("check-ins").intValue());

            callback.onUserLoaded(user);
        }).addOnFailureListener(e -> callback.onUserLoaded(null));
    }

    public void getEvent(String eventId, OnEventDataLoaded callback) {
        eventsRef.document(eventId).get().addOnSuccessListener(queryDocumentSnapshot -> {
                if (!queryDocumentSnapshot.exists()) {
                    callback.onEventLoaded(null);
                    return;
                }

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



}
